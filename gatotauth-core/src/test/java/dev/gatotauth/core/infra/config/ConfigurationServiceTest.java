package dev.gatotauth.core.infra.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigurationServiceTest {
    @TempDir
    Path tempDir;

    private ConfigurationService configService;

    @BeforeEach
    void setUp() {
        configService = new ConfigurationService();
    }

    @Test
    void shouldCreateDefaultConfigIfMissing() {
        Path confFile = tempDir.resolve("config.json");
        assertThat(Files.exists(confFile)).isFalse();

        configService.loadFromFile(confFile);

        assertThat(Files.exists(confFile)).isTrue();
        assertThat(configService.<String>get("storage.type", "default")).isEqualTo("sqlite");
        assertThat(configService.<Integer>get("security.password.strength", 1)).isEqualTo(10);
    }

    @Test
    void shouldLoadValidConfigSuccessfully() throws IOException {
        Path confFile = tempDir.resolve("config.json");
        String customJson = "{\n"
                + "  \"storage.type\": \"postgresql\",\n"
                + "  \"security.password.strength\": 12,\n"
                + "  \"session.expiration-minutes\": 15\n"
                + "}\n";
        Files.writeString(confFile, customJson);

        configService.loadFromFile(confFile);

        assertThat(configService.<String>get("storage.type", "")).isEqualTo("postgresql");
        assertThat(configService.<Integer>get("security.password.strength", 0)).isEqualTo(12);
        assertThat(configService.<Integer>get("session.expiration-minutes", 0)).isEqualTo(15);
    }

    @Test
    void shouldThrowExceptionOnInvalidPasswordStrength() throws IOException {
        Path confFile = tempDir.resolve("config.json");
        String invalidJson = "{\n"
                + "  \"security.password.strength\": 3\n"
                + "}\n";
        Files.writeString(confFile, invalidJson);

        assertThatThrownBy(() -> configService.loadFromFile(confFile))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
