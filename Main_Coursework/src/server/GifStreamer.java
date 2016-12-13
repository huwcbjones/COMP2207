package server;

import client.util.PatchedGIFImageReader;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;
import shared.Notification;
import shared.exceptions.ConnectException;
import shared.util.ImageUtils;
import shared.util.Log;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * GIF Streaming Source
 *
 * @author Huw Jones
 * @since 13/12/2016
 */
public class GifStreamer extends NotificationSource {

    /**
     * ArrayList of the frames of the GIF (stored as bytes so they can be serialised)
     */
    private ArrayList<byte[]> images;

    /**
     * Frame interval in 100th seconds (10 ms/0.01s)
     */
    private int interval = 100;

    public GifStreamer(File gifFile) throws RemoteException {
        super(Config.getServerID() + " " + gifFile.getName().split("\\.")[0]);

        // Check file exists
        if(!gifFile.exists()){
            Log.Fatal("Cannot start GifStreamer, file does not exist: " + gifFile.getAbsolutePath());
            return;
        }
        // And we can read it
        if(!gifFile.canRead()){
            Log.Fatal("Cannot start GifStreamer, cannot read file: " + gifFile.getAbsolutePath());
            return;
        }

        // Process GIF file and extract frames
        try {
            ArrayList<BufferedImage> images = processGif(gifFile);
            processImageList(images);
        } catch (IOException e) {
            Log.Fatal("Failed to process gif!");
            return;
        }

        // Bind the server to the registry
        try {
            bind();
        } catch (ConnectException ex){
            Log.Fatal("Failed to bind server: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        // Start streaming thread
        Thread t = new StreamThread();
        t.start();
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
    private ArrayList<BufferedImage> processGif(File gif) throws IOException {
        // Read GIF file
        ImageReader ir = new PatchedGIFImageReader(new GIFImageReaderSpi());
        ir.setInput(ImageIO.createImageInputStream(gif));

        // Extract all the images from the file
        ArrayList<BufferedImage> imageList = new ArrayList<>(ir.getNumImages(true));
        for (int i = 0; i < ir.getNumImages(true); i++)
            imageList.add(ir.read(i));

        // Get the frame interval time
        IIOMetadata gifMetaData = ir.getImageMetadata(0);
        String formatName = gifMetaData.getNativeMetadataFormatName();

        IIOMetadataNode rootNode = (IIOMetadataNode) gifMetaData.getAsTree(formatName);
        IIOMetadataNode graphicsControlExtensionNode = getNode(rootNode, "GraphicControlExtension");

        String delayTime = graphicsControlExtensionNode.getAttribute("delayTime");
        Log.Info("Delay time: " + delayTime);
        this.interval = Integer.parseInt(graphicsControlExtensionNode.getAttribute("delayTime"));

        return imageList;
    }

    /**
     * Process all the BufferedImages and convert them into bytes - suitable for transporting over the network
     * @param imageList List of Buffered Images
     */
    private void processImageList(ArrayList<BufferedImage> imageList) {
        this.images = new ArrayList<>(imageList.size());
        for (int i = 0; i < imageList.size(); i++){
            try {
                this.images.add(ImageUtils.imageToBytes(imageList.get(i)));
            } catch (IOException e) {
                Log.Error("Failed to convert image to byte. Index: " + i);
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
}
