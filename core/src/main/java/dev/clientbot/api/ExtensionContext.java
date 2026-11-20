package dev.clientbot.api;

import dev.clientbot.command.CommandRegistry;
import dev.clientbot.config.BotConfig;
import dev.clientbot.integration.IntegrationRegistry;
import dev.clientbot.task.TaskManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;

/** Stable services passed to every extension. */
public record ExtensionContext(
        Minecraft client,
        CommandRegistry commands,
        TaskManager tasks,
        IntegrationRegistry integrations,
        BotConfig config,
        Path configDirectory) {

    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    /** Run work on the client thread. Minecraft objects must not be touched from worker threads. */
    public void execute(Runnable runnable) {
        client.execute(runnable);
    }
}
