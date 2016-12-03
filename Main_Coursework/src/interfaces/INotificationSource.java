package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Notification Source Interface
 *
 * @author Huw Jones
 * @since 02/12/2016
 */
public interface INotificationSource extends Remote {

    /**
     * Registers a sink to receive notifications
     * @param sink Sink to register
     * @return True if Sink was successfully registered
     * @throws RemoteException
     */
    boolean register(INotificationSink sink) throws RemoteException;

    /**
     * Returns whether a sink is registered or not
     * @param sink Sink to check
     * @return True if the sink is registered
     * @throws RemoteException
     */
    boolean isRegistered(INotificationSink sink) throws RemoteException;
}
