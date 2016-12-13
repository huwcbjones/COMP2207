package shared.notifications;

import javafx.util.Pair;
import shared.exceptions.ConnectException;
import shared.exceptions.RegisterFailException;
import shared.interfaces.INotificationSink;
import shared.interfaces.INotificationSource;
import shared.interfaces.ISinkCallbackHandler;
import shared.util.Log;
import shared.util.RMIUtils;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Notification Sink
 * Receives shared.util.notifications from sources
 *
 * @author Huw Jones
 * @since 12/12/2016
 */
public abstract class NotificationSink extends UnicastRemoteObject implements INotificationSink {

    public final UUID sinkID;
    protected Registry registry = null;
    protected INotificationSource sourceProxy = null;
    private HashMap<String, ISinkCallbackHandler> callbackRegistry;

    private ConcurrentHashMap<String, INotificationSource> sources;

    public NotificationSink() throws RemoteException {
        this(UUID.randomUUID());
    }

    public NotificationSink(UUID clientID) throws RemoteException {
        super();
        this.sinkID = clientID;
        sources = new ConcurrentHashMap<>();
        callbackRegistry = new HashMap<>();
    }

    /**
     * Connect to an RMI server running on server:port.
     *
     * @param server Server to connect to
     * @param port   Port to connect to
     * @throws ConnectException Thrown if failed to connect
     */
    @SuppressWarnings("unchecked")
    public void connectRMI(String server, int port) throws ConnectException {
        registry = RMIUtils.connect(server, port);

        Log.Info("Registering with SourceProxy...");

        try {
            sourceProxy = (INotificationSource) registry.lookup("SourceProxy");
            callbackRegistry.put("SourceProxy", (e) -> Log.Info("Received list of sources (" + ((List<Pair<String,INotificationSource>>)e.getData()).size() + ")"));
            sourceProxy.register(sinkID, this);
        } catch (RemoteException | NotBoundException | RegisterFailException ex) {
            throw new ConnectException("Failed to register with SourceProxy.", ex);
        }
    }

    /**
     * Connects this sink to a source
     *
     * @param sourceID Source to connect to
     * @throws ConnectException
     */
    public void connectSource(String sourceID) throws ConnectException {
        connectSource(sourceID, null);
    }

    /**
     * Connects this sink to a source
     *
     * @param sourceID Source to connect to
     * @param handler  A runnable to be run when the sink is notified of notifications
     * @throws ConnectException
     */
    public void connectSource(String sourceID, ISinkCallbackHandler handler) throws ConnectException {
        if (registry == null || sourceProxy == null) {
            throw new ConnectException("Not connected to remote server.");
        }

        try {
            INotificationSource source = (INotificationSource) registry.lookup(sourceID);
            source.register(sinkID, this);
            Log.Info(String.format("Registered with %s!", sourceID));

            this.sources.put(sourceID, source);
            if (handler != null) this.callbackRegistry.put(sourceID, handler);
        } catch (NotBoundException ex) {
            throw new ConnectException(String.format("Failed to register with %s - source could not be found.", sourceID), ex);
        } catch (RemoteException | RegisterFailException ex) {
            throw new ConnectException(String.format("Failed to register with %s.", sourceID), ex);
        }
    }

    public void disconnectSource(String sourceID) {
        Log.Info(String.format("Disconnecting from %s...", sourceID));
        if (!this.sources.containsKey(sourceID)) {
            return;
        }
        INotificationSource source = this.sources.get(sourceID);
        try {
            source.unRegister(this.sinkID);
            sources.remove(sourceID);
            callbackRegistry.remove(sourceID);

            Log.Info(String.format("Disconnected from %s.", source));
        } catch (RemoteException e) {
            Log.Error(String.format("Failed to unregister from %s: %s", sourceID, e.getMessage()));
        }
    }

    public void disconnectAllSource() {
        Log.Info("Disconnecting from all sources...");
        sources.entrySet().forEach(e -> disconnectSource(e.getKey()));
        Log.Info("Disconnected from all sources!");
    }

    public void disconnectRMI() {
        if (sourceProxy == null) {
            return;
        }
        try {
            Log.Info("Disconnecting from RMI registry (Source Proxy)...");
            sourceProxy.unRegister(this.sinkID);
            Log.Info("Disconnected from RMI registry (Source Proxy)!");
        } catch (RemoteException e) {
            Log.Error("Failed to unregister from SourceProxy: " + e.getMessage());
        }
    }

    /**
     * Gets whether or not the sink is connected to an RMI (Proxy) server
     *
     * @return True if connected
     */
    public boolean isConnectedRMI() {
        return !(registry == null || sourceProxy == null);
    }

    /**
     * Gets whether or not the sink is connected to any sources
     *
     * @return True if the sink is connected to a source
     */
    public boolean isConnectedSource() {
        return sources.size() != 0;
    }

    /**
     * Notifies a Sink
     *
     * @param notification Notification
     * @throws RemoteException
     */
    @Override
    public void notify(Notification notification) throws RemoteException {
        if (!callbackRegistry.containsKey(notification.getSource())) {
            Log.Warn(String.format("No handler registered for %s. Logging to console.", notification.getSource()));
            Log.Info(notification.toString());
            return;
        }
        ISinkCallbackHandler callback = callbackRegistry.get(notification.getSource());
        callback.notify(notification);
    }
}
