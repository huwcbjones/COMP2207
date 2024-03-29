package shared.interfaces;

import java.rmi.RemoteException;

/**
 * Notification Source Proxy Interface
 *
 * @author Huw Jones
 * @since 12/12/2016
 */
public interface INotificationSourceProxy extends INotificationSource {

    /**
     * Registers a source on this RMI Registry for sinks to access
     *
     * @param sourceID ID to register as
     * @param source   Source to register
     * @throws RemoteException
     */
    void register(String sourceID, INotificationSource source) throws RemoteException;

    /**
     * Unregisters a source on this RMI Registry for sinks to access
     *
     * @param sourceID ID to register as
     * @throws RemoteException
     */
    void unregister(String sourceID) throws RemoteException;
}
