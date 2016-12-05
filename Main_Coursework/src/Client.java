import interfaces.INotificationSink;
import interfaces.INotificationSource;
import notifications.Notification;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Notification Client
 *
 * @author Huw Jones
 * @since 03/12/2016
 */
public class Client implements INotificationSink {

    public Client(String rmiServer, String source) {
        try {
            Registry registry = LocateRegistry.getRegistry(rmiServer);
            INotificationSource server = (INotificationSource) registry.lookup(source);
            INotificationSink sink = (INotificationSink) UnicastRemoteObject.exportObject(this, 0);
            if (!server.isRegistered(sink)) {
                server.register(sink);
            }

        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Client client = new Client(args[0], args[1]);
    }

    /**
     * Notifies a Sink
     *
     * @param notification Notification
     * @throws RemoteException
     */
    @Override
    public void notify(Notification notification) throws RemoteException {
        System.out.println(notification);
    }
}
