package dev.clientbot.core;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.clientbot.command.CommandDispatcher;
import dev.clientbot.command.ResponseRouter;
import dev.clientbot.config.BotConfig;
import dev.clientbot.config.ConfigManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class LocalCommands {
    private LocalCommands() {}

    public static void register(Minecraft client, ConfigManager configs, ResponseRouter responses,
                                CommandDispatcher dispatcher, RenderController rendering) {
        ClientCommandRegistrationCallback.EVENT.register((commands, buildContext) -> commands.register(
                ClientCommands.literal("clientbot")
                        .then(ClientCommands.literal("enable").executes(c -> { configs.get().enabled = true; configs.save(); feedback(client, "远程控制已启用"); return 1; }))
                        .then(ClientCommands.literal("disable").executes(c -> { configs.get().enabled = false; configs.save(); feedback(client, "远程控制已禁用"); return 1; }))
                        .then(ClientCommands.literal("status").executes(c -> {
                            feedback(client, "enabled=" + configs.get().enabled + ", operators=" + configs.get().operators.size()
                                    + ", commands=" + dispatcher.commandCount() + ", renderingSuppressed=" + rendering.isSuppressed());
                            return 1;
                        }))
                        .then(ClientCommands.literal("render").then(ClientCommands.argument("enabled", BoolArgumentType.bool()).executes(c -> {
                            rendering.setSuppressed(BoolArgumentType.getBool(c, "enabled"));
                            feedback(client, "世界渲染=" + !rendering.isSuppressed());
                            return 1;
                        })))
                        .then(ClientCommands.literal("allow")
                                .then(ClientCommands.argument("player", StringArgumentType.word())
                                        .then(ClientCommands.argument("role", StringArgumentType.word()).executes(c -> {
                                            String name = StringArgumentType.getString(c, "player");
                                            String role = StringArgumentType.getString(c, "role");
                                            PlayerInfo info = findOnline(client, name);
                                            if (info == null) { feedback(client, "玩家不在线，无法可靠解析 UUID"); return 0; }
                                            configs.get().operators.removeIf(o -> o.uuid.equalsIgnoreCase(info.getProfile().id().toString()));
                                            List<String> permissions = role.equals("admin") ? List.of("*") : BotConfig.defaultUserPermissions();
                                            configs.get().operators.add(new BotConfig.Operator(info.getProfile().id(), info.getProfile().name(), permissions));
                                            configs.save();
                                            feedback(client, "已授权 " + name + " (" + role + "): " + info.getProfile().id());
                                            return 1;
                                        }))))
                        .then(ClientCommands.literal("deny").then(ClientCommands.argument("uuid", StringArgumentType.word()).executes(c -> {
                            String uuid = StringArgumentType.getString(c, "uuid");
                            boolean removed = configs.get().operators.removeIf(o -> o.uuid.equalsIgnoreCase(uuid));
                            configs.save(); feedback(client, removed ? "已撤销" : "未找到该 UUID");
                            return removed ? 1 : 0;
                        })))
                        .then(ClientCommands.literal("reload").executes(c -> { configs.load(); rendering.applyFrameLimit(); feedback(client, "配置已重载"); return 1; }))
                        .then(ClientCommands.literal("run").then(ClientCommands.argument("command", StringArgumentType.greedyString()).executes(c -> {
                            dispatcher.executeLocal(StringArgumentType.getString(c, "command")); return 1;
                        })))
        ));
    }

    private static PlayerInfo findOnline(Minecraft client, String name) {
        if (client.getConnection() == null) return null;
        for (PlayerInfo info : client.getConnection().getListedOnlinePlayers()) {
            if (info.getProfile().name().equalsIgnoreCase(name)) return info;
        }
        return null;
    }

    private static void feedback(Minecraft client, String message) {
        if (client.player != null) client.player.sendSystemMessage(Component.literal("[ClientBot] " + message));
    }
}
