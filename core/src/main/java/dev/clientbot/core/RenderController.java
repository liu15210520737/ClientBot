package dev.clientbot.core;

import dev.clientbot.config.ConfigManager;
import net.minecraft.client.Minecraft;

/** Rendering-only power saver. Networking and client ticks remain active. */
public final class RenderController {
    private final Minecraft client;
    private final ConfigManager configs;

    public RenderController(Minecraft client, ConfigManager configs) {
        this.client = client;
        this.configs = configs;
    }

    public void setSuppressed(boolean value) {
        configs.get().rendering.suppressWorld = value;
        configs.get().rendering.suppressHand = value;
        applyFrameLimit();
        configs.save();
    }

    public void toggle() {
        setSuppressed(!configs.get().rendering.suppressWorld);
    }

    public void applyFrameLimit() {
        if (client.options == null) return;
        int target = configs.get().rendering.limitFps && configs.get().rendering.suppressWorld
                ? configs.get().rendering.backgroundFps : configs.get().rendering.normalFps;
        client.options.framerateLimit().set(target);
    }

    public boolean isSuppressed() {
        return configs.get().rendering.suppressWorld;
    }

    public boolean isHandSuppressed() {
        return configs.get().rendering.suppressHand;
    }
}
