package shared;

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

    private final String source;
    private final T data;
    private final Timestamp time;
    private final PRIORITY priority;

    public Notification(String source, T data) {
        this.source = source;
        this.priority = PRIORITY.Normal;
        this.data = data;
        this.time = new Timestamp(Calendar.getInstance().getTime().getTime());
    }

    public Notification(String source, PRIORITY priority, T data) {
        this.source = source;
        this.priority = priority;
        this.data = data;
        this.time = new Timestamp(Calendar.getInstance().getTime().getTime());
    }

    /**
     * Get the SourceID of the notification
     * @return SourceID
     */
    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return String.format("notification: {\n  date: %s,\n  priority: %s,\n  data: %s\n}", this.getTime(), this.getPriority(), this.getData());
    }

    /**
     * Get the time the notification was sent
     * @return Time notification was sent
     */
    public Timestamp getTime() {
        return time;
    }

    /**
     * Get the priority of the notification
     * @return Notification priority
     */
    public PRIORITY getPriority() {
        return priority;
    }

    /**
     * Get the notification data
     * @return Data
     */
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
