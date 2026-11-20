package dev.clientbot.command;

import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class CommandRegistry {
    private final Map<String, BotCommand> commands = new LinkedHashMap<>();

    public synchronized void register(BotCommand command) {
        String key = command.name().toLowerCase(Locale.ROOT);
        if (commands.putIfAbsent(key, command) != null) {
            throw new IllegalArgumentException("指令已注册: " + key);
        }
    }

    public synchronized Optional<BotCommand> find(String name) {
        return Optional.ofNullable(commands.get(name.toLowerCase(Locale.ROOT)));
    }

    public synchronized Collection<BotCommand> all() {
        return List.copyOf(commands.values());
    }

    public synchronized int size() {
        return commands.size();
    }
}
