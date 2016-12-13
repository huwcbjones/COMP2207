package client.components;

import client.GifClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * Window for displaying Images (or Gifs)
 *
 * @author Huw Jones
 * @since 13/12/2016
 */
public class GifWindow extends JFrame implements AutoCloseable {

    ImagePanel imagePanel;
    String sourceID;

    public GifWindow(String sourceID) {
        super(sourceID);

        // Create GUI
        this.sourceID = sourceID;
        this.setMinimumSize(new Dimension(400, 300));
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.addWindowListener(new WindowHandler());

        imagePanel = new ImagePanel();
        this.setContentPane(imagePanel);
    }

    /**
     * Displays an image in the window
     *
     * @param image
     */
    public void displayImage(BufferedImage image) {
        SwingUtilities.invokeLater(() -> imagePanel.setImage(image, true));
    }

    @Override
    public void close() {
        this.dispose();
    }

    /**
     * Handler to unregister from source when window closed
     */
    private class WindowHandler extends WindowAdapter {
        /**
         * Invoked when a window is in the process of being closed.
         * The close operation can be overridden at this point.
         *
         * @param e
         */
        @Override
        public void windowClosing(WindowEvent e) {
            GifClient.sourceDisconnect(sourceID);
        }
    }
}
