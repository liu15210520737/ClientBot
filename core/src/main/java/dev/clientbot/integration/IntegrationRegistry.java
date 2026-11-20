package dev.clientbot.integration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Registry for toggles owned by other mods. Core can list and switch them uniformly. */
public final class IntegrationRegistry {
    private final Map<String, Toggle> toggles = new LinkedHashMap<>();

    public synchronized void register(String id, String ownerMod, String description,
                                      BooleanSupplier getter, Consumer<Boolean> setter) {
        toggles.putIfAbsent(id, new Toggle(id, ownerMod, description, getter, setter));
    }

    public synchronized Collection<Toggle> all() {
        return List.copyOf(toggles.values());
    }

    public record Toggle(String id, String ownerMod, String description,
                         BooleanSupplier getter, Consumer<Boolean> setter) {}
}
