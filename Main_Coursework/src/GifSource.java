import server.GifStreamer;
import shared.util.Log;

import java.io.File;
import java.rmi.RemoteException;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 13/12/2016
 */
public class GifSource {

    public static void main(String[] args){
        if(args.length != 1){
            Log.Fatal("Please provide a path to gif file.");
            return;
        }
        File gifFile = new File(args[0]);
        try {
            GifStreamer streamer = new GifStreamer(gifFile);
        } catch (RemoteException e) {
            Log.Fatal("Failed to start Clock.\n" + e.getMessage());
            e.printStackTrace();
        }
    }
}
