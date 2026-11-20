package dev.clientbot.core;

import dev.clientbot.api.ClientBotExtension;
import dev.clientbot.api.ExtensionContext;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class ExtensionManager {
    private final List<Status> statuses = new ArrayList<>();

    public void load(ExtensionContext context) {
        for (EntrypointContainer<ClientBotExtension> container : FabricLoader.getInstance()
                .getEntrypointContainers("clientbot", ClientBotExtension.class)) {
            ModMetadata metadata = container.getProvider().getMetadata();
            String providerId = metadata.getId();
            try {
                Set<String> declared = declaredRequirements(metadata);
                Set<String> missing = missingMods(declared, context);
                if (!missing.isEmpty()) {
                    statuses.add(new Status(providerId, false, "缺少: " + String.join(", ", missing)));
                    continue;
                }

                // Declared requirements are checked before this call, so optional third-party
                // classes in the entrypoint's method bodies are never resolved prematurely.
                ClientBotExtension extension = container.getEntrypoint();
                missing = missingMods(extension.requiredMods(), context);
                if (!missing.isEmpty()) {
                    statuses.add(new Status(extension.id(), false, "缺少: " + String.join(", ", missing)));
                    continue;
                }
                extension.initialize(context);
                statuses.add(new Status(extension.id(), true, "已加载"));
            } catch (Throwable error) {
                ClientBotMod.LOGGER.error("扩展 {} 加载失败；核心与其他扩展继续运行", providerId, error);
                statuses.add(new Status(providerId, false, error.getClass().getSimpleName() + ": " + error.getMessage()));
            }
        }
    }

    private static Set<String> declaredRequirements(ModMetadata metadata) {
        Set<String> result = new TreeSet<>();
        if (!metadata.containsCustomValue("clientbot:requires")) return result;
        CustomValue value = metadata.getCustomValue("clientbot:requires");
        if (value.getType() != CustomValue.CvType.ARRAY) return result;
        for (CustomValue entry : value.getAsArray()) {
            if (entry.getType() == CustomValue.CvType.STRING) result.add(entry.getAsString());
        }
        return result;
    }

    private static Set<String> missingMods(Set<String> required, ExtensionContext context) {
        Set<String> missing = new TreeSet<>();
        for (String mod : required) {
            if (!context.isModLoaded(mod)) missing.add(mod);
        }
        return missing;
    }

    public List<Status> statuses() {
        return List.copyOf(statuses);
    }

    public record Status(String id, boolean loaded, String detail) {}
}
