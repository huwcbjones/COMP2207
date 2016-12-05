import shared.util.interfaces.INotificationSource;
import shared.util.notifications.Notification;
import shared.util.notifications.NotificationSource;
import shared.util.Log;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.Date;

/**
 * A Clock Server
 *
 * @author Huw Jones
 * @since 02/12/2016
 */
public class Clock extends NotificationSource {

    public Clock() {
        super();
    }

    public static void main(String args[]) {
        try {
            String name = "Clock";
            Clock clock = new Clock();
            INotificationSource clockStub = (INotificationSource) UnicastRemoteObject.exportObject(clock, 0);

            Log.Info("Locating registry...");
            Registry registry = LocateRegistry.getRegistry();

            Log.Info("Binding clock stub...");
            registry.rebind(name, clockStub);

            Log.Info("Clock stub bound!");

            Log.Info("Starting clock...");
            clock.runClock();
            Log.Info("Clock started!");
        } catch (Exception ex) {
            System.err.println("Clock exception: ");
            ex.printStackTrace();
        }
    }

    public void runClock() {
        Thread t = new Thread(new ClockRunner(), "Clock");
        t.start();
    }

    private class ClockRunner implements Runnable {
        @Override
        public void run() {
            try {
                Date time;
                while (true) {
                    time = Calendar.getInstance().getTime();
                    Log.Info("The time is now: " + time);
                    sendNotification(new Notification<>(time));
                    try {
                        Thread.sleep(999);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                System.err.println("Clock exception: ");
                ex.printStackTrace();
            }
        }
    }
}
