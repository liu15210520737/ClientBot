package dev.clientbot.extension.quickshulker;

import dev.clientbot.api.ClientBotExtension;
import dev.clientbot.api.ExtensionContext;
import dev.clientbot.command.BotCommand;
import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.kyrptonaught.quickshulker.client.ClientUtil;
import net.kyrptonaught.quickshulker.config.ConfigOptions;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.util.Set;

public final class QuickShulkerExtension implements ClientBotExtension {
    public String id() { return "quickshulker"; }
    public Set<String> requiredMods() { return Set.of("quickshulker"); }

    public void initialize(ExtensionContext context) {
        context.commands().register(BotCommand.builder("qshulker")
                .usage("open <背包槽0-35/offhand> | close | list | get <配置> | set <配置> <on/off>")
                .description("通过 QuickShulker 协议打开物品容器并管理其配置")
                .permission("extensions.quickshulker")
                .handler(ctx -> {
                    switch (ctx.arg(0)) {
                        case "open" -> {
                            String slotText = ctx.arg(1);
                            int inventorySlot;
                            int menuSlot;
                            if (slotText.equalsIgnoreCase("offhand")) {
                                inventorySlot = 40;
                                menuSlot = 45;
                            } else {
                                inventorySlot = Integer.parseInt(slotText);
                                if (inventorySlot < 0 || inventorySlot > 35) throw new IllegalArgumentException("背包槽范围为 0-35，或 offhand");
                                menuSlot = inventorySlot < 9 ? 36 + inventorySlot : inventorySlot;
                            }
                            ItemStack stack = ctx.player().getInventory().getItem(inventorySlot);
                            if (!ClientUtil.CheckAndSend(stack, menuSlot)) {
                                throw new IllegalArgumentException("槽位物品不可被 QuickShulker 打开: " + stack.getHoverName().getString());
                            }
                            ctx.reply("已请求打开槽位 " + slotText + ": " + stack.getHoverName().getString());
                        }
                        case "close" -> { ctx.player().closeContainer(); ctx.reply("已关闭容器"); }
                        case "list" -> {
                            for (Field field : ConfigOptions.class.getFields()) {
                                if (field.getType() == boolean.class) ctx.reply(field.getName() + " = " + field.get(QuickShulkerMod.getConfig()));
                            }
                        }
                        case "get" -> ctx.reply(ctx.arg(1) + " = " + booleanField(ctx.arg(1)).get(QuickShulkerMod.getConfig()));
                        case "set" -> {
                            Field field = booleanField(ctx.arg(1));
                            field.setBoolean(QuickShulkerMod.getConfig(), ctx.booleanArg(2));
                            QuickShulkerMod.config.save();
                            ctx.reply(field.getName() + " -> " + field.getBoolean(QuickShulkerMod.getConfig()));
                        }
                        default -> throw new IllegalArgumentException("qshulker open|close|list|get|set");
                    }
                }).build());
        context.integrations().register("quickshulker.rightClickToOpen", "quickshulker", "手持右键快速打开",
                () -> QuickShulkerMod.getConfig().rightClickToOpen, value -> {
                    QuickShulkerMod.getConfig().rightClickToOpen = value;
                    QuickShulkerMod.config.save();
                });
        context.integrations().register("quickshulker.keybind", "quickshulker", "快捷键快速打开",
                () -> QuickShulkerMod.getConfig().keybind, value -> {
                    QuickShulkerMod.getConfig().keybind = value;
                    QuickShulkerMod.config.save();
                });
    }

    private static Field booleanField(String name) throws NoSuchFieldException {
        Field field = ConfigOptions.class.getField(name);
        if (field.getType() != boolean.class) throw new IllegalArgumentException(name + " 不是布尔配置");
        return field;
    }
}
