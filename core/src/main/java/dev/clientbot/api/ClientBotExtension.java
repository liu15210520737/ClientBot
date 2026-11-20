package dev.clientbot.api;

import java.util.Set;

/**
 * Entry point for a ClientBot extension. Declare the implementation in fabric.mod.json
 * under the "clientbot" entry point. Extensions are loaded after the core registry exists.
 */
public interface ClientBotExtension {
    String id();

    /** Mod IDs that must be present before this extension is initialized. */
    default Set<String> requiredMods() {
        return Set.of();
    }

    void initialize(ExtensionContext context) throws Exception;
}
