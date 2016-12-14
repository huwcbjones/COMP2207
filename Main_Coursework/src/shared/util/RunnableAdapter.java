package shared.util;


/**
 * RunnableAdapter Adapter
 *
 * @author Huw Jones
 * @since 23/04/2016
 */
public abstract class RunnableAdapter implements java.lang.Runnable {

    @Override
    public void run() {
        try {
            this.runSafe();
        } catch (Exception ex){
            Log.Error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public abstract void runSafe() throws Exception;
}