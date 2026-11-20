package dev.clientbot.command;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CommandContextTest {
    private final List<String> replies = new ArrayList<>();

    @Test
    void parsesTypedArgumentsAndJoinedText() {
        CommandContext context = context(List.of("12", "3.5", "YES", "hello", "world"));
        assertEquals("12", context.arg(0));
        assertEquals(12, context.intArg(0));
        assertEquals(3.5, context.doubleArg(1));
        assertTrue(context.booleanArg(2));
        assertEquals("YES hello world", context.joined(2));
        assertThrows(IllegalArgumentException.class, () -> context.arg(9));
    }

    @Test
    void acceptsDocumentedBooleanValuesOnly() {
        for (String value : List.of("on", "true", "yes", "1", "开")) {
            assertTrue(context(List.of(value)).booleanArg(0));
        }
        for (String value : List.of("off", "false", "no", "0", "关")) {
            assertFalse(context(List.of(value)).booleanArg(0));
        }
        assertThrows(IllegalArgumentException.class, () -> context(List.of("maybe")).booleanArg(0));
    }

    @Test
    void replyUsesSenderSpecificOutput() {
        CommandContext context = context(List.of());
        context.reply("done");
        assertEquals(List.of("done"), replies);
    }

    @Test
    void playerIsRequiredOnlyWhenCommandNeedsIt() {
        CommandContext context = context(List.of());
        IllegalStateException error = assertThrows(IllegalStateException.class, context::player);
        assertEquals("当前不在世界中", error.getMessage());
    }

    private CommandContext context(List<String> args) {
        return new CommandContext(null,
                new CommandSender(UUID.randomUUID(), "Tester", false), args, replies::add, "test");
    }
}
