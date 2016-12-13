package shared.interfaces;

import shared.Notification;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Notification Sink Interface
 *
 * @author Huw Jones
 * @since 02/12/2016
 */
public interface INotificationSink extends Remote {

    /**
     * Notifies a Sink
     *
     * @param notification Notification
     * @throws RemoteException
     */
    void notify(Notification notification) throws RemoteException;
}
