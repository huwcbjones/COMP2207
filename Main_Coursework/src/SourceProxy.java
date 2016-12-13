import shared.notifications.SourceProxySource;
import shared.util.Log;

import java.rmi.RemoteException;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 12/12/2016
 */
public class SourceProxy {

    public static void main(String[] args){
        try {
            SourceProxySource sourceProxy = new SourceProxySource();
        } catch (RemoteException e) {
            Log.Fatal("Failed to start SourceProxy.\n" + e.getMessage());
            e.printStackTrace();
        }
    }
}
