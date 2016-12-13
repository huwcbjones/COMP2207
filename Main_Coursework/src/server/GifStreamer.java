package server;

import client.util.PatchedGIFImageReader;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;
import shared.Notification;
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
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 13/12/2016
 */
public class GifStreamer extends NotificationSource {

    private ArrayList<byte[]> images;
    /**
     * Frame interval in 100th seconds (10 ms/0.01s)
     */
    private int interval = 100;

    public GifStreamer(File gifFile) throws RemoteException {
        super("GifStream " + gifFile.getName());
        try {
            ArrayList<BufferedImage> images = processGif(gifFile);
            processImageList(images);
        } catch (IOException e) {
            Log.Fatal("Failed to process gif!");
            return;
        }
        bind();
        Thread t = new StreamThread();
        t.start();
    }

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
        ImageReader ir = new PatchedGIFImageReader(new GIFImageReaderSpi());
        ir.setInput(ImageIO.createImageInputStream(gif));

        ArrayList<BufferedImage> imageList = new ArrayList<>(ir.getNumImages(true));
        for (int i = 0; i < ir.getNumImages(true); i++)
            imageList.add(ir.read(i));

        IIOMetadata gifMetaData = ir.getImageMetadata(0);
        String formatName = gifMetaData.getNativeMetadataFormatName();

        IIOMetadataNode rootNode = (IIOMetadataNode) gifMetaData.getAsTree(formatName);
        IIOMetadataNode graphicsControlExtensionNode = getNode(rootNode, "GraphicControlExtension");

        String delayTime = graphicsControlExtensionNode.getAttribute("delayTime");
        Log.Info("Delay time: " + delayTime);
        this.interval = Integer.parseInt(graphicsControlExtensionNode.getAttribute("delayTime"));

        return imageList;
    }

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
