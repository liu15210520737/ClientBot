package dev.clientbot.core;

import dev.clientbot.api.ExtensionContext;
import dev.clientbot.command.CommandDispatcher;
import dev.clientbot.command.CommandRegistry;
import dev.clientbot.command.ResponseRouter;
import dev.clientbot.config.ConfigManager;
import dev.clientbot.integration.IntegrationRegistry;
import dev.clientbot.task.TaskManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientBotMod implements ClientModInitializer {
    public static final String MOD_ID = "clientbot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ClientBotMod instance;

    private ConfigManager configs;
    private RenderController rendering;
    private LocalPlayer lastPlayer;

    public static ClientBotMod instance() { return instance; }
    public RenderController rendering() { return rendering; }

    @Override
    public void onInitializeClient() {
        instance = this;
        Minecraft client = Minecraft.getInstance();
        configs = new ConfigManager();
        configs.load();
        rendering = new RenderController(client, configs);
        CommandRegistry commands = new CommandRegistry();
        TaskManager tasks = new TaskManager();
        IntegrationRegistry integrations = new IntegrationRegistry();
        ActionController actions = new ActionController();
        ResponseRouter responses = new ResponseRouter(client, configs.get());
        CommandDispatcher dispatcher = new CommandDispatcher(client, configs, commands, responses);
        ExtensionManager extensions = new ExtensionManager();
        ExtensionContext context = new ExtensionContext(client, commands, tasks, integrations, configs.get(),
                FabricLoader.getInstance().getConfigDir().resolve("clientbot"));

        CoreCommands.register(context, dispatcher, extensions, rendering, actions);
        extensions.load(context);
        new MessageListener(client, configs, dispatcher).register();
        LocalCommands.register(client, configs, responses, dispatcher, rendering);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player != lastPlayer) {
                if (lastPlayer != null) { tasks.stopAll(); actions.clear(); }
                lastPlayer = mc.player;
                rendering.applyFrameLimit();
            }
            if (mc.player != null && mc.level != null) {
                tasks.tick();
                actions.apply(mc);
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) -> {
            tasks.stopAll();
            actions.clear();
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> {
            tasks.stopAll();
            actions.clear();
            configs.save();
        });
        LOGGER.info("ClientBot 已初始化：{} 条核心/扩展指令，{} 个扩展。远程控制默认关闭。", commands.size(), extensions.statuses().size());
    }
}
