package dev.clientbot.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** JSON-backed settings. Remote control is intentionally disabled by default. */
public final class BotConfig {
    public boolean enabled = false;
    public String commandPrefix = "!cb";
    public String replyMode = "whisper";
    public int maxCommandsPerMinute = 12;
    public int confirmationSeconds = 30;
    public boolean allowSystemWhispers = false;
    public boolean auditLog = true;
    public List<String> systemWhisperPatterns = new ArrayList<>(List.of(
            "^\\[(?<sender>[A-Za-z0-9_]{1,16}) whispers to you: (?<command>.*)]$",
            "^(?<sender>[A-Za-z0-9_]{1,16}) whispers to you: (?<command>.*)$"));
    public List<String> blockedServerCommands = new ArrayList<>(List.of(
            "stop", "op", "deop", "ban", "ban-ip", "pardon", "kick", "whitelist"));
    public List<Operator> operators = new ArrayList<>();
    public Rendering rendering = new Rendering();

    public static final class Operator {
        public String uuid = "";
        public String name = "";
        public List<String> permissions = new ArrayList<>();

        public Operator() {}

        public Operator(UUID uuid, String name, List<String> permissions) {
            this.uuid = uuid.toString();
            this.name = name;
            this.permissions = new ArrayList<>(permissions);
        }

        public UUID uuid() {
            return UUID.fromString(uuid);
        }

        public boolean permits(String node) {
            String wanted = node.toLowerCase(Locale.ROOT);
            for (String permission : permissions) {
                String candidate = permission.toLowerCase(Locale.ROOT);
                if (candidate.equals("*") || candidate.equals(wanted)) return true;
                if (candidate.endsWith(".*") && wanted.startsWith(candidate.substring(0, candidate.length() - 1))) {
                    return true;
                }
            }
            return false;
        }
    }

    public static final class Rendering {
        public boolean suppressWorld = false;
        public boolean suppressHand = false;
        public boolean limitFps = true;
        public int backgroundFps = 5;
        public int normalFps = 120;
    }

    public static List<String> defaultUserPermissions() {
        return List.of("core.status", "core.chat", "core.movement", "core.inventory",
                "core.interact", "core.render", "core.task",
                "extensions.carpet", "extensions.servux", "extensions.litematica",
                "extensions.litematicacfg", "extensions.printer", "extensions.printercfg",
                "extensions.tweakeroo", "extensions.minihud", "extensions.itemscroller",
                "extensions.quickshulker", "extensions.example");
    }
}
