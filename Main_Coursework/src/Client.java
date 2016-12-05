import shared.util.interfaces.INotificationSink;
import shared.util.interfaces.INotificationSource;
import shared.util.notifications.Notification;
import shared.util.Log;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

/**
 * Notification Client
 *
 * @author Huw Jones
 * @since 03/12/2016
 */
public class Client implements INotificationSink {

    private INotificationSource server;
    private Registry registry;
    private UUID clientID = null;

    public Client(String rmiServer, String source) {
        try {
            this.registry = LocateRegistry.getRegistry(rmiServer);
            this.server = (INotificationSource) this.registry.lookup(source);

            INotificationSink sink = (INotificationSink) UnicastRemoteObject.exportObject(this, 0);
            if (!server.isRegistered(sink)) {
                this.clientID = server.register(sink);
            }

        } catch (Exception e) {
            Log.Fatal("Client exception:");
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
        Log.Info(notification.toString());
    }

    private class ShutdownThread extends Thread {

        public ShutdownThread() {
            super("ShutdownThread");
        }

        @Override
        public void run() {
            if(clientID != null) try {
                server.unRegister(clientID);
            } catch (RemoteException e) {

            }
        }
    }
}
