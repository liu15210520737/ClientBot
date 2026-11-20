package dev.clientbot.extension.tweakeroo;

import dev.clientbot.api.ClientBotExtension;
import dev.clientbot.api.ExtensionContext;
import dev.clientbot.util.MasaCommandSupport;
import dev.clientbot.util.ReflectedConfig;

import java.util.Set;

public final class TweakerooExtension implements ClientBotExtension {
    public String id() { return "tweakeroo"; }
    public Set<String> requiredMods() { return Set.of("tweakeroo"); }

    public void initialize(ExtensionContext context) throws Exception {
        String root = "fi.dy.masa.tweakeroo.config.Configs";
        ReflectedConfig config = ReflectedConfig.scan(root, "fi.dy.masa.tweakeroo.config.FeatureToggle");
        MasaCommandSupport.register(context, "tweakeroo", root, config);
        config.registerBooleanToggles(context.integrations(), "tweakeroo", "tweakeroo.");
    }
}
