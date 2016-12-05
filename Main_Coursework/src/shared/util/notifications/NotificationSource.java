package shared.util.notifications;

import shared.util.exceptions.RegisterFailException;
import shared.util.interfaces.INotificationSink;
import shared.util.interfaces.INotificationSource;
import shared.util.Log;
import shared.util.UUIDUtils;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A Notification Source
 * Send shared.util.notifications to sinks
 *
 * @author Huw Jones
 * @since 02/12/2016
 */
public class NotificationSource implements INotificationSource {
    /**
     * Map of sinks (UUID=>sink) that are registered to this source
     */
    private HashMap<UUID, INotificationSink> registeredSinks;

    /**
     * Data structure to keep shared.util.notifications that cannot be delivered to sinks.
     * 1 queue for each sink.
     */
    private HashMap<UUID, Queue<Notification>> notificationQueue;

    public NotificationSource() {
        super();
        registeredSinks = new HashMap<>();
        notificationQueue = new HashMap<>();
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

        if (!isRegistered(sink)) {
            try {
                UUID ID = getUUID();
                this.registeredSinks.put(ID, sink);
                this.notificationQueue.put(ID, new ConcurrentLinkedQueue<>());
                Log.Info("Registered sink: " + UUIDUtils.UUIDToBase64String(ID));

                return ID;
            } catch (Exception e) {
                Log.Error(e.toString());
            }
        }

        throw new RegisterFailException();
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
        }
        return isRegistered(sinkID);
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
}
