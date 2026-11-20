package dev.clientbot.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandRegistryTest {
    @Test
    void lookupIsCaseInsensitiveAndNamesAreUnique() {
        CommandRegistry registry = new CommandRegistry();
        registry.register(command("Ping"));
        assertTrue(registry.find("PING").isPresent());
        assertEquals(1, registry.size());
        assertThrows(IllegalArgumentException.class, () -> registry.register(command("ping")));
    }

    @Test
    void commandRequiresHandler() {
        assertThrows(IllegalStateException.class, () -> BotCommand.builder("broken").build());
    }

    private static BotCommand command(String name) {
        return BotCommand.builder(name).handler(ctx -> {}).build();
    }
}
