package shared.util;

import shared.exceptions.ConnectException;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * RMI Utils
 *
 * @author Huw Jones
 * @since 12/12/2016
 */
public class RMIUtils {

    public static Registry connect(String server, int port) throws ConnectException {
        try {
            Registry registry;
            Log.Info("Locating registry...");
            registry = LocateRegistry.getRegistry(server, port);
            registry.list();
            Log.Info("Registry found!");
            return registry;
        } catch (RemoteException e) {
            throw new ConnectException(String.format("Failed to connect to RMI registry @%s:%d", server, port), e);
        }
    }
}
