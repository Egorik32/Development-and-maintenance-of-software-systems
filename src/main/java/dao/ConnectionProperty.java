package dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConnectionProperty {
    public static final String CONFIG_NAME = "config.properties";
    private static final Properties CONFIG = new Properties();

    static {
        ClassLoader classLoader = ConnectionProperty.class.getClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream("config/" + CONFIG_NAME)) {
            if (stream == null) {
                throw new IOException("Configuration file is not found: " + CONFIG_NAME);
            }
            CONFIG.load(stream);
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static String getProperty(String property) {
        return CONFIG.getProperty(property);
    }
}
