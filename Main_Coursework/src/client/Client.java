package client;

import javafx.util.Pair;
import shared.components.HintTextFieldUI;
import shared.exceptions.ConnectException;
import shared.interfaces.INotificationSource;
import shared.notifications.Notification;
import shared.notifications.NotificationSink;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

/**
 * Client GUI Class
 *
 * @author Huw Jones
 * @since 06/12/2016
 */
public class Client extends JFrame {

    private static Sink sink = null;
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

    public Client() {
        super("RMI Client");
        try {
            Client.sink = new Sink(Config.getClientID());
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

        this.setVisible(true);
    }

    public static Sink getSink() {
        return sink;
    }

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

    private void initEventListeners() {
        this.button_connect.addActionListener(rmiConnectListener);
        this.button_disconnect.addActionListener(e -> {
            gifWindows.entrySet().forEach(w -> {w.getValue().dispose(); gifWindows.remove(w.getKey());});
            if (sink.isConnectedSource()) {
                sink.disconnectAllSource();
            }
            if (sink.isConnectedRMIProxy()) {
                sink.disconnectRMIProxy();
            }
            button_disconnect.setEnabled(false);
            text_source.setEnabled(false);
            combo_source.setEnabled(false);
            combo_source.removeAllItems();
            text_server.setEnabled(true);
            text_port.setEnabled(true);

            button_connect.removeActionListener(sourceConnectListener);
            button_connect.addActionListener(rmiConnectListener);

            this.setTitle("RMI Client");
        });
    }

    private class ConnectThread extends Thread {
        private String rmiServer;
        private int port;

        public ConnectThread(String rmiServer, int port) {
            super("ConnectThread");
            this.rmiServer = rmiServer;
            this.port = port;
        }

        @Override
        public void run() {
            // Try to connect to SourceProxy
            try {
                sink.connectRMIProxy(rmiServer, port);
                SwingUtilities.invokeLater(() -> {
                    combo_source.setVisible(true);
                    text_source.setVisible(false);
                });
            } catch (ConnectException ex) {
                Log.Error(ex.getMessage() + " " + ex.getCause().getMessage());
            }

            // If not, just connect to the registry and let the user type the source they want to connect to
            if (!sink.isConnectedRMIProxy()) {
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
                                    Client.this,
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
            }

            Log.Info("Connected to  RMI registry!");

            // Can't use SourceProxy to get a list of sources, so enable manual bind
            SwingUtilities.invokeLater(() -> {

                Client.this.setTitle(String.format("RMI Client - %s:%s", rmiServer, port));
                button_connect.removeActionListener(rmiConnectListener);
                button_connect.addActionListener(sourceConnectListener);

                button_connect.setEnabled(true);
                button_disconnect.setEnabled(true);
                combo_source.setEnabled(true);
                text_source.setEnabled(true);
                text_server.setEnabled(false);
                text_port.setEnabled(false);
            });
        }
    }

    private class SourceConnectThread extends Thread {

        private String sourceID;

        public SourceConnectThread(String sourceID) {
            super("SourceThread");
            this.sourceID = sourceID;
        }

        @Override
        public void run() {
            GifWindow window = new GifWindow(sourceID);
            SwingUtilities.invokeLater(() -> {
                button_connect.setEnabled(false);
                text_source.setEnabled(false);
                combo_source.setEnabled(false);
            });

            try {


                sink.connectSource(sourceID, n -> SwingUtilities.invokeLater(() -> {
                    try {
                        window.displayImage(ImageUtils.bytesToImage((byte[]) n.getData()));
                    } catch (IOException e1) {
                        Log.Error("Failed to convert bytes to image: " + e1.getMessage());
                        e1.printStackTrace();
                    }
                }));
                gifWindows.put(sourceID, window);
                SwingUtilities.invokeLater(() -> window.setVisible(true));
            } catch (ConnectException ex) {
                SwingUtilities.invokeLater(() -> {
                    window.dispose();
                    JOptionPane.showMessageDialog(
                            Client.this,
                            ex.getMessage(),
                            "Failed to connect", JOptionPane.ERROR_MESSAGE);
                });
            } finally {
                SwingUtilities.invokeLater(() -> {
                    button_connect.setEnabled(true);
                    text_source.setEnabled(true);
                    combo_source.setEnabled(true);
                });
            }
        }
    }

    @SuppressWarnings("unchecked")
    private class Sink extends NotificationSink {

        public Sink(UUID clientID) throws RemoteException {
            super(clientID);
        }

        /**
         * Notifies a Sink
         *
         * @param notification Notification
         * @throws RemoteException
         */
        @Override
        public void notify(Notification notification) throws RemoteException {
            super.notify(notification);
            if (notification.getSource().equals("SourceProxy")) {
                ArrayList<Pair<String, INotificationSource>> data = (ArrayList<Pair<String, INotificationSource>>) notification.getData();
                SwingUtilities.invokeLater(() -> {
                    combo_source.removeAllItems();
                    data.stream().map(Pair::getKey).forEach(combo_source::addItem);
                });
            }
        }
    }

    private class RMIConnect implements ActionListener {
        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String server = text_server.getText().trim();
                server = (server.length() == 0 ? "localhost" : server);
                String intStr = text_port.getText().trim();
                intStr = (intStr.length() == 0 ? "1099" : intStr);

                Thread t = new ConnectThread(server, Integer.parseInt(intStr));
                t.start();
                button_connect.setEnabled(false);
                button_disconnect.setEnabled(false);
                combo_source.setEnabled(false);
                text_server.setEnabled(false);
                text_port.setEnabled(false);
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    button_connect.setEnabled(true);
                    button_disconnect.setEnabled(false);
                    combo_source.setEnabled(false);
                    text_server.setEnabled(true);
                    text_port.setEnabled(true);
                    JOptionPane.showMessageDialog(
                            Client.this,
                            "Failed to connect to source.\n" + ex.getMessage(),
                            "Failed to connect.", JOptionPane.ERROR_MESSAGE);
                });
            }
        }
    }

    private class SourceConnect implements ActionListener {
        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String sourceID = "";
                if (combo_source.isVisible()) {
                    sourceID = (String) combo_source.getSelectedItem();
                } else if (text_source.isVisible()) {
                    sourceID = text_source.getText();
                }
                if (sourceID.length() == 0) {
                    JOptionPane.showMessageDialog(Client.this,
                            "Please enter/select the ID of the source you'd like to connect to.",
                            "Failed to connect",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if(gifWindows.containsKey(sourceID)){
                    GifWindow window = gifWindows.get(sourceID);
                    window.setVisible(true);
                    window.toFront();
                    window.repaint();
                } else {
                    SourceConnectThread t = new SourceConnectThread(sourceID);
                    t.start();
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        Client.this,
                        ex.getMessage(),
                        "Failed to connect", JOptionPane.ERROR_MESSAGE));
            }
        }
    }
}