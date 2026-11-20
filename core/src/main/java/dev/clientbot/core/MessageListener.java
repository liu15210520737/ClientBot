package dev.clientbot.core;

import com.mojang.authlib.GameProfile;
import dev.clientbot.command.CommandDispatcher;
import dev.clientbot.config.ConfigManager;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Accepts authenticated player whisper packets; server-emulated whispers are opt-in. */
public final class MessageListener {
    private final Minecraft client;
    private final ConfigManager configs;
    private final CommandDispatcher dispatcher;

    public MessageListener(Minecraft client, ConfigManager configs, CommandDispatcher dispatcher) {
        this.client = client;
        this.configs = configs;
        this.dispatcher = dispatcher;
    }

    public void register() {
        ClientReceiveMessageEvents.CHAT.register((message, playerMessage, sender, bound, timestamp) -> {
            if (sender != null && isPrivate(message, bound)) {
                dispatcher.handleRemote(sender.id(), sender.name(), message.getString());
            }
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay && configs.get().allowSystemWhispers) handleSystem(message.getString());
        });
    }

    private boolean isPrivate(Component message, ChatType.Bound bound) {
        if (containsWhisperTranslation(message)) return true;
        return bound.toString().toLowerCase(Locale.ROOT).contains("msg_command_incoming");
    }

    private boolean containsWhisperTranslation(Component component) {
        if (component.getContents() instanceof TranslatableContents translatable
                && translatable.getKey().equals("commands.message.display.incoming")) return true;
        for (Component sibling : component.getSiblings()) {
            if (containsWhisperTranslation(sibling)) return true;
        }
        return false;
    }

    private void handleSystem(String text) {
        for (String expression : configs.get().systemWhisperPatterns) {
            try {
                Matcher matcher = Pattern.compile(expression).matcher(text);
                if (!matcher.matches()) continue;
                String sender = matcher.group("sender");
                String command = matcher.group("command");
                if (client.getConnection() == null) return;
                for (PlayerInfo info : client.getConnection().getListedOnlinePlayers()) {
                    GameProfile profile = info.getProfile();
                    if (profile.name().equalsIgnoreCase(sender)) {
                        dispatcher.handleRemote(profile.id(), profile.name(), command);
                        return;
                    }
                }
            } catch (Exception e) {
                ClientBotMod.LOGGER.warn("无效的系统私聊匹配规则: {}", expression, e);
            }
        }
    }
}
