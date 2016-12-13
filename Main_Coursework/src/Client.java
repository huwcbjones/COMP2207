import client.GifClient;
import client.Config;

import javax.swing.*;

/**
 * Client Bootstrapper
 *
 * @author Huw Jones
 * @since 06/12/2016
 */
public class Client {

    public static void main(String[] args){
        Config.loadConfig();
        SwingUtilities.invokeLater(() -> {
            GifClient gifClient = new GifClient();
        });
    }
}
