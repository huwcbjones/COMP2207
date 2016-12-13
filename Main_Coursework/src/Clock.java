import shared.interfaces.INotificationSource;
import shared.notifications.Notification;
import shared.notifications.NotificationSource;
import shared.util.Log;

import java.net.BindException;
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
        super("Clock");
        try {
            this.bind("localhost");

            Log.Info("Clock starting...");
            this.runClock();
            Log.Info("Clock started!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        Clock clock = new Clock();
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
                    Log.Info(String.format("The time is now: %s\r", time), false);
                    sendNotification(new Notification<>("Clock", time));
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
