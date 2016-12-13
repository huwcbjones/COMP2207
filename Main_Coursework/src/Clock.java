import shared.notifications.Notification;
import shared.notifications.NotificationSource;
import shared.util.Log;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

/**
 * A Clock Server
 *
 * @author Huw Jones
 * @since 02/12/2016
 */
public class Clock extends NotificationSource {

    public Clock() throws RemoteException {
        super("Clock");
        try {
            //this.bind("HCBJ-MBP");
            this.bind();

            Log.Info("Clock starting...");
            this.runClock();
            Log.Info("Clock started!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        try {
            Clock clock = new Clock();
        } catch (RemoteException e) {
            Log.Fatal("Failed to start Clock.\n" + e.getMessage());
            e.printStackTrace();
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
