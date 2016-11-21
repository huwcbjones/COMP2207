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
    private final int priority;

    public Notification(int priority, T data){
        this.priority = priority;
        this.data = data;
        this.time = new Timestamp(Calendar.getInstance().getTime().getTime());
    }

    public T getData() {
        return data;
    }

    public Timestamp getTime() {
        return time;
    }

    public int getPriority() {
        return priority;
    }
}
