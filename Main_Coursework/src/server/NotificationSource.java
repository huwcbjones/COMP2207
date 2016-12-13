package server;

import shared.Notification;
import shared.exceptions.ConnectException;
import shared.exceptions.RegisterFailException;
import shared.interfaces.INotificationSink;
import shared.interfaces.INotificationSource;
import shared.interfaces.INotificationSourceProxy;
import shared.util.Log;
import shared.util.RMIUtils;
import shared.util.UUIDUtils;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A Notification Source
 * Send shared.util.notifications to sinks
 *
 * @author Huw Jones
 * @since 02/12/2016
 */
public abstract class NotificationSource extends UnicastRemoteObject implements INotificationSource {
    /**
     * ID of the source (used to bind to the registry server)
     */
    protected final String sourceID;
    /**
     * RMI Registry Server
     */
    protected Registry registry;

    private INotificationSourceProxy proxy;

    private String registryServer;
    private int registryPort;

    /**
     * Map of sinks (UUID=>sink) that are registered to this source
     */
    private ConcurrentHashMap<UUID, INotificationSink> registeredSinks;
    /**
     * Data structure to keep shared.util.notifications that cannot be delivered to sinks.
     * 1 queue for each sink.
     */
    private HashMap<UUID, ConcurrentLinkedQueue<Notification>> notificationQueue;

    public NotificationSource() throws RemoteException {
        this(Config.getServerID());
    }

    public NotificationSource(String sourceID) throws RemoteException {
        super();
        Runtime.getRuntime().addShutdownHook(new ShutdownHandler());
        this.sourceID = sourceID;
        this.registeredSinks = new ConcurrentHashMap<>();
        this.notificationQueue = new HashMap<>();
    }

    /**
     * Binds this source to the RMI Registry server using the Proxy Source,
     * then falls back to RMI binding if the server is the localhost.
     * Otherwise fails to bind.
     *
     * @throws ConnectException Thrown if failed to connect to the registry server
     */
    public void bind() throws ConnectException {
        String server = (Config.getRmiServer() == null) ? "localhost" : Config.getRmiServer();
        int port = (Config.getRmiPort() == null) ? 1099 : Config.getRmiPort();
        bind(server, port);
    }

    /**
     * Binds this source to the RMI Registry server using the Proxy Source,
     * then falls back to RMI binding if the server is the localhost.
     * Otherwise fails to bind.
     *
     * @param registryServer Server hostname
     * @param registryPort   Server port
     * @throws ConnectException Thrown if failed to connect to the registry server
     */
    public void bind(String registryServer, int registryPort) throws ConnectException {
        this.proxy = null;

        // Connect to the registry
        registry = RMIUtils.connect(registryServer, registryPort);

        // We are now connected to the registry server
        this.registryServer = registryServer;
        this.registryPort = registryPort;

        Log.Info("Registering " + this.sourceID + "...");

        try {
            // Try to register using the proxy
            INotificationSourceProxy proxy = (INotificationSourceProxy) registry.lookup("sourceproxy");
            proxy.register(sourceID, this);
            this.proxy = proxy;
            Log.Info("Registered " + this.sourceID + "!");

        } catch (RemoteException | NotBoundException ex) {
            // If the proxy is not available and the rmi server is running locally, bind straight to the local registry
            // Clients will still be able to access the source through specifying the SourceID, but they won't get notified when
            // Sources register/unregister with the proxy server.
            if (!registryServer.equals("localhost")) {
                throw new ConnectException("Failed to register source (using SourceProxy).", ex);
            }

            Log.Warn(String.format("Failed to register %s (using SourceProxy)... attempting straight bind", sourceID));
            try {
                // Bind using the localhost registry
                registry.rebind(sourceID, this);
                Log.Info("Registered " + this.sourceID + "!");
            } catch (RemoteException e) {
                throw new ConnectException("Failed to register source.", e);
            }
        }
    }

    /**
     * Binds this source to the RMI Registry server using the Proxy Source,
     * then falls back to RMI binding if the server is the localhost.
     * Otherwise fails to bind.
     *
     * @param registryServer Server hostname
     * @throws ConnectException Thrown if failed to connect to the registry server
     */
    public void bind(String registryServer) throws ConnectException {
        bind(registryServer, 1099);
    }

    /**
     * Binds this source to the RMI Registry server using the Proxy Source,
     * then falls back to RMI binding if the server is the localhost.
     * Otherwise fails to bind.
     *
     * @param registryPort   Server port
     * @throws ConnectException Thrown if failed to connect to the registry server
     */
    public void bind(int registryPort) throws ConnectException {
        bind("localhost", registryPort);
    }

    /**
     * Registers a sink to receive shared.util.notifications
     *
     * @param sink Sink to register
     * @return True if Sink was successfully registered
     * @throws RemoteException       If the method could not be called
     * @throws RegisterFailException If the sink failed to register
     */
    @Override
    public UUID register(INotificationSink sink) throws RemoteException, RegisterFailException {
        UUID id = getUUID();
        register(id, sink);
        return id;
    }

    /**
     * Registers a sink to receive shared.util.notifications
     *
     * @param sinkID ID of sink
     * @param sink   Sink to register
     * @return True if Sink was successfully registered
     * @throws RemoteException
     */
    @Override
    public boolean register(UUID sinkID, INotificationSink sink) throws RemoteException, RegisterFailException {
        if (!isRegistered(sinkID)) {
            try {
                // If the sink doesn't have a UUID, create one
                if (sinkID == null) sinkID = getUUID();

                // Store the sink
                this.registeredSinks.put(sinkID, sink);

                // Create the queue for storing messages that fail to send
                this.notificationQueue.put(sinkID, new ConcurrentLinkedQueue<>());
                Log.Info("Sink registered: " + UUIDUtils.UUIDToBase64String(sinkID));
                return true;
            } catch (Exception e) {
                Log.Error(e.toString());
            }
        } else {
            this.registeredSinks.put(sinkID, sink);
            Log.Info("Sink reregistered: " + UUIDUtils.UUIDToBase64String(sinkID));
            sendQueue(sinkID);
            return true;
        }

        throw new RegisterFailException();
    }

    /**
     * Returns whether a sink is registered or not
     *
     * @param sink Sink to check
     * @return True if the sink is registered
     * @throws RemoteException
     */
    public boolean isRegistered(INotificationSink sink) throws RemoteException {
        return registeredSinks.containsValue(sink);
    }

    /**
     * Unregisters a sink from the source
     *
     * @param sink Sink to unregister
     * @return True if sink was unregistered
     * @throws RemoteException
     */
    @Override
    public boolean unRegister(INotificationSink sink) throws RemoteException {
        if (isRegistered(sink)) {
            // Get the sink UUID
            UUID sinkID = this.registeredSinks
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue().equals(sink))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            return sinkID == null || unRegister(sinkID);
        }
        return isRegistered(sink);
    }

    /**
     * Unregisters a sink from the source by SinkID
     *
     * @param sinkID SinkID to unregister
     * @return True if sink was unregistered
     * @throws RemoteException
     */
    public boolean unRegister(UUID sinkID) throws RemoteException {
        if (isRegistered(sinkID)) {
            this.registeredSinks.remove(sinkID);
            this.notificationQueue.remove(sinkID);
            Log.Info("Sink unregistered: " + UUIDUtils.UUIDToBase64String(sinkID));
        }
        return isRegistered(sinkID);
    }

    /**
     * Gets an unused UUID
     *
     * @return UUID
     */
    private UUID getUUID() {
        UUID uuid;
        do {
            uuid = UUID.randomUUID();
        } while (_isRegistered(uuid));
        return uuid;
    }

    /**
     * Returns whether a sink is registered to an ID or not
     *
     * @param sinkID SinkID to check
     * @return True if the sink is registered
     */
    private boolean _isRegistered(UUID sinkID) {
        return registeredSinks.containsKey(sinkID);
    }

    /**
     * Returns whether a sink is registered to an ID or not
     *
     * @param sinkID SinkID to check
     * @return True if the sink is registered
     * @throws RemoteException
     */
    public boolean isRegistered(UUID sinkID) throws RemoteException {
        return _isRegistered(sinkID);
    }

    /**
     * Sends the queue of missed notifications to the sink
     * @param sinkID
     */
    private void sendQueue(UUID sinkID) {
        // Get the sink, and the sink's queue
        INotificationSink sink = this.registeredSinks.get(sinkID);
        ConcurrentLinkedQueue<Notification> queue = this.notificationQueue.get(sinkID);

        // Send all the notifications in the queue
        // We are peeking to get the notification, then after we know it's been sent, removing it
        Notification notification;
        while ((notification = queue.peek()) != null) {
            try {
                sink.notify(notification);
                queue.remove();
            } catch (RemoteException e) {
                Log.Warn("Failed to send message to: " + UUIDUtils.UUIDToBase64String(sinkID));
                break;
            }
        }
    }

    /**
     * Sends a notification to all registered sinks, or if sending failed,
     * queues it to be sent at a later date.
     *
     * @param notification Notification to send
     */
    protected void sendNotification(Notification notification) {
        registeredSinks.entrySet().forEach(map -> {
            UUID id = map.getKey();
            INotificationSink sink = map.getValue();
            try {
                Log.Trace("Sending message to: " + UUIDUtils.UUIDToBase64String(id));
                sink.notify(notification);

                // If we successfully sent a notification, check if the queue is empty, if not, empty it (as we can now send notifications)
                if(notificationQueue.get(id).size() != 0){
                    sendQueue(id);
                }
            } catch (RemoteException e) {
                Log.Warn(String.format("Failed to send message to: %s. Queuing for delivery later. ", UUIDUtils.UUIDToBase64String(id)));
                this.queueNotification(id, notification);
            }
        });
    }

    /**
     * Queues a notification for delivery at a later date if the notification could not be delivered
     *
     * @param sinkID       Sink that could not be reached
     * @param notification Notification that could not be sent
     */
    private void queueNotification(UUID sinkID, Notification notification) {
        this.notificationQueue.get(sinkID).add(notification);
    }

    /**
     * Unbinds the source from the registry
     */
    private class ShutdownHandler extends Thread {

        public ShutdownHandler() {
            super("ShutdownHandler");
        }

        @Override
        public void run() {
            if (registry == null) {
                return;
            }

            if (proxy != null) {
                Log.Info("Unregistering " + sourceID + "...");
                try {
                    proxy.unregister(sourceID);
                    Log.Info("Unregistered " + sourceID);
                } catch (RemoteException e) {
                    Log.Warn(String.format("Failed to unregister %s: %s", sourceID, e.getMessage()));
                    e.printStackTrace();
                }
                return;
            }

            if (!registryServer.equals("localhost")) {
                return;
            }

            try {
                if (Arrays.stream(registry.list()).anyMatch(e -> e.equals(sourceID))) {
                    Log.Info("Unbinding " + sourceID + "...");
                    registry.unbind(sourceID);
                    Log.Info("Unbound " + sourceID);
                }
            } catch (NotBoundException shouldNotHappen) {
            } catch (RemoteException ex) {
                Log.Warn("Failed to unbind " + sourceID);
            }
        }
    }
}
