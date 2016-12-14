package server;

import client.util.PatchedGIFImageReader;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;
import shared.Notification;
import shared.exceptions.ConnectException;
import shared.util.ImageUtils;
import shared.util.Log;
import shared.util.RunnableAdapter;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GIF Streaming Source
 *
 * @author Huw Jones
 * @since 13/12/2016
 */
public class GifStreamer extends NotificationSource {

    private static DecimalFormat df = new DecimalFormat("00.00");
    /**
     * ArrayList of the frames of the GIF (stored as bytes so they can be serialised)
     */
    private ArrayList<byte[]> images;
    private ConcurrentHashMap<Integer, BufferedImage> loadedImages;
    private ConcurrentHashMap<Integer, byte[]> processedImages;

    private double percentage = 0d;

    /**
     * Frame interval in 100th seconds (10 ms/0.01s)
     */
    private int interval = 100;

    public GifStreamer(File gifFile) throws RemoteException {
        super(Config.getServerID() + " " + gifFile.getName().split("\\.")[0]);

        // Check file exists
        if (!gifFile.exists()) {
            Log.Fatal("Cannot start GifStreamer, file does not exist: " + gifFile.getAbsolutePath());
            return;
        }
        // And we can read it
        if (!gifFile.canRead()) {
            Log.Fatal("Cannot start GifStreamer, cannot read file: " + gifFile.getAbsolutePath());
            return;
        }

        // Process GIF file and extract frames
        try {
            processGif(gifFile);
            processImageList();
            loadedImages = null;
            processedImages = null;
        } catch (IOException e) {
            Log.Fatal("Failed to process gif!");
            return;
        }

        // Bind the server to the registry
        try {
            bind();
        } catch (ConnectException ex) {
            Log.Fatal("Failed to bind server: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        // Start streaming thread
        Thread t = new StreamThread();
        Log.Info("Starting source...");
        t.start();
        Log.Info("Source started and ready for clients!");
    }

    /**
     * Extract metadata node from file
     * <p>
     * I take no credit for this method as it uses code sourced from: <ul>
     * <li>c24w - http://stackoverflow.com/a/8935070/5909019</li>
     * <li>Ansel Zandegran - http://stackoverflow.com/a/16234122/5909019</li>
     * <li>Sage - http://stackoverflow.com/a/20079110/5909019</li>
     * </ul>
     *
     * @param rootNode Root metadata node
     * @param nodeName Node to retrieve
     */
    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0) {
                return ((IIOMetadataNode) rootNode.item(i));
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return (node);
    }

    /**
     * Process the gif file and convert it to an array of frames and set frame interval time.
     * <p>
     * I take no credit for this method as it uses code sourced from: <ul>
     * <li>c24w - http://stackoverflow.com/a/8935070/5909019</li>
     * <li>Ansel Zandegran - http://stackoverflow.com/a/16234122/5909019</li>
     * <li>Sage - http://stackoverflow.com/a/20079110/5909019</li>
     * </ul>
     *
     * @param gif File to process
     * @throws IOException
     */
    private void processGif(File gif) throws IOException {
        // Read GIF file
        ImageReader ir = new PatchedGIFImageReader(new GIFImageReaderSpi());
        ir.setInput(ImageIO.createImageInputStream(gif));

        int numberOfImages = ir.getNumImages(true);


        MonitorThread m = new MonitorThread("Reading");
        m.start();

        loadedImages = new ConcurrentHashMap<>(numberOfImages);
        // Extract all the images from the file
        for (int i = 0; i < numberOfImages; i++) {
            loadedImages.put(i, ir.read(i));
            percentage = (i + 1) * 100d / numberOfImages;
            synchronized (this) {
                this.notify();
            }
        }
        m.done();
        System.out.println();
        Log.Info("GIF loaded!");

        percentage = 0d;
        processedImages = new ConcurrentHashMap<>();
        m = new MonitorThread("Converting");
        m.start();
        for (int i = 0; i < numberOfImages; i++) {
            dispatchEvent(new ImageProcessor(i));
        }
        do {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                }
            }
        } while ((processedImages.size() != loadedImages.size()));
        m.done();
        System.out.println();
        Log.Info("GIF converted!");

        // Get the frame interval time
        IIOMetadata gifMetaData = ir.getImageMetadata(0);
        String formatName = gifMetaData.getNativeMetadataFormatName();

        IIOMetadataNode rootNode = (IIOMetadataNode) gifMetaData.getAsTree(formatName);
        IIOMetadataNode graphicsControlExtensionNode = getNode(rootNode, "GraphicControlExtension");

        String delayTime = graphicsControlExtensionNode.getAttribute("delayTime");
        this.interval = Integer.parseInt(graphicsControlExtensionNode.getAttribute("delayTime"));
        if(this.interval == 0){
            this.interval = 10;
        }
        Log.Info("Delay time: " + this.interval);
    }

    private void processImageList() {
        images = new ArrayList<>(processedImages.size());
        for (int i = 0; i < processedImages.size(); i++) {
            images.add(processedImages.get(i));
        }
    }

    private class ImageProcessor extends RunnableAdapter {
        int number;

        public ImageProcessor(int number) {
            this.number = number;
        }

        @Override
        public void runSafe() throws Exception {
            BufferedImage bufferedImage = loadedImages.get(number);
            byte[] imageBytes = ImageUtils.imageToBytes(bufferedImage);
            processedImages.put(number, imageBytes);
            percentage = processedImages.size() * 100d / loadedImages.size();
            synchronized (GifStreamer.this) {
                GifStreamer.this.notify();
            }
        }
    }

    /**
     * Thread to stream the images
     */
    private class StreamThread extends Thread {
        public StreamThread() {
            super("StreamThread");
        }

        @Override
        public void run() {
            boolean shouldExit = false;
            while (!shouldExit) {
                for (int i = 0; i < images.size(); i++) {
                    sendNotification(new Notification<>(GifStreamer.this.sourceID, images.get(i)));
                    try {
                        Thread.sleep(interval * 10);
                    } catch (InterruptedException e) {
                        shouldExit = true;
                    }
                }
            }
        }
    }

    private class MonitorThread extends Thread {

        boolean shouldQuit = false;
        private String verb;

        public MonitorThread(String verb) {
            super("MonitorThread");
            this.setDaemon(true);
            this.verb = verb;
        }

        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.Info(String.format("%s GIF... %s%%\r", verb, df.format(percentage)), false);
            } while (!shouldQuit);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {

            }
            synchronized (GifStreamer.this){
                GifStreamer.this.notifyAll();
            }
        }

        public void done() {
            shouldQuit = true;
        }
    }
}
