package dev.clientbot.command;

import dev.clientbot.config.BotConfig;
import dev.clientbot.config.ConfigManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CommandDispatcherTest {
    @TempDir
    Path tempDir;

    @Test
    void tokenizesWhitespaceAndQuotedArguments() {
        assertEquals(List.of("say", "hello world", "two words", "plain"),
                CommandDispatcher.tokenize("say \"hello world\" 'two words' plain"));
        assertEquals(List.of("say", "a'b"), CommandDispatcher.tokenize("say \"a'b\""));
        assertThrows(IllegalArgumentException.class, () -> CommandDispatcher.tokenize("say \"unclosed"));
    }

    @Test
    void dangerousCommandRequiresMatchingOneTimeConfirmation() throws Exception {
        UUID uuid = UUID.randomUUID();
        AtomicInteger executions = new AtomicInteger();
        Fixture fixture = new Fixture(tempDir.resolve("core.json"), uuid, List.of("*"));
        fixture.registry.register(BotCommand.builder("delete").permission("test.delete").dangerous()
                .handler(ctx -> executions.incrementAndGet()).build());

        fixture.dispatcher.handleRemote(uuid, "Remote", "!cb delete");
        assertEquals(0, executions.get(), "危险命令不能直接执行");

        String code = fixture.onlyPendingCode();
        assertTrue(code.matches("[0-9]{6}"));

        UUID other = UUID.randomUUID();
        fixture.config.operators.add(new BotConfig.Operator(other, "Other", List.of("*")));
        fixture.dispatcher.handleRemote(other, "Other", "!cb confirm " + code);
        assertEquals(0, executions.get(), "其他 UUID 不能使用确认码");
        assertEquals(code, fixture.onlyPendingCode(), "其他 UUID 的尝试不能消费确认码");

        fixture.dispatcher.handleRemote(uuid, "Remote", "!cb confirm " + code);
        assertEquals(1, executions.get());

        fixture.dispatcher.handleRemote(uuid, "Remote", "!cb confirm " + code);
        assertEquals(1, executions.get(), "确认码只能使用一次");
    }

    @Test
    void permissionIsCheckedAgainWhenConfirmationIsUsed() throws Exception {
        UUID uuid = UUID.randomUUID();
        AtomicInteger executions = new AtomicInteger();
        Fixture fixture = new Fixture(tempDir.resolve("core.json"), uuid, List.of("test.delete"));
        fixture.registry.register(BotCommand.builder("delete").permission("test.delete").dangerous()
                .handler(ctx -> executions.incrementAndGet()).build());

        fixture.dispatcher.handleRemote(uuid, "Remote", "!cb delete");
        String code = fixture.onlyPendingCode();
        fixture.config.operators.clear();
        fixture.dispatcher.handleRemote(uuid, "Remote", "!cb confirm " + code);
        assertEquals(0, executions.get(), "确认时必须重新检查当前权限");
    }

    @Test
    void disabledRemoteControlAndMissingPrefixAreIgnored() {
        UUID uuid = UUID.randomUUID();
        AtomicInteger executions = new AtomicInteger();
        Fixture fixture = new Fixture(tempDir.resolve("core.json"), uuid, List.of("*"));
        fixture.registry.register(BotCommand.builder("ping").permission("test.ping")
                .handler(ctx -> executions.incrementAndGet()).build());

        fixture.config.enabled = false;
        fixture.dispatcher.handleRemote(uuid, "Remote", "!cb ping");
        fixture.config.enabled = true;
        fixture.dispatcher.handleRemote(uuid, "Remote", "ordinary chat");
        assertEquals(0, executions.get());

        fixture.dispatcher.handleRemote(uuid, "Remote", "!cb ping");
        assertEquals(1, executions.get());
    }

    private static final class Fixture {
        final CommandRegistry registry = new CommandRegistry();
        final ConfigManager configs;
        final BotConfig config;
        final CommandDispatcher dispatcher;

        Fixture(Path file, UUID uuid, List<String> permissions) {
            configs = new ConfigManager(file);
            config = configs.get();
            config.enabled = true;
            config.auditLog = false;
            config.replyMode = "off";
            config.operators.add(new BotConfig.Operator(uuid, "Remote", permissions));
            ResponseRouter responses = new ResponseRouter(null, config);
            dispatcher = new CommandDispatcher(null, configs, registry, responses);
        }

        String onlyPendingCode() throws Exception {
            Field field = CommandDispatcher.class.getDeclaredField("pending");
            field.setAccessible(true);
            Map<?, ?> pending = (Map<?, ?>) field.get(dispatcher);
            assertEquals(1, pending.size());
            return (String) pending.keySet().iterator().next();
        }
    }
}
