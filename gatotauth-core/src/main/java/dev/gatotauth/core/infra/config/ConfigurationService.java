package dev.gatotauth.core.infra.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ConfigurationService manages read access to engine settings.
 */
public final class ConfigurationService {
    private final Map<String, Object> configValues = new ConcurrentHashMap<>();

    /**
     * Retrieves a configuration setting.
     *
     * @param key Config key name.
     * @param defaultValue Fallback value if configuration key does not exist.
     * @param <T> Expected return type.
     * @return Configuration value or default.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        Object val = configValues.get(key);
        if (val == null) {
            return defaultValue;
        }
        try {
            return (T) val;
        } catch (ClassCastException ex) {
            return defaultValue;
        }
    }

    /**
     * Sets a configuration option (used during bootloader parsing).
     *
     * @param key Config key name.
     * @param value Config value object.
     */
    public void set(String key, Object value) {
        if (key != null && value != null) {
            configValues.put(key, value);
        }
    }

    /**
     * Loads configurations from a JSON file, or creates defaults if missing.
     *
     * @param file Target file path.
     */
    public void loadFromFile(Path file) {
        if (!Files.exists(file)) {
            writeDefaultConfig(file);
        }
        try {
            String content = Files.readString(file);
            Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|([\\w\\d.-]+))");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String key = matcher.group(1);
                String valueStr = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
                if (valueStr.equalsIgnoreCase("true") || valueStr.equalsIgnoreCase("false")) {
                    configValues.put(key, Boolean.parseBoolean(valueStr));
                } else if (valueStr.matches("-?\\d+")) {
                    configValues.put(key, Integer.parseInt(valueStr));
                } else {
                    configValues.put(key, valueStr);
                }
            }
            validate();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read configuration file", e);
        }
    }

    private void writeDefaultConfig(Path file) {
        String defaultJson = "{\n"
                + "  \"storage.type\": \"sqlite\",\n"
                + "  \"storage.sqlite.path\": \"gatotauth.db\",\n"
                + "  \"security.password.strength\": 10,\n"
                + "  \"session.expiration-minutes\": 30\n"
                + "}\n";
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, defaultJson);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write default configuration", e);
        }
    }

    private void validate() {
        int strength = get("security.password.strength", 10);
        if (strength < 4 || strength > 31) {
            throw new IllegalArgumentException(
                    "Invalid security.password.strength: " + strength + ". Must be between 4 and 31."
            );
        }
        int sessionExp = get("session.expiration-minutes", 30);
        if (sessionExp <= 0) {
            throw new IllegalArgumentException("session.expiration-minutes must be positive.");
        }
    }

    /**
     * Clears all configurations.
     */
    public void clear() {
        configValues.clear();
    }
}
