package dev.clientbot.extension.printer;

import dev.clientbot.api.ClientBotExtension;
import dev.clientbot.api.ExtensionContext;
import dev.clientbot.command.BotCommand;
import dev.clientbot.util.MasaCommandSupport;
import dev.clientbot.util.ReflectedConfig;
import me.aleksilassila.litematica.printer.config.Configs;

import java.util.Locale;
import java.util.Set;

public final class PrinterExtension implements ClientBotExtension {
    public String id() { return "litematica-printer"; }
    public Set<String> requiredMods() { return Set.of("malilib", "litematica", "litematica_printer"); }

    public void initialize(ExtensionContext context) throws Exception {
        ReflectedConfig config = ReflectedConfig.scan("me.aleksilassila.litematica.printer.config.Configs");
        MasaCommandSupport.register(context, "printercfg", "fi.dy.masa.litematica.config.Configs", config);
        config.registerBooleanToggles(context.integrations(), "litematica_printer", "printer.");

        context.commands().register(BotCommand.builder("printer")
                .usage("start | stop | toggle | status | range <2.5-5> | interval <1-40> | rotate <on/off>")
                .description("控制 Litematica Printer 自动打印")
                .permission("extensions.printer")
                .handler(ctx -> {
                    String action = ctx.arg(0).toLowerCase(Locale.ROOT);
                    switch (action) {
                        case "start" -> setPrinting(true);
                        case "stop" -> setPrinting(false);
                        case "toggle" -> setPrinting(!Configs.PRINT_MODE.getBooleanValue());
                        case "range" -> Configs.PRINTING_RANGE.setDoubleValue(ctx.doubleArg(1));
                        case "interval" -> Configs.PRINTING_INTERVAL.setIntegerValue(ctx.intArg(1));
                        case "rotate" -> Configs.ROTATE.setBooleanValue(ctx.booleanArg(1));
                        case "status" -> {}
                        default -> throw new IllegalArgumentException("printer start|stop|toggle|status|range|interval|rotate");
                    }
                    ctx.reply(String.format("printing=%s, range=%.1f, interval=%d, rotate=%s, interact=%s",
                            Configs.PRINT_MODE.getBooleanValue(), Configs.PRINTING_RANGE.getDoubleValue(),
                            Configs.PRINTING_INTERVAL.getIntegerValue(), Configs.ROTATE.getBooleanValue(),
                            Configs.INTERACT_BLOCKS.getBooleanValue()));
                }).build());
        context.integrations().register("printer.printing", "litematica_printer", "自动打印原理图",
                Configs.PRINT_MODE::getBooleanValue, PrinterExtension::setPrinting);
    }

    private static void setPrinting(boolean value) {
        Configs.PRINT_MODE.setBooleanValue(value);
    }
}
