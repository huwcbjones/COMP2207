package shared.util.notifications;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Notification Class
 *
 * @author Huw Jones
 * @since 21/11/2016
 */
public class Notification<T extends Serializable> implements Serializable {
    private static long serialUID = -783483475;

    private final T data;
    private final Timestamp time;
    private final PRIORITY priority;

    public Notification(T data) {
        this.priority = PRIORITY.Normal;
        this.data = data;
        this.time = new Timestamp(Calendar.getInstance().getTime().getTime());
    }

    public Notification(PRIORITY priority, T data) {
        this.priority = priority;
        this.data = data;
        this.time = new Timestamp(Calendar.getInstance().getTime().getTime());
    }

    @Override
    public String toString() {
        return String.format("notification: {\n  date: %s,\n  priority: %s,\n  data: %s\n}", this.getTime(), this.getPriority(), this.getData());
    }

    public Timestamp getTime() {
        return time;
    }

    public PRIORITY getPriority() {
        return priority;
    }

    public T getData() {
        return data;
    }

    public enum PRIORITY {
        Lowest,
        Low,
        Normal,
        High,
        Highest
    }
}
