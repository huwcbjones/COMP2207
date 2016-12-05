package notifications;

import interfaces.INotificationSink;
import interfaces.INotificationSource;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * A Notification Source
 * Send notifications to sinks
 *
 * @author Huw Jones
 * @since 02/12/2016
 */
public class NotificationSource implements INotificationSource {
    private ArrayList<INotificationSink> registeredSinks;

    public NotificationSource() {
        super();
        registeredSinks = new ArrayList<>();
    }

    /**
     * Registers a sink to receive notifications
     *
     * @param sink Sink to register
     * @return True if Sink was successfully registered
     * @throws RemoteException
     */
    @Override
    public boolean register(INotificationSink sink) throws RemoteException {
        if (isRegistered(sink)) {
            registeredSinks.add(sink);
            System.out.println("Registered sink");
        }
        return isRegistered(sink);
    }

    /**
     * Returns whether a sink is registered or not
     *
     * @param sink Sink to check
     * @return True if the sink is registered
     * @throws RemoteException
     */
    @Override
    public boolean isRegistered(INotificationSink sink) throws RemoteException {
        return registeredSinks.contains(sink);
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
        if(isRegistered(sink)){
            this.registeredSinks.remove(sink);
        }
        return isRegistered(sink);
    }

    protected void sendNotification(Notification notification) {
        registeredSinks.forEach(s -> {
            try {
                s.notify(notification);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }
}
