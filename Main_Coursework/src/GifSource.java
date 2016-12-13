import server.Config;
import server.GifStreamer;
import shared.util.Log;

import java.io.File;
import java.rmi.RemoteException;

/**
 * GIF Stream Source
 *
 * @author Huw Jones
 * @since 13/12/2016
 */
public class GifSource {

    public static void main(String[] args){
        Log.setLogLevel(args);
        Config.loadConfig();
        File gifFile = null;
        if(Config.getServerID() == null){
            Log.Fatal("serverID not set. Please set serverID before starting server.");
            System.exit(1);
        }

        if(Config.getSource() != null) {
            gifFile = new File(Config.getSource());
        } else {
            if(args.length == 0){
                Log.Fatal("Please provide the path to the source gif.");
                System.exit(1);
            } else {
                gifFile = new File(args[0]);
            }
        }

        try {
            GifStreamer streamer = new GifStreamer(gifFile);
        } catch (RemoteException e) {
            Log.Fatal("Failed to start GifStreamer.\n" + e.getMessage());
            e.printStackTrace();
        }
    }
}
