package server;

import shared.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Config Class
 *
 * @author Huw Jones
 * @since 05/12/2016
 */
public class Config {

    private static String configLocation = "server.conf";

    private static String serverID = null;

    private static int threadNumber = Runtime.getRuntime().availableProcessors() * 8;

    private static String source = null;

    private static String rmiServer = null;
    private static Integer rmiPort = null;

    /**
     * Sets the config file location from program arguments
     *
     * @param args Main arguments
     */
    public static void setConfigLocation(String[] args) {
        ArrayList<String> argList = new ArrayList<>(Arrays.asList(args));
        if (argList.contains("-c") || argList.contains("--conf")) {
            int index = (argList.contains("-c")) ? argList.indexOf("-c") : argList.indexOf("--conf");
            if (index + 1 < argList.size()) {
                configLocation = argList.get(index + 1);
            }
        }
    }

    /**
     * Get the RMI Server hostname
     *
     * @return Hostname
     */
    public static String getRmiServer() {
        return rmiServer;
    }

    /**
     * Set the RMI server hostname
     *
     * @param rmiServer Hostname
     */
    public static void setRmiServer(String rmiServer) {
        Config.rmiServer = rmiServer;
        saveConfig();
    }

    /**
     * Get the RMI server port
     *
     * @return Port number
     */
    public static Integer getRmiPort() {
        return rmiPort;
    }

    /**
     * Set the RMI server port
     *
     * @param rmiPort Port number
     */
    public static void setRmiPort(Integer rmiPort) {
        Config.rmiPort = rmiPort;
        saveConfig();
    }

    /**
     * Get the client ID
     *
     * @return Client ID
     */
    public static String getServerID() {
        return serverID;
    }

    /**
     * Set the server ID
     *
     * @param serverID ID
     */
    public static void setServerID(String serverID) {
        Config.serverID = serverID;
        saveConfig();
    }

    /**
     * Gets the source for the server
     *
     * @return Server source
     */
    public static String getSource() {
        return source;
    }

    /**
     * Sets the server source
     *
     * @param source Server source
     */
    public static void setSource(String source) {
        Config.source = source;
        saveConfig();
    }

    /**
     * Get the number of threads in the worker pool
     * @return Number of threads
     */
    public static int getThreadNumber() {
        return threadNumber;
    }

    /**
     * Set the number of threads in the worker pool
     * @param threadNumber Number of threads
     */
    public static void setThreadNumber(int threadNumber) {
        Config.threadNumber = threadNumber;
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
     *
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
     *
     * @return Config string
     */
    private static String getConfigString() {
        StringBuilder b = new StringBuilder();
        if (serverID != null) {
            b.append("serverID: ");
            b.append(serverID);
            b.append(";\n");
        } else {
            b.append("// uncomment and set a server ID string");
            b.append("// serverID: ");
            b.append(";\n");
        }

        b.append("threads: ");
        b.append(threadNumber);
        b.append(";\n");

        if (rmiServer != null && rmiPort != null) {
            b.append("server: ");
            b.append(rmiServer);
            b.append(",");
            b.append(rmiPort);
            b.append(";\n");
        }

        if (source != null) {
            b.append("source: ");
            b.append(source);
            b.append(";\n");
        } else {
            b.append("// set source to the (absolute or relative) path of the gif you want to stream from this source.");
            b.append("// source: ");
            b.append(";\n");
        }

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
     *
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
            Log.Fatal("Failed to load config file: " + location);
            Log.Info("Saving default config file...");
            try {
                saveConfig(location);
            } catch (FileSystemException e1) {
                Log.Error("Failed to save config file:" + e1.getMessage());
            }
            System.exit(0);
        }
    }

    /**
     * Loads a config statement and updates the config options
     *
     * @param statement Statement to load
     * @param number    Statement number (for error logging)
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
        strings[1] = strings[1].replace(";", "").trim();

        // Parse data
        switch (strings[0]) {
            case "serverID":
                serverID = strings[1].trim();
                break;
            case "server":
                String[] server = strings[1].split(",");
                if (server.length != 2) {
                    throw new ParseException("Invalid source statement: '" + statement + "'", number);
                }
                rmiServer = server[0].trim();
                rmiPort = Integer.parseInt(server[1].trim());
                break;
            case "source":
                source = strings[1];
                break;
            case "threads":
                threadNumber = Integer.parseInt(strings[1]);
                break;
        }
    }
}
