import client.Config;
import client.GifClient;
import shared.util.Log;

import javax.swing.*;

/**
 * Client Bootstrapper
 *
 * @author Huw Jones
 * @since 06/12/2016
 */
public class Client {

    public static void main(String[] args){
        Log.setLogLevel(args);
        Config.loadConfig();
        SwingUtilities.invokeLater(() -> {
            GifClient gifClient = new GifClient();
        });
    }
}
