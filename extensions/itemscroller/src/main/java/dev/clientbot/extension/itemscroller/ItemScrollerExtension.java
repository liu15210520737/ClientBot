package dev.clientbot.extension.itemscroller;

import dev.clientbot.api.ClientBotExtension;
import dev.clientbot.api.ExtensionContext;
import dev.clientbot.util.MasaCommandSupport;
import dev.clientbot.util.ReflectedConfig;

import java.util.Set;

public final class ItemScrollerExtension implements ClientBotExtension {
    public String id() { return "itemscroller"; }
    public Set<String> requiredMods() { return Set.of("itemscroller"); }

    public void initialize(ExtensionContext context) throws Exception {
        String root = "fi.dy.masa.itemscroller.config.Configs";
        ReflectedConfig config = ReflectedConfig.scan(root);
        MasaCommandSupport.register(context, "itemscroller", root, config);
        config.registerBooleanToggles(context.integrations(), "itemscroller", "itemscroller.");
    }
}
