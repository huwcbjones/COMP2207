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

    /**
     * Connects to an RMI server server/port and returns the registry if the connection succeeded.
     *
     * @param server Server hostname to connect to
     * @param port Server port
     * @return The Registry on the server:port
     * @throws ConnectException Thrown if the connection failed
     */
    public static Registry connect(String server, int port) throws ConnectException {
        try {
            Registry registry;
            Log.Info("Locating registry...");
            registry = LocateRegistry.getRegistry(server, port, new CustomRMISocketFactory());

            // By calling list, it either succeeds (registry connected), or throws a RemoteException (connection failed)
            registry.list();

            Log.Info("Registry found!");
            return registry;
        } catch (RemoteException e) {
            throw new ConnectException(String.format("Failed to connect to RMI registry @%s:%d", server, port), e);
        }
    }
}
