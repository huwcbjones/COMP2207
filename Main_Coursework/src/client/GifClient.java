package client;

import client.components.GifWindow;
import client.components.HintTextFieldUI;
import javafx.util.Pair;
import shared.exceptions.ConnectException;
import shared.interfaces.INotificationSource;
import shared.util.ImageUtils;
import shared.util.Log;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client GUI Class
 *
 * @author Huw Jones
 * @since 06/12/2016
 */
public class GifClient extends JFrame {

    private static NotificationSink sink;
    JPanel panel_gui;
    JButton button_disconnect;
    JButton button_connect;
    JLabel label_source;
    JTextField text_source;
    JComboBox<String> combo_source;
    JLabel label_server;
    JTextField text_server;
    JLabel label_port;
    JTextField text_port;

    ConcurrentHashMap<String, GifWindow> gifWindows = new ConcurrentHashMap<>();

    ActionListener rmiConnectListener = new RMIConnect();
    ActionListener sourceConnectListener = new SourceConnect();

    public GifClient() {
        super("RMI Client");
        try {
            GifClient.sink = new NotificationSink();
        } catch (RemoteException ex) {
            Log.Fatal("Failed to load client: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }

        this.initUI();
        this.initEventListeners();

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(600, 400));
        this.setLocationRelativeTo(null);
        this.setResizable(true);

        // Handle autoconnect
        if (Config.getRmiServer() != null) {
            text_server.setText(Config.getRmiServer());
        }
        if (Config.getRmiPort() != null) {
            text_port.setText(Config.getRmiPort().toString());
        }

        if (Config.isAutoconnect()) {
            rmiConnectListener.actionPerformed(null);
        }

        this.setVisible(true);
    }

    /**
     * Gets the sink for this client
     *
     * @return Sink
     */
    public static NotificationSink getSink() {
        return sink;
    }

    /**
     * Initialise the UI - lots of Swing
     */
    private void initUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            Log.Warn(ex.getMessage());
        }

        this.panel_gui = new JPanel(new GridBagLayout());
        this.panel_gui.setBorder(new EmptyBorder(32, 96, 0, 96));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridwidth = 2;
        int row = 0;

        //region Source/Server/Port
        this.label_source = new JLabel("Source", JLabel.LEADING);
        this.label_source.setLabelFor(this.combo_source);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.panel_gui.add(this.label_source, c);
        row++;

        this.text_source = new JTextField();
        this.text_source.setEnabled(false);
        this.text_source.setVisible(false);
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.panel_gui.add(this.text_source, c);
        row++;

        this.combo_source = new JComboBox<>();
        this.combo_source.setEnabled(false);
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.panel_gui.add(this.combo_source, c);
        row++;

        this.label_server = new JLabel("Server", JLabel.LEADING);
        this.label_server.setLabelFor(this.text_server);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.panel_gui.add(this.label_server, c);
        row++;

        this.text_server = new JTextField();
        this.text_server.setUI(new HintTextFieldUI("Server (default: localhost)", true));
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.panel_gui.add(this.text_server, c);
        row++;

        this.label_port = new JLabel("Port", JLabel.LEADING);
        this.label_port.setLabelFor(this.text_port);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.panel_gui.add(this.label_port, c);
        row++;

        this.text_port = new JTextField();
        this.text_port.setUI(new HintTextFieldUI("Port (default: 1099)", true));
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.panel_gui.add(this.text_port, c);
        row++;
        //endregion

        //region Source List
        //endregion

        //region Buttons
        this.button_disconnect = new JButton("Disconnect");
        this.button_disconnect.setMnemonic('d');
        this.button_disconnect.setEnabled(false);
        c.insets = new Insets(6, 0, 6, 0);
        c.weightx = 0.5;
        c.gridwidth = 1;
        c.gridy = row;
        this.panel_gui.add(this.button_disconnect, c);

        this.button_connect = new JButton("Connect");
        this.button_connect.setMnemonic('c');
        c.insets = new Insets(6, 0, 6, 0);
        c.gridy = row;
        c.gridx = 1;
        c.weightx = 0.5;
        this.panel_gui.add(this.button_connect, c);
        row++;
        //endregion

        this.setContentPane(this.panel_gui);
        this.getRootPane().setDefaultButton(this.button_connect);

    }

    /**
     * Initialise event listeners
     */
    private void initEventListeners() {
        this.button_connect.addActionListener(rmiConnectListener);

        this.button_disconnect.addActionListener(e -> {
            // Close all open GifWindows
            gifWindows.entrySet().forEach(w -> {
                w.getValue().dispose();
                gifWindows.remove(w.getKey());
            });

            if (sink.isConnectedSource()) {
                sink.disconnectAllSource();
            }
            if (sink.isConnectedRMIProxy()) {
                sink.disconnectRMIProxy();
            }

            // Set form controls to default state
            button_disconnect.setEnabled(false);
            text_source.setEnabled(false);
            combo_source.setEnabled(false);
            combo_source.removeAllItems();
            text_server.setEnabled(true);
            text_port.setEnabled(true);

            // Set action listeners back to default state
            button_connect.removeActionListener(sourceConnectListener);
            button_connect.addActionListener(rmiConnectListener);

            this.setTitle("RMI Client");
        });
    }

    /**
     * Connects to a source as specified by the combo box/text field
     */
    private void sourceConnect() {
        sourceConnect(null);
    }

    /**
     * Connects to the source with the specified SourceID
     *
     * @param source SourceID of the source to connec to
     */
    private void sourceConnect(String source) {
        try {
            // If null, fallback to getting the SourceID from the text field/combo box
            if (source == null) {
                if (combo_source.isVisible()) {
                    source = (String) combo_source.getSelectedItem();
                } else if (text_source.isVisible()) {
                    source = text_source.getText();
                }
            }

            // If there is no specified source, inform the user and return
            if (source == null || source.length() == 0) {
                JOptionPane.showMessageDialog(GifClient.this,
                        "Please enter/select the ID of the source you'd like to connect to.",
                        "Failed to connect",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Otherwise, check if we're already connected, if so, bring the window to the front
            if (gifWindows.containsKey(source)) {
                GifWindow window = gifWindows.get(source);
                window.setVisible(true);
                window.toFront();
                window.repaint();
            } else {
                // Else, connect to the source
                SourceConnectThread t = new SourceConnectThread(source);
                t.start();
            }
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    GifClient.this,
                    ex.getMessage(),
                    "Failed to connect", JOptionPane.ERROR_MESSAGE));
        }
    }

    /**
     * Thread to Connect to the RMI Server/Proxy Server
     */
    private class ConnectThread extends Thread {
        private String rmiServer;
        private int port;

        public ConnectThread(String rmiServer, int port) {
            super("ConnectThread");
            this.rmiServer = rmiServer;
            this.port = port;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void run() {
            // Try to connect to SourceProxy
            try {
                // Connect to proxy server with a callback to update the combo box list
                sink.connectRMIProxy(rmiServer, port, (n) -> {
                    ArrayList<Pair<String, INotificationSource>> data = (ArrayList<Pair<String, INotificationSource>>) n.getData();
                    SwingUtilities.invokeLater(() -> {
                        combo_source.removeAllItems();
                        data.stream().map(Pair::getKey).forEach(combo_source::addItem);
                    });
                });
                SwingUtilities.invokeLater(() -> {
                    combo_source.setVisible(true);
                    text_source.setVisible(false);
                });
            } catch (ConnectException ex) {
                Log.Error(ex.getMessage() + " " + ex.getCause().getMessage());
            }

            // The RMI server could be running, but not the proxy
            if (!sink.isConnectedRMI()) {
                // RMI server isn't running
                SwingUtilities.invokeLater(() -> {
                    button_connect.setEnabled(true);
                    button_disconnect.setEnabled(false);
                    combo_source.setEnabled(false);
                    text_source.setEnabled(false);
                    text_server.setEnabled(true);
                    text_port.setEnabled(true);
                    JOptionPane.showMessageDialog(
                            GifClient.this,
                            "Failed to connect to RMI Registry.",
                            "Failed to connect.", JOptionPane.ERROR_MESSAGE);
                });
                return;
            }

            // Now we know the Proxy Server isn't running, so check that we are connected to the RMI Registry - if not, try to connect.
            // The client can't get a list of sources that are registered, so let them manually type into a text box
            if (!sink.isConnectedRMI()) {
                Log.Info("Connecting to RMI registry...");
                try {
                    sink.connectRMI(rmiServer, port);
                } catch (ConnectException ex) {
                    SwingUtilities.invokeLater(() -> {
                        button_connect.setEnabled(true);
                        button_disconnect.setEnabled(false);
                        combo_source.setEnabled(false);
                        text_server.setEnabled(true);
                        text_port.setEnabled(true);
                        button_connect.addActionListener(rmiConnectListener);
                        button_connect.removeActionListener(sourceConnectListener);
                        JOptionPane.showMessageDialog(
                                GifClient.this,
                                ex.getMessage(),
                                "Failed to connect.", JOptionPane.ERROR_MESSAGE);
                    });
                }
            } else {
                SwingUtilities.invokeLater(() -> {
                    combo_source.setVisible(false);
                    text_source.setVisible(true);
                });
            }


            Log.Info("Connected to  RMI registry!");

            // Can't use SourceProxy to get a list of sources, so enable manual bind
            SwingUtilities.invokeLater(() -> {

                GifClient.this.setTitle(String.format("RMI Client - %s:%s", rmiServer, port));
                button_connect.removeActionListener(rmiConnectListener);
                button_connect.addActionListener(sourceConnectListener);

                button_connect.setEnabled(true);
                button_disconnect.setEnabled(true);
                combo_source.setEnabled(true);
                text_source.setEnabled(true);
                text_server.setEnabled(false);
                text_port.setEnabled(false);
            });

            // If autoconnect, then connect to all the sources
            if(Config.isAutoconnect()) {
                for (String source : Config.getSources()) {
                    sourceConnect(source);
                }
            }
        }
    }

    /**
     * Thread to Connect to a Source
     */
    private class SourceConnectThread extends Thread {

        private String sourceID;

        public SourceConnectThread(String sourceID) {
            super("SourceThread");
            this.sourceID = sourceID;
        }

        @Override
        public void run() {
            // Create a window
            GifWindow window = new GifWindow(sourceID);

            // Disable GUI temporarily
            SwingUtilities.invokeLater(() -> {
                button_connect.setEnabled(false);
                text_source.setEnabled(false);
                combo_source.setEnabled(false);
            });

            try {
                // Connect to the source with a callback to update the GifWindow
                sink.connectSource(sourceID, n -> SwingUtilities.invokeLater(() -> {
                    try {
                        window.displayImage(ImageUtils.bytesToImage((byte[]) n.getData()));
                    } catch (IOException e1) {
                        Log.Error("Failed to convert bytes to image: " + e1.getMessage());
                        e1.printStackTrace();
                    }
                }));

                // Store the window
                gifWindows.put(sourceID, window);

                // Display window
                SwingUtilities.invokeLater(() -> window.setVisible(true));

            } catch (ConnectException ex) {
                // Inform user we failed to connect, then destroy the GifWindow as we have no use for it
                SwingUtilities.invokeLater(() -> {
                    window.dispose();
                    JOptionPane.showMessageDialog(
                            GifClient.this,
                            ex.getMessage(),
                            "Failed to connect", JOptionPane.ERROR_MESSAGE);
                });
            } finally {
                // Now re-enable the GUI
                SwingUtilities.invokeLater(() -> {
                    button_connect.setEnabled(true);
                    text_source.setEnabled(true);
                    combo_source.setEnabled(true);
                });
            }
        }
    }

    /**
     * Connects to an RMI server when the connect button is pressed
     */
    private class RMIConnect implements ActionListener {
        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // Get the text field details and trim them
                // Set default values to localhost:1099
                String server = text_server.getText().trim();
                server = (server.length() == 0 ? "localhost" : server);
                String intStr = text_port.getText().trim();
                intStr = (intStr.length() == 0 ? "1099" : intStr);

                // Do the connect
                Thread t = new ConnectThread(server, Integer.parseInt(intStr));
                t.start();

                // Disable GUI temporarily
                button_connect.setEnabled(false);
                button_disconnect.setEnabled(false);
                combo_source.setEnabled(false);
                text_server.setEnabled(false);
                text_port.setEnabled(false);
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    // Re-enable GUI
                    button_connect.setEnabled(true);
                    button_disconnect.setEnabled(false);
                    combo_source.setEnabled(false);
                    text_server.setEnabled(true);
                    text_port.setEnabled(true);
                    JOptionPane.showMessageDialog(
                            GifClient.this,
                            "Failed to connect to source.\n" + ex.getMessage(),
                            "Failed to connect.", JOptionPane.ERROR_MESSAGE);
                });
            }
        }
    }

    /**
     * Connects to a Source when the connect button is pressed
     */
    private class SourceConnect implements ActionListener {
        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            sourceConnect();
        }
    }
}