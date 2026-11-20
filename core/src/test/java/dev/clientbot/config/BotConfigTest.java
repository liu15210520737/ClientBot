package dev.clientbot.config;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BotConfigTest {
    @Test
    void remoteControlIsDisabledAndOperatorsAreEmptyByDefault() {
        BotConfig config = new BotConfig();
        assertFalse(config.enabled);
        assertTrue(config.operators.isEmpty());
        assertFalse(config.allowSystemWhispers);
    }

    @Test
    void permissionsSupportExactNodesAndScopedWildcards() {
        BotConfig.Operator operator = new BotConfig.Operator(UUID.randomUUID(), "Tester",
                List.of("core.status", "extensions.carpet.*"));
        assertTrue(operator.permits("core.status"));
        assertTrue(operator.permits("CORE.STATUS"));
        assertTrue(operator.permits("extensions.carpet.admin"));
        assertFalse(operator.permits("extensions.carpet"));
        assertFalse(operator.permits("extensions.carpet-admin"));
        assertFalse(operator.permits("core.command"));
    }

    @Test
    void globalWildcardIsExplicit() {
        BotConfig.Operator admin = new BotConfig.Operator(UUID.randomUUID(), "Admin", List.of("*"));
        BotConfig.Operator user = new BotConfig.Operator(UUID.randomUUID(), "User", List.of("core.*"));
        assertTrue(admin.permits("extensions.carpet.admin"));
        assertFalse(user.permits("extensions.carpet.admin"));
        assertFalse(BotConfig.defaultUserPermissions().contains("extensions.*"));
        assertFalse(BotConfig.defaultUserPermissions().contains("*"));
    }
}
