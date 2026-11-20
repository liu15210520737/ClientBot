package dev.clientbot.extension.minihud;

import dev.clientbot.api.ClientBotExtension;
import dev.clientbot.api.ExtensionContext;
import dev.clientbot.util.MasaCommandSupport;
import dev.clientbot.util.ReflectedConfig;

import java.util.Set;

public final class MiniHudExtension implements ClientBotExtension {
    public String id() { return "minihud"; }
    public Set<String> requiredMods() { return Set.of("minihud"); }

    public void initialize(ExtensionContext context) throws Exception {
        String root = "fi.dy.masa.minihud.config.Configs";
        ReflectedConfig config = ReflectedConfig.scan(root,
                "fi.dy.masa.minihud.config.InfoToggle",
                "fi.dy.masa.minihud.config.RendererToggle",
                "fi.dy.masa.minihud.config.StructureToggle");
        MasaCommandSupport.register(context, "minihud", root, config);
        config.registerBooleanToggles(context.integrations(), "minihud", "minihud.");
    }
}
