package client;

import shared.util.Log;
import shared.util.UUIDUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.registry.Registry;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Config Class
 *
 * @author Huw Jones
 * @since 05/12/2016
 */
public class Config {

    private static Registry registry;

    private static String configLocation = "client.conf";

    private static UUID clientID = UUID.randomUUID();

    private static List<URI> sources = new ArrayList<>();

    private static boolean autoconnect = false;

    public static UUID getClientID() {

        return clientID;
    }

    public static void setClientID(UUID clientID) {
        Config.clientID = clientID;
    }

    public static List<URI> getSources() {
        return new ArrayList<>(sources);
    }

    public static void addSource(URI source) {
        sources.add(source);
    }

    public static void removeSource(URI source) {
        if (sources.contains(source)) sources.remove(source);
    }

    public static boolean isAutoconnect() {
        return autoconnect;
    }

    public static void setAutoconnect(boolean autoconnect) {
        Config.autoconnect = autoconnect;
    }

    public static Registry getRegistry() {
        return registry;
    }

    public static void setRegistry(Registry registry) {
        Config.registry = registry;
    }

    public static void saveConfig(String location) throws FileSystemException {
        File configFile = new File(location);
        if (configFile.isDirectory()) {
            throw new FileSystemException(location, null, "Cannot write to file - file is a directory.");
        }
        if (!configFile.canWrite()) {
            throw new FileSystemException(location, null, "Cannot write to file.");
        }
        try {
            PrintWriter output = new PrintWriter(configFile);
            output.write(getConfigString());
            output.close();
            Log.Info(String.format("Wrote config to '%s'", configLocation));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String getConfigString() {
        StringBuilder b = new StringBuilder();
        b.append("clientID: ");
        b.append(UUIDUtils.UUIDToBase64String(clientID));
        b.append(";\n");

        sources.forEach(e -> {
            b.append("source: ");
            b.append(e.getHost());
            b.append(",");
            b.append(e.getPort());
            b.append(";\n");
        });
        return b.toString();
    }

    public static void loadConfig() {
        loadConfig(configLocation);
    }

    public static void loadConfig(String location) {
        try {
            String configFile = new String(Files.readAllBytes(Paths.get(location)));
            String[] statements = configFile.split(";(\n|\r\n|\r)");
            int statementNumber = 1;
            for (String e : statements) {
                try {
                    processStatement(e, statementNumber);
                    statementNumber++;
                } catch (ParseException ex) {
                    Log.Error(ex.getMessage());
                    ex.printStackTrace();
                }
            }
        } catch (IOException e) {
            Log.Error(e.toString());
            e.printStackTrace();
        }
    }

    private static void processStatement(String statement, int number) throws ParseException {
        if(statement.substring(0, 2).equals("//")){
            return;
        }
        String[] strings = statement.split(":");
        if (strings.length != 2) {
            throw new ParseException("Failed to parse declaration.", number);
        }
        strings[1] = strings[1].replace(";", "");
        switch (strings[0]) {
            case "id":
                clientID = UUIDUtils.Base64StringToUUID(strings[1].trim());
                break;
            case "autoconnect":
                autoconnect = Boolean.parseBoolean(strings[1].trim());
                break;
            case "source":
                String[] source = strings[1].split(",");
                if(source.length != 2){
                    throw new ParseException("Invalid source statement: '" + statement + "'", number);
                }
                try {
                    sources.add(new URI( source[0].trim() + ":" + source[1].trim() ));
                } catch (URISyntaxException e) {
                    throw new ParseException("Invalid source statement: '" + statement + "'", number);
                }
        }
    }
}
