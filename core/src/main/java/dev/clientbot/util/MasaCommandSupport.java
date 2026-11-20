package dev.clientbot.util;

import com.google.gson.JsonPrimitive;
import dev.clientbot.api.ExtensionContext;
import dev.clientbot.command.BotCommand;

import java.util.List;
import java.util.Locale;

/** Shared implementation for extensions exposing a MaLiLib configuration tree. */
public final class MasaCommandSupport {
    private MasaCommandSupport() {}

    public static void register(ExtensionContext context, String commandName, String rootClass,
                                ReflectedConfig config) {
        context.commands().register(BotCommand.builder(commandName)
                .usage("list [过滤] | get <名称> | set <名称> <JSON值> | toggle <名称> | save")
                .description("枚举和操作 " + commandName + " 的全部公开 MaLiLib 配置")
                .permission("extensions." + commandName)
                .handler(ctx -> {
                    String action = ctx.args().isEmpty() ? "status" : ctx.arg(0).toLowerCase(Locale.ROOT);
                    switch (action) {
                        case "status" -> ctx.reply(commandName + ": " + config.list().size() + " 个公开配置项");
                        case "list" -> {
                            String filter = ctx.args().size() > 1 ? ctx.arg(1).toLowerCase(Locale.ROOT) : "";
                            List<ReflectedConfig.Option> matches = config.list().stream()
                                    .filter(o -> o.name().toLowerCase(Locale.ROOT).contains(filter)).toList();
                            matches.stream().limit(30).forEach(o -> ctx.reply(o.name() + " = " + o.value()));
                            ctx.reply("匹配 " + matches.size() + " 项" + (matches.size() > 30 ? "，仅显示前 30 项；请加过滤词" : ""));
                        }
                        case "get" -> ctx.reply(config.get(ctx.arg(1)).name() + " = " + config.get(ctx.arg(1)).value());
                        case "set" -> {
                            config.get(ctx.arg(1)).setFromText(ctx.joined(2));
                            ctx.reply(config.get(ctx.arg(1)).name() + " -> " + config.get(ctx.arg(1)).value());
                        }
                        case "toggle" -> {
                            ReflectedConfig.Option option = config.get(ctx.arg(1));
                            if (!(option.value() instanceof JsonPrimitive primitive) || !primitive.isBoolean()) {
                                throw new IllegalArgumentException(option.name() + " 不是布尔配置；请使用 set");
                            }
                            option.set(new JsonPrimitive(!primitive.getAsBoolean()));
                            ctx.reply(option.name() + " -> " + option.value());
                        }
                        case "save" -> {
                            config.save(rootClass);
                            ctx.reply("已调用 " + commandName + " 的配置保存");
                        }
                        default -> throw new IllegalArgumentException("用法: " + commandName + " list|get|set|toggle|save");
                    }
                }).build());
    }
}
