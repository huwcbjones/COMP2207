import client.Client;
import client.Config;

import javax.swing.*;

/**
 * Client Bootstrapper
 *
 * @author Huw Jones
 * @since 06/12/2016
 */
public class Client2 {

    public static void main(String[] args){
        Config.loadConfig();
        SwingUtilities.invokeLater(() -> {
            Client client = new Client();
        });
    }
}
