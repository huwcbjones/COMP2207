import shared.util.Log;

import java.rmi.RemoteException;

/**
 * Source Proxy Source
 * Allows sources to register on an non-localhost RMI registry.
 * Also provides notification to sinks when sources register.
 *
 * @author Huw Jones
 * @since 12/12/2016
 */
public class SourceProxy {

    public static void main(String[] args){
        try {
            sourceproxy.SourceProxy sourceProxy = new sourceproxy.SourceProxy();
        } catch (RemoteException e) {
            Log.Fatal("Failed to start SourceProxy.\n" + e.getMessage());
            e.printStackTrace();
        }
    }
}
