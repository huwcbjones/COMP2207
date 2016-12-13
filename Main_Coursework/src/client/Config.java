package client;

import shared.util.Log;
import shared.util.UUIDUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    private static String configLocation = "client.conf";

    private static UUID clientID = null;

    private static List<String> sources = new ArrayList<>();

    private static boolean autoconnect = false;

    private static String rmiServer = null;
    private static Integer rmiPort = null;

    public static String getRmiServer() {
        return rmiServer;
    }

    public static void setRmiServer(String rmiServer) {
        Config.rmiServer = rmiServer;
        saveConfig();
    }

    public static Integer getRmiPort() {
        return rmiPort;
    }

    public static void setRmiPort(Integer rmiPort) {
        Config.rmiPort = rmiPort;
        saveConfig();
    }

    public static UUID getClientID() {
        return clientID;
    }

    public static void setClientID(UUID clientID) {
        Config.clientID = clientID;
        saveConfig();
    }

    public static List<String> getSources() {
        return new ArrayList<>(sources);
    }

    public static void addSource(String source) {
        sources.add(source);
        saveConfig();
    }

    public static void removeSource(String source) {
        if (sources.contains(source)) sources.remove(source);
        saveConfig();
    }

    public static boolean isAutoconnect() {
        return autoconnect;
    }

    public static void setAutoconnect(boolean autoconnect) {
        Config.autoconnect = autoconnect;
        saveConfig();
    }

    public static void saveConfig() {
        try {
            saveConfig(configLocation);
        } catch (IOException ex) {
            Log.Error("Failed to save config file:" + ex.getMessage());
        }
    }

    public static void saveConfig(String location) throws FileSystemException {
        File configFile = new File(location);
        try {
            if (configFile.createNewFile()) {
                Log.Info("Created config file.");
            }
            if (configFile.isDirectory()) {
                throw new FileSystemException(location, null, "Cannot write to file - file is a directory.");
            }
            if (!configFile.canWrite()) {
                throw new FileSystemException(location, null, "Cannot write to file.");
            }
            PrintWriter output = new PrintWriter(configFile);
            output.write(getConfigString());
            output.close();
            Log.Info(String.format("Wrote config to '%s'", configLocation));
        } catch (IOException e) {
            Log.Error("Failed to save config file. " + e.getMessage());
        }
    }

    private static String getConfigString() {
        StringBuilder b = new StringBuilder();
        if (clientID != null) {
            b.append("clientID: ");
            b.append(UUIDUtils.UUIDToBase64String(clientID));
            b.append(";\n");
        }


        if(rmiServer != null && rmiPort != null){
            b.append("server: ");
            b.append(rmiServer);
            b.append(",");
            b.append(rmiPort);
            b.append(";\n");
        }

        b.append("autoconnect: ");
        b.append(autoconnect);
        b.append(";\n");

        sources.forEach(e -> {
            b.append("source: ");
            b.append(e);
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
                e = e.trim();
                if (e.length() == 0) continue;
                try {
                    processStatement(e, statementNumber);
                    statementNumber++;
                } catch (ParseException ex) {
                    Log.Error(ex.getMessage());
                    ex.printStackTrace();
                }
            }
        } catch (IOException e) {
            Log.Warn("Failed to load config file. " + e.getMessage());
            try {
                saveConfig(location);
            } catch (FileSystemException e1) {
                Log.Error("Failed to save config file:" + e1.getMessage());
            }
        }
    }

    private static void processStatement(String statement, int number) throws ParseException {
        if (statement.substring(0, 2).equals("//")) {
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
            case "server":
                String[] server = strings[1].split(",");
                if(server.length != 2) {
                    throw new ParseException("Invalid source statement: '" + statement + "'", number);
                }
                rmiServer = server[0].trim();
                rmiPort = Integer.parseInt(server[1].trim());
                break;
            case "source":
                sources.add(strings[1].trim());
        }
    }
}
