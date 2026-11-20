package dev.clientbot.example;

import dev.clientbot.api.ClientBotExtension;
import dev.clientbot.api.ExtensionContext;
import dev.clientbot.command.BotCommand;

/** Minimal extension used as the starting point in docs/EXTENSION_API.md. */
public final class ExampleExtension implements ClientBotExtension {
    @Override
    public String id() {
        return "example";
    }

    @Override
    public void initialize(ExtensionContext context) {
        context.commands().register(BotCommand.builder("example")
                .usage("[文本]")
                .description("扩展开发示例：返回参数和当前位置")
                .permission("extensions.example")
                .handler(ctx -> ctx.reply("示例扩展收到: " + ctx.joined(0) + "；位置=" + ctx.player().blockPosition().toShortString()))
                .build());
        context.integrations().register("example.enabled", "clientbot-example", "示例开关",
                () -> true, value -> { /* 替换为自己的 Mod 配置 setter */ });
    }
}
