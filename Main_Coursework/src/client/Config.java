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

    /**
     * Get the RMI Server hostname
     * @return Hostname
     */
    public static String getRmiServer() {
        return rmiServer;
    }

    /**
     * Set the RMI server hostname
     * @param rmiServer Hostname
     */
    public static void setRmiServer(String rmiServer) {
        Config.rmiServer = rmiServer;
        saveConfig();
    }

    /**
     * Get the RMI server port
     * @return Port number
     */
    public static Integer getRmiPort() {
        return rmiPort;
    }

    /**
     * Set the RMI server port
     * @param rmiPort Port number
     */
    public static void setRmiPort(Integer rmiPort) {
        Config.rmiPort = rmiPort;
        saveConfig();
    }

    /**
     * Get the client UUID
     * @return Client UUID
     */
    public static UUID getClientID() {
        return clientID;
    }

    /**
     * Set the client UUID
     * @param clientID UUID
     */
    public static void setClientID(UUID clientID) {
        Config.clientID = clientID;
        saveConfig();
    }

    /**
     * Get a list of sources that are known to the client
     * @return List of sources
     */
    public static List<String> getSources() {
        return new ArrayList<>(sources);
    }

    /**
     * Add a source to the list of sources
     * @param source Source to add
     */
    public static void addSource(String source) {
        sources.add(source);
        saveConfig();
    }

    /**
     * Remove a source from the list of sources
     * @param source Source to remove
     */
    public static void removeSource(String source) {
        if (sources.contains(source)) sources.remove(source);
        saveConfig();
    }

    /**
     * Returns whether the client should automatically connect to the RMI server (as per the server declaration),
     * and any sources (as per the source declarations).
     * @return True if the client should autoconnect
     */
    public static boolean isAutoconnect() {
        return autoconnect;
    }

    /**
     * Sets whether or not the client should autoconnect to the RMI server/sources.
     * @param autoconnect True if the client should autoconnect
     */
    public static void setAutoconnect(boolean autoconnect) {
        Config.autoconnect = autoconnect;
        saveConfig();
    }

    /**
     * Saves the config to the file specified by configLocation.
     */
    public static void saveConfig() {
        try {
            saveConfig(configLocation);
        } catch (IOException ex) {
            Log.Error("Failed to save config file:" + ex.getMessage());
        }
    }

    /**
     * Saves the config file to a specified location
     * @param location Location to save the config file
     * @throws FileSystemException Thrown if there was an error whilst saving the config file
     */
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

    /**
     * Creates a string from the current configuration that can be loaded at a later date.
     * @return Config string
     */
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

    /**
     * Loads the config from the default location
     */
    public static void loadConfig() {
        loadConfig(configLocation);
    }

    /**
     * Loads the config from a specified location
     * @param location Location to load config from
     */
    public static void loadConfig(String location) {
        try {
            String configFile = new String(Files.readAllBytes(Paths.get(location)));

            // Split file by semicolon, newline
            String[] statements = configFile.split(";(\n|\r\n|\r)");
            int statementNumber = 1;

            // Process each statement
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

    /**
     * Loads a config statement and updates the config options
     * @param statement Statement to load
     * @param number Statement number (for error logging)
     * @throws ParseException Thrown if the statement could not be processed
     */
    private static void processStatement(String statement, int number) throws ParseException {
        if (statement.substring(0, 2).equals("//")) {
            return;
        }

        // Separate the declaration, from the data
        String[] strings = statement.split(":");

        // Should have 2 strings now (declaration, value)
        if (strings.length != 2) {
            throw new ParseException("Failed to parse declaration.", number);
        }

        // Strip semicolon out
        strings[1] = strings[1].replace(";", "");

        // Parse data
        switch (strings[0]) {
            case "clientID":
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
