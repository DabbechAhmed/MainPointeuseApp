package com.example.mainapp.utils;

import java.io.*;
import java.util.Properties;

public class ConfigManager {

    private static final String CONFIG_FILE = "server_config.properties";
    private Properties properties;

    public ConfigManager() {
        properties = new Properties();
        loadConfig();
    }

    private void loadConfig() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException ex) {
            properties.setProperty("server.port", "8080");
            properties.setProperty("tolerance.minutes", "15");
            saveConfig();
        }
    }

    public void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "Server Configuration");
        } catch (IOException io) {
            System.err.println("❌ Erreur de sauvegarde config : " + io.getMessage());
        }
    }

    public int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "8080"));
    }

    public void setServerPort(int port) {
        properties.setProperty("server.port", String.valueOf(port));
    }

    public int getToleranceMinutes() {
        return Integer.parseInt(properties.getProperty("tolerance.minutes", "15"));
    }

    public void setToleranceMinutes(int minutes) {
        properties.setProperty("tolerance.minutes", String.valueOf(minutes));
    }
}