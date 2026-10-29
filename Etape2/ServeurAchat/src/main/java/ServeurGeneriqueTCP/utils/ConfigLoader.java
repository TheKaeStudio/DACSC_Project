package ServeurGeneriqueTCP.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private final Properties properties = new Properties();

    public ConfigLoader() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties"))
        {

            if (input == null) {
                throw new IllegalStateException( "Fichier config.properties introuvable" );
            }

            properties.load(input);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Impossible de lire la configuration", e
            );
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }
}