package dev.clientbot.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.clientbot.core.ClientBotMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final Path file;
    private BotConfig config = new BotConfig();

    public ConfigManager() {
        this(FabricLoader.getInstance().getConfigDir().resolve("clientbot").resolve("core.json"));
    }

    /** Also useful for isolated tests and tools that manage a separate configuration file. */
    public ConfigManager(Path file) {
        this.file = file;
    }

    public BotConfig get() {
        return config;
    }

    public void load() {
        try {
            if (Files.notExists(file)) {
                save();
                return;
            }
            BotConfig loaded = GSON.fromJson(Files.readString(file), BotConfig.class);
            adopt(loaded == null ? new BotConfig() : loaded);
            sanitize();
        } catch (Exception e) {
            ClientBotMod.LOGGER.error("无法读取 {}；使用默认配置。原文件不会被自动覆盖。", file, e);
            adopt(new BotConfig());
            sanitize();
        }
    }

    /* Keep the BotConfig identity stable: ExtensionContext and ResponseRouter retain it. */
    private void adopt(BotConfig loaded) {
        config.enabled = loaded.enabled;
        config.commandPrefix = loaded.commandPrefix;
        config.replyMode = loaded.replyMode;
        config.maxCommandsPerMinute = loaded.maxCommandsPerMinute;
        config.confirmationSeconds = loaded.confirmationSeconds;
        config.allowSystemWhispers = loaded.allowSystemWhispers;
        config.auditLog = loaded.auditLog;
        config.systemWhisperPatterns = loaded.systemWhisperPatterns;
        config.blockedServerCommands = loaded.blockedServerCommands;
        config.operators = loaded.operators;
        config.rendering = loaded.rendering;
    }

    public void save() {
        try {
            sanitize();
            Files.createDirectories(file.getParent());
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temporary, GSON.toJson(config));
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            ClientBotMod.LOGGER.error("无法保存 {}", file, e);
        }
    }

    private void sanitize() {
        if (config.commandPrefix == null || config.commandPrefix.isBlank()) config.commandPrefix = "!cb";
        config.commandPrefix = config.commandPrefix.trim();
        if (config.commandPrefix.length() > 32) config.commandPrefix = config.commandPrefix.substring(0, 32);
        if (config.replyMode == null || !java.util.Set.of("whisper", "local", "off").contains(config.replyMode.toLowerCase(java.util.Locale.ROOT))) {
            config.replyMode = "whisper";
        }
        config.maxCommandsPerMinute = Math.max(1, Math.min(120, config.maxCommandsPerMinute));
        config.confirmationSeconds = Math.max(5, Math.min(300, config.confirmationSeconds));
        if (config.rendering == null) config.rendering = new BotConfig.Rendering();
        config.rendering.backgroundFps = Math.max(1, Math.min(60, config.rendering.backgroundFps));
        config.rendering.normalFps = Math.max(5, Math.min(1000, config.rendering.normalFps));
        if (config.operators == null) config.operators = new java.util.ArrayList<>();
        config.operators.removeIf(java.util.Objects::isNull);
        for (BotConfig.Operator operator : config.operators) {
            if (operator.uuid == null) operator.uuid = "";
            if (operator.name == null) operator.name = "";
            if (operator.permissions == null) operator.permissions = new java.util.ArrayList<>();
            operator.permissions.removeIf(java.util.Objects::isNull);
        }
        if (config.blockedServerCommands == null) config.blockedServerCommands = new java.util.ArrayList<>();
        config.blockedServerCommands.removeIf(java.util.Objects::isNull);
        if (config.systemWhisperPatterns == null) config.systemWhisperPatterns = new java.util.ArrayList<>();
        config.systemWhisperPatterns.removeIf(java.util.Objects::isNull);
    }
}
