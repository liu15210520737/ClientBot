package dev.clientbot.command;

import dev.clientbot.config.BotConfig;
import dev.clientbot.config.ConfigManager;
import dev.clientbot.core.ClientBotMod;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommandDispatcher {
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private final Minecraft client;
    private final ConfigManager configs;
    private final CommandRegistry registry;
    private final ResponseRouter responses;
    private final Map<UUID, Deque<Long>> rateLimits = new HashMap<>();
    private final Map<String, Pending> pending = new HashMap<>();
    private final SecureRandom random = new SecureRandom();

    public CommandDispatcher(Minecraft client, ConfigManager configs, CommandRegistry registry, ResponseRouter responses) {
        this.client = client;
        this.configs = configs;
        this.registry = registry;
        this.responses = responses;
    }

    public void handleRemote(UUID uuid, String name, String text) {
        BotConfig config = configs.get();
        if (!config.enabled) return;
        int prefix = text.indexOf(config.commandPrefix);
        if (prefix < 0) return;
        String raw = text.substring(prefix + config.commandPrefix.length()).trim();
        if (raw.isEmpty()) return;
        BotConfig.Operator operator = findOperator(uuid);
        if (operator == null || !allowRate(uuid)) return;
        execute(new CommandSender(uuid, name, false), raw, operator, false, 0);
    }

    public void executeLocal(String raw) {
        execute(CommandSender.localSender(), raw, null, true, 0);
    }

    private void execute(CommandSender sender, String raw, BotConfig.Operator operator,
                         boolean confirmed, int depth) {
        List<String> tokens = tokenize(raw);
        if (tokens.isEmpty() || depth > 4) return;
        String name = tokens.remove(0).toLowerCase(Locale.ROOT);
        if (name.equals("confirm")) {
            confirm(sender, tokens.isEmpty() ? "" : tokens.get(0));
            return;
        }
        registry.find(name).ifPresentOrElse(command -> {
            if (!sender.local() && (operator == null || !operator.permits(command.permission()))) {
                responses.forSender(sender, "无权限: " + command.permission());
                audit(sender, raw, "denied");
                return;
            }
            if (command.dangerous() && !sender.local() && !confirmed) {
                String code = String.format("%06d", random.nextInt(1_000_000));
                pending.put(code, new Pending(sender.uuid(), raw, System.currentTimeMillis() + configs.get().confirmationSeconds * 1000L));
                responses.forSender(sender, "危险操作需确认：" + configs.get().commandPrefix + " confirm " + code + "（原始指令：" + raw + "）");
                return;
            }
            CommandContext context = new CommandContext(client, sender, tokens,
                    message -> responses.forSender(sender, message), raw);
            try {
                command.handler().execute(context);
                audit(sender, raw, "ok");
            } catch (Exception e) {
                String message = e.getCause() != null && e.getCause().getMessage() != null
                        ? e.getCause().getMessage() : e.getMessage();
                responses.forSender(sender, "执行失败: " + (message == null ? e.getClass().getSimpleName() : message));
                ClientBotMod.LOGGER.warn("指令执行失败: {}", raw, e);
                audit(sender, raw, "error");
            }
        }, () -> responses.forSender(sender, "未知指令 " + name + "；使用 " + configs.get().commandPrefix + " help"));
    }

    public int commandCount() {
        return registry.size();
    }

    private void confirm(CommandSender sender, String code) {
        Pending value = pending.get(code);
        if (value == null || value.expires() < System.currentTimeMillis() || !value.sender().equals(sender.uuid())) {
            if (value != null && value.expires() < System.currentTimeMillis()) pending.remove(code, value);
            responses.forSender(sender, "确认码无效、已过期或不属于当前操作者");
            return;
        }
        pending.remove(code, value);
        BotConfig.Operator operator = sender.local() ? null : findOperator(sender.uuid());
        execute(sender, value.raw(), operator, true, 0);
    }

    private BotConfig.Operator findOperator(UUID uuid) {
        for (BotConfig.Operator operator : configs.get().operators) {
            try {
                if (operator.uuid().equals(uuid)) return operator;
            } catch (IllegalArgumentException e) {
                ClientBotMod.LOGGER.warn("忽略无效操作者 UUID: {}", operator.uuid);
            }
        }
        return null;
    }

    private boolean allowRate(UUID uuid) {
        long now = System.currentTimeMillis();
        Deque<Long> history = rateLimits.computeIfAbsent(uuid, ignored -> new ArrayDeque<>());
        while (!history.isEmpty() && history.peekFirst() < now - 60_000) history.removeFirst();
        if (history.size() >= configs.get().maxCommandsPerMinute) return false;
        history.addLast(now);
        return true;
    }

    private void audit(CommandSender sender, String raw, String result) {
        if (!configs.get().auditLog) return;
        try {
            Path file = configs.get().operators == null ? null : net.fabricmc.loader.api.FabricLoader.getInstance()
                    .getConfigDir().resolve("clientbot").resolve("audit.log");
            if (file != null) {
                Files.createDirectories(file.getParent());
                String line = Instant.now() + " " + sender.uuid() + " " + sender.name() + " " + result + " " + raw + System.lineSeparator();
                Files.writeString(file, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            ClientBotMod.LOGGER.warn("审计日志写入失败", e);
        }
    }

    public static List<String> tokenize(String raw) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        char quote = 0;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if ((c == '\'' || c == '"')) {
                if (quote == 0) quote = c;
                else if (quote == c) quote = 0;
                else current.append(c);
            } else if (Character.isWhitespace(c) && quote == 0) {
                if (!current.isEmpty()) { result.add(current.toString()); current.setLength(0); }
            } else current.append(c);
        }
        if (quote != 0) throw new IllegalArgumentException("引号未闭合");
        if (!current.isEmpty()) result.add(current.toString());
        return result;
    }

    private record Pending(UUID sender, String raw, long expires) {}
}
