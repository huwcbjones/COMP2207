package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 13/12/2016
 */
public class GifWindow extends JFrame implements AutoCloseable {

    ImagePanel imagePanel;
    String sourceID;

    public GifWindow(String sourceID){
        super(sourceID);
        this.sourceID = sourceID;
        this.setMinimumSize(new Dimension(400, 300));
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowHandler());

        imagePanel = new ImagePanel();
        this.setContentPane(imagePanel);
    }

    public void displayImage(BufferedImage image){
        imagePanel.setImage(image, true);
    }

    @Override
    public void close() {
        this.dispose();
    }

    private class WindowHandler extends WindowAdapter {
        /**
         * Invoked when a window is in the process of being closed.
         * The close operation can be overridden at this point.
         *
         * @param e
         */
        @Override
        public void windowClosing(WindowEvent e) {
           NotificationSink sink = GifClient.getSink();
           sink.disconnectSource(sourceID);
        }
    }
}
