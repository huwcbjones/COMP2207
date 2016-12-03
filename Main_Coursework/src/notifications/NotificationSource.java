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

    public NotificationSource(){
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
        if(registeredSinks.contains(sink)){
            return true;
        }
        registeredSinks.add(sink);
        return registeredSinks.contains(sink);
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

    protected void sendNotification(Notification notification){
        registeredSinks.forEach(s -> {
            try {
                s.notify(notification);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }
}
