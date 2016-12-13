package client;

import javafx.util.Pair;
import shared.components.HintTextFieldUI;
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
import java.util.UUID;

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
    JComboBox<String> combo_source;
    JLabel label_server;
    JTextField text_server;
    JLabel label_port;
    JTextField text_port;

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

        Runtime.getRuntime().addShutdownHook(new ShutdownHandler());

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
            if (sink.isConnectedSource()) {
                sink.disconnectAllSource();
            }
            if (sink.isConnectedRMI()) {
                sink.disconnectRMI();
            }
            button_disconnect.setEnabled(false);
            combo_source.setEnabled(false);
            combo_source.removeAllItems();
            text_server.setEnabled(true);
            text_port.setEnabled(true);

            button_connect.removeActionListener(sourceConnectListener);
            button_connect.addActionListener(rmiConnectListener);

            this.setTitle("RMI Client");
        });
    }

    private void connect_disconnect(boolean state) {
        button_disconnect.setEnabled(true);
        combo_source.setEnabled(true);
        text_server.setEnabled(false);
        text_port.setEnabled(false);

        button_connect.removeActionListener(rmiConnectListener);
        button_connect.addActionListener(sourceConnectListener);
    }

    private class connectThread extends Thread {
        private String rmiServer;
        private int port;

        public connectThread(String rmiServer, int port) {
            super("ConnectionThread");
            this.rmiServer = rmiServer;
            this.port = port;
        }

        @Override
        public void run() {
            try {
                sink.connectRMI(rmiServer, port);
                SwingUtilities.invokeLater(() -> Client.this.setTitle(String.format("RMI Client - %s:%s", rmiServer, port)));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        Client.this,
                        ex.getMessage(),
                        "Failed to connect.", JOptionPane.ERROR_MESSAGE));
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

                Thread t = new connectThread(server, Integer.parseInt(intStr));
                t.start();
                button_disconnect.setEnabled(true);
                combo_source.setEnabled(true);
                text_server.setEnabled(false);
                text_port.setEnabled(false);

                button_connect.removeActionListener(rmiConnectListener);
                button_connect.addActionListener(sourceConnectListener);
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        Client.this,
                        "Failed to connect to source.\n" + ex.getMessage(),
                        "Failed to connect.", JOptionPane.ERROR_MESSAGE));
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
                String sourceID = (String) combo_source.getSelectedItem();
                GifWindow window = new GifWindow(sourceID);
                sink.connectSource(sourceID, n -> SwingUtilities.invokeLater(() -> {
                    try {

                        window.displayImage(ImageUtils.bytesToImage((byte[]) n.getData()));
                    } catch (IOException e1) {
                        Log.Error("Failed to convert bytes to image: " + e1.getMessage());
                        e1.printStackTrace();
                    }
                }));
                window.setVisible(true);
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        Client.this,
                        ex.getMessage(),
                        "Failed to connect.", JOptionPane.ERROR_MESSAGE));
            }
        }
    }

    private class ShutdownHandler extends Thread {

        public ShutdownHandler() {
            super("ShutdownHandler");
        }

        @Override
        public void run() {
            if (sink == null) {
                return;
            }
            if (sink.isConnectedSource()) {
                sink.disconnectAllSource();
            }
            if (sink.isConnectedRMI()) {
                sink.disconnectRMI();
            }
        }
    }
}