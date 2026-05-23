package com.sigomei.servidor.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {

    private static final String DEFAULT_CONFIG = "config/app.properties";
    private static final Properties PROPERTIES = cargar();

    private AppConfig() {
    }

    public static String get(String key, String defaultValue) {
        return PROPERTIES.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
    }

    private static Properties cargar() {
        Properties properties = new Properties();
        String path = System.getProperty("sigomei.config", DEFAULT_CONFIG);
        try (InputStream input = new FileInputStream(path)) {
            properties.load(input);
        } catch (IOException ignored) {
            try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("app.properties")) {
                if (input != null) {
                    properties.load(input);
                }
            } catch (IOException ignoredAgain) {
            }
        }
        return properties;
    }
}
