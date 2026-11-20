package dev.clientbot.extension.carpet;

import dev.clientbot.api.ClientBotExtension;
import dev.clientbot.api.ExtensionContext;
import dev.clientbot.command.BotCommand;
import dev.clientbot.util.JsonFiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Carpet is server-side, so this extension needs no client-side Carpet installation. */
public final class CarpetExtension implements ClientBotExtension {
    public String id() { return "carpet"; }

    public void initialize(ExtensionContext context) {
        Config config = JsonFiles.load(context.configDirectory().resolve("carpet.json"), Config.class, new Config());
        context.commands().register(BotCommand.builder("carpet")
                .usage("status | query <规则> | log <logger> [参数]")
                .description("查询 Carpet 规则、订阅日志与协议状态")
                .permission("extensions.carpet")
                .handler(ctx -> {
                    switch (ctx.arg(0)) {
                        case "status" -> ctx.reply(detectCarpet());
                        case "query" -> { send(ctx, "carpet " + ctx.arg(1), config); ctx.reply("已查询 Carpet 规则 " + ctx.arg(1)); }
                        case "log" -> { send(ctx, "log " + ctx.joined(1), config); ctx.reply("已发送 /log 订阅"); }
                        default -> throw new IllegalArgumentException("carpet status|query|log；修改请用 carpetset");
                    }
                }).build());
        context.commands().register(BotCommand.builder("carpetset")
                .usage("rule <规则> <值> | player <假人名> <动作...> | tick <参数...>")
                .description("修改 Carpet 规则、控制假人或 tick（危险操作）")
                .permission("extensions.carpet.admin").dangerous()
                .handler(ctx -> {
                    String type = ctx.arg(0);
                    String command;
                    switch (type) {
                        case "rule" -> {
                            String rule = ctx.arg(1);
                            requireAllowed(rule, config.allowedRules, "规则");
                            command = "carpet " + rule + " " + ctx.joined(2);
                        }
                        case "player" -> {
                            String player = ctx.arg(1);
                            requireAllowed(player, config.allowedFakePlayers, "假人");
                            command = "player " + player + " " + ctx.joined(2);
                        }
                        case "tick" -> {
                            if (!config.allowTickCommands) throw new IllegalArgumentException("carpet.json 已禁止 tick 命令");
                            command = "tick " + ctx.joined(1);
                        }
                        default -> throw new IllegalArgumentException("carpetset rule|player|tick");
                    }
                    send(ctx, command, config);
                    ctx.reply("已执行 /" + command);
                }).build());
    }

    private static void send(dev.clientbot.command.CommandContext ctx, String command, Config config) {
        if (command.length() > config.maxCommandLength || command.contains("\n") || command.contains("\r")) {
            throw new IllegalArgumentException("命令过长或含换行");
        }
        ctx.player().connection.sendCommand(command);
    }

    private static void requireAllowed(String value, List<String> allowed, String type) {
        boolean matches = allowed.isEmpty() || allowed.stream().anyMatch(candidate -> {
            if (candidate.endsWith("*")) {
                return value.toLowerCase(Locale.ROOT).startsWith(candidate.substring(0, candidate.length() - 1).toLowerCase(Locale.ROOT));
            }
            return value.equalsIgnoreCase(candidate);
        });
        if (!matches) throw new IllegalArgumentException(type + " 不在 carpet.json 白名单中: " + value);
    }

    private static String detectCarpet() {
        for (String className : new String[]{"fi.dy.masa.minihud.util.DataStorage", "fi.dy.masa.tweakeroo.data.DataManager"}) {
            try {
                Object instance = Class.forName(className).getMethod("getInstance").invoke(null);
                boolean carpet = (boolean) instance.getClass().getMethod("hasCarpetServer").invoke(instance);
                return className + " 报告 Carpet=" + carpet;
            } catch (Throwable ignored) {}
        }
        return "未安装 MiniHUD/Tweakeroo，无法读取 Carpet 握手状态；命令仍可发送";
    }

    public static final class Config {
        public List<String> allowedRules = new ArrayList<>();
        public List<String> allowedFakePlayers = new ArrayList<>(List.of("bot_*"));
        public boolean allowTickCommands = false;
        public int maxCommandLength = 200;
    }
}
