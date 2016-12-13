package sourceproxy;

import javafx.util.Pair;
import server.NotificationSource;
import shared.Notification;
import shared.exceptions.RegisterFailException;
import shared.interfaces.INotificationSink;
import shared.interfaces.INotificationSource;
import shared.interfaces.INotificationSourceProxy;
import shared.util.Log;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Maintains registry of sources
 * and binds the source stubs
 *
 * @author Huw Jones
 * @since 12/12/2016
 */
public class SourceProxy extends NotificationSource implements INotificationSourceProxy {

    private HashMap<String, INotificationSource> sourceMap;

    public SourceProxy() throws RemoteException {
        super("SourceProxy");
        sourceMap = new HashMap<>();
        bind();

        // Try to register all previously registered Sources
        Arrays.stream(registry.list())
                .filter(e -> !e.equals("SourceProxy"))
                .forEach(e -> {
                    try {
                        register(e, (INotificationSource) registry.lookup(e));
                    } catch (RemoteException | NotBoundException ex) {
                        Log.Warn(String.format("Failed to register '%s': %s", e, ex.getMessage()));
                    }
                });
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
        boolean status = super.register(sinkID, sink);
        if (isRegistered(sinkID)) {
            sink.notify(new Notification<>(sourceID, getSourceList()));
        }
        return status;
    }


    /**
     * Registers a source on this RMI Registry for sinks to access
     *
     * @param sourceID ID to register as
     * @param source   Source to register
     * @throws RemoteException
     */
    public void register(String sourceID, INotificationSource source) throws RemoteException {
        // Store source
        sourceMap.put(sourceID, source);

        // Export object (the received object should have already been exported, but better make sure)
        INotificationSource sourceStub = (INotificationSource) UnicastRemoteObject.exportObject(source, 0);

        // Bind the source, then tell all sinks there is a new source
        registry.rebind(sourceID, sourceStub);
        Log.Info(String.format("Registered Source: %s", sourceID));
        updateSinks();
    }

    /**
     * Get a list of the registered sources
     *
     * @return List of registered sources
     */
    private ArrayList<Pair<String, INotificationSource>> getSourceList() {
        return new ArrayList<>(sourceMap.entrySet().stream()
                .map(e -> new Pair<>(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));
    }

    /**
     * Updates the sinks with the list of sources
     */
    private void updateSinks() {
        // Send notification with list of sources to sinks
        sendNotification(new Notification<>(sourceID, getSourceList()));
    }

    /**
     * Unregisters a source on this RMI Registry for sinks to access
     *
     * @param sourceID ID to register as
     * @throws RemoteException
     */
    public void unregister(String sourceID) throws RemoteException {
        // Store source
        sourceMap.remove(sourceID);
        try {
            registry.unbind(sourceID);
        } catch (NotBoundException e) {
            Log.Warn(String.format("Failed to unbind %s: %s", sourceID, e.getMessage()));
        }

        updateSinks();
    }
}
