import interfaces.INotificationSource;
import notifications.Notification;
import notifications.NotificationSource;

import java.rmi.Naming;
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

    public Clock(){
        super();
    }

    private class ClockRunner implements Runnable {
        @Override
        public void run() {
            try {
                Date time;
                while (true) {
                    time = Calendar.getInstance().getTime();
                    System.out.println("The time is now: " + time);
                    sendNotification(new Notification<>(time));
                    try {
                        Thread.sleep(999);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception ex){
                System.err.println("Clock exception: ");
                ex.printStackTrace();
            }
        }
    }

    public void runClock(){
        Thread t = new Thread(new ClockRunner(), "Clock");
        t.run();
    }

    public static void main(String args[]){
        try {
            String name = "Clock";
            Clock clock = new Clock();
            INotificationSource clockStub = (INotificationSource) UnicastRemoteObject.exportObject(clock, 0);

            Registry registry = LocateRegistry.getRegistry();
            Naming.rebind(name, clockStub);
            //registry.rebind(name, clockStub);
            System.out.println("Clock bound.");

            System.out.println("Starting clock...");
            clock.runClock();
        } catch (Exception ex){
            System.err.println("Clock exception: ");
            ex.printStackTrace();
        }
    }
}
