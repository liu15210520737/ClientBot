package dev.clientbot.command;

import dev.clientbot.config.BotConfig;
import dev.clientbot.core.ClientBotMod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Routes remote replies back through /msg; local replies stay in the client HUD. */
public final class ResponseRouter {
    private final Minecraft client;
    private final BotConfig config;

    public ResponseRouter(Minecraft client, BotConfig config) {
        this.client = client;
        this.config = config;
    }

    public void forSender(CommandSender sender, String message) {
        String clean = "[CB] " + message.replaceAll("[\\r\\n\\t]", " ").trim();
        if (sender.local() || config.replyMode.equalsIgnoreCase("local")) {
            local(clean);
            return;
        }
        if (config.replyMode.equalsIgnoreCase("off") || client.player == null) return;
        String target = sender.name().replaceAll("[^A-Za-z0-9_]", "");
        if (target.isEmpty()) {
            local(clean + "（无法私聊发送者）");
            return;
        }
        for (String part : split(clean, 180)) {
            client.player.connection.sendCommand("msg " + target + " " + part);
        }
    }

    public void local(String message) {
        if (client.player != null) client.player.sendSystemMessage(Component.literal(message));
        else ClientBotMod.LOGGER.info(message);
    }

    private static List<String> split(String value, int length) {
        List<String> parts = new ArrayList<>();
        for (int offset = 0; offset < value.length(); offset += length) {
            parts.add(value.substring(offset, Math.min(value.length(), offset + length)));
        }
        return parts;
    }
}
