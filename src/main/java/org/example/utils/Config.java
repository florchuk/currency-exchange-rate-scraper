package org.example.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public class Config {
    private final Properties properties;

    private static Config instance;

    /**
     * Constructor.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the configuration resource could not be found.
     */
    private Config() throws IOException, NullPointerException {
        // If configuration file path is not set (in system properties),
        // then assume that the configuration file is located in the same directory that the app running.
        try (
                InputStream inputStream =
                        Files.newInputStream(Path.of(System.getProperty("configPath", "config.properties")))
        ) {
            this.properties = new Properties();

            this.properties.load(Objects.requireNonNull(inputStream));
        }
    }

    /**
     * Getting configuration property value, for the provided key.
     * @param key Configuration property key.
     * @return Configuration property value, for the provided key.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the configuration resource could not be found,
     *         or if a configuration property value, for the provided key, wasn't found.
     */
    public static String getProperty(String key) throws IOException, NullPointerException {
        return Config.getProperty(key, null);
    }

    /**
     * Getting configuration property value, for the provided key,
     * or default value, if configuration property value, for provided key, is null.
     * @param key Configuration property key.
     * @param defaultValue Default value.
     * @return Configuration property value, for the provided key, or default value,
     *         if configuration property value, for provided key, is null.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If the configuration resource could not be found,
     *         or if a configuration property value, for the provided key, wasn't found, and default value is null.
     */
    public static String getProperty(String key, String defaultValue) throws IOException, NullPointerException {
        if (Config.instance == null) {
            Config.instance = new Config();
        }

        return Objects.requireNonNull(Config.instance.properties.getProperty(key, defaultValue));
    }
}