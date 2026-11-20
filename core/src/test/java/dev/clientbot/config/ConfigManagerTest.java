package dev.clientbot.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConfigManagerTest {
    @TempDir
    Path tempDir;

    @Test
    void saveAndLoadRoundTrip() {
        Path file = tempDir.resolve("nested/core.json");
        ConfigManager manager = new ConfigManager(file);
        manager.get().enabled = true;
        manager.get().commandPrefix = "!bot";
        manager.get().operators.add(new BotConfig.Operator(UUID.randomUUID(), "Tester",
                java.util.List.of("core.status")));
        manager.save();

        ConfigManager loaded = new ConfigManager(file);
        loaded.load();
        assertTrue(loaded.get().enabled);
        assertEquals("!bot", loaded.get().commandPrefix);
        assertEquals(1, loaded.get().operators.size());
        assertTrue(Files.exists(file));
    }

    @Test
    void sanitizesNumbersCollectionsAndReplyMode() {
        ConfigManager manager = new ConfigManager(tempDir.resolve("core.json"));
        BotConfig config = manager.get();
        config.commandPrefix = "   ";
        config.replyMode = "invalid";
        config.maxCommandsPerMinute = 999;
        config.confirmationSeconds = -1;
        config.rendering = null;
        config.operators.add(null);
        BotConfig.Operator operator = new BotConfig.Operator();
        operator.permissions = null;
        config.operators.add(operator);
        config.blockedServerCommands.add(null);
        config.systemWhisperPatterns.add(null);
        manager.save();

        assertEquals("!cb", config.commandPrefix);
        assertEquals("whisper", config.replyMode);
        assertEquals(120, config.maxCommandsPerMinute);
        assertEquals(5, config.confirmationSeconds);
        assertNotNull(config.rendering);
        assertEquals(1, config.operators.size());
        assertNotNull(operator.permissions);
        assertFalse(config.blockedServerCommands.contains(null));
        assertFalse(config.systemWhisperPatterns.contains(null));
    }

    @Test
    void reloadKeepsConfigurationIdentityForExistingServices() throws Exception {
        Path file = tempDir.resolve("core.json");
        Files.writeString(file, """
                {
                  "enabled": true,
                  "commandPrefix": "!new",
                  "replyMode": "local",
                  "maxCommandsPerMinute": 42
                }
                """);
        ConfigManager manager = new ConfigManager(file);
        BotConfig retained = manager.get();
        manager.load();

        assertSame(retained, manager.get());
        assertTrue(retained.enabled);
        assertEquals("!new", retained.commandPrefix);
        assertEquals("local", retained.replyMode);
        assertEquals(42, retained.maxCommandsPerMinute);
    }

    @Test
    void malformedFileIsNotOverwrittenAndFallsBackToSafeDefaults() throws Exception {
        Path file = tempDir.resolve("core.json");
        String malformed = "{ definitely not valid JSON";
        Files.writeString(file, malformed);
        ConfigManager manager = new ConfigManager(file);
        manager.get().enabled = true;
        manager.load();

        assertFalse(manager.get().enabled);
        assertTrue(manager.get().operators.isEmpty());
        assertEquals(malformed, Files.readString(file));
    }

    @Test
    void nullOptionalObjectsAreRepairedDuringLoad() throws Exception {
        Path file = tempDir.resolve("core.json");
        Files.writeString(file, """
                {
                  "rendering": null,
                  "operators": null,
                  "blockedServerCommands": null,
                  "systemWhisperPatterns": null
                }
                """);
        ConfigManager manager = new ConfigManager(file);
        manager.load();
        assertNotNull(manager.get().rendering);
        assertNotNull(manager.get().operators);
        assertNotNull(manager.get().blockedServerCommands);
        assertNotNull(manager.get().systemWhisperPatterns);
    }
}
