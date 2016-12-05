package shared.util.interfaces;

import shared.util.exceptions.RegisterFailException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

/**
 * Notification Source Interface
 *
 * @author Huw Jones
 * @since 02/12/2016
 */
public interface INotificationSource extends Remote {

    /**
     * Registers a sink to receive shared.util.notifications
     *
     * @param sink Sink to register
     * @return True if Sink was successfully registered
     * @throws RemoteException
     */
    UUID register(INotificationSink sink) throws RemoteException, RegisterFailException;

    /**
     * Returns whether a sink is registered or not
     *
     * @param sink Sink to check
     * @return True if the sink is registered
     * @throws RemoteException
     */
    boolean isRegistered(INotificationSink sink) throws RemoteException;

    /**
     * Unregisters a sink from the source
     * @param sink Sink to unregister
     * @return True if sink was unregistered
     * @throws RemoteException
     */
    boolean unRegister(INotificationSink sink) throws RemoteException;

    /**
     * Unregisters a sink from the source
     * @param sinkID Sink to unregister
     * @return True if sink was unregistered
     * @throws RemoteException
     */
    boolean unRegister(UUID sinkID) throws RemoteException;
}
