package dev.clientbot.extension.litematica;

import com.google.gson.JsonPrimitive;
import dev.clientbot.api.ClientBotExtension;
import dev.clientbot.api.ExtensionContext;
import dev.clientbot.command.BotCommand;
import dev.clientbot.util.MasaCommandSupport;
import dev.clientbot.util.ReflectedConfig;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.data.SchematicHolder;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;

import java.nio.file.Path;
import java.util.Set;

public final class LitematicaExtension implements ClientBotExtension {
    public String id() { return "litematica"; }
    public Set<String> requiredMods() { return Set.of("malilib", "litematica"); }

    public void initialize(ExtensionContext context) throws Exception {
        String root = "fi.dy.masa.litematica.config.Configs";
        ReflectedConfig config = ReflectedConfig.scan(root);
        MasaCommandSupport.register(context, "litematicacfg", root, config);
        config.registerBooleanToggles(context.integrations(), "litematica", "litematica.");

        context.commands().register(BotCommand.builder("litematica")
                .usage("load <文件> [x y z] | list | unload [名称] | easyplace <on/off> | render <on/off>")
                .description("加载/卸载投影、EasyPlace 和投影渲染")
                .permission("extensions.litematica")
                .handler(ctx -> {
                    SchematicPlacementManager manager = DataManager.getSchematicPlacementManager();
                    switch (ctx.arg(0)) {
                        case "load" -> {
                            Path base = FabricLoader.getInstance().getGameDir().resolve("schematics").toAbsolutePath().normalize();
                            Path file = base.resolve(ctx.arg(1)).normalize();
                            if (!file.startsWith(base)) throw new IllegalArgumentException("原理图必须位于 .minecraft/schematics 内");
                            LitematicaSchematic schematic = LitematicaSchematic.createFromFile(file.getParent(), file.getFileName().toString());
                            if (schematic == null) throw new IllegalArgumentException("无法读取 .litematic: " + file.getFileName());
                            SchematicHolder.getInstance().addSchematic(schematic, true);
                            BlockPos origin = ctx.args().size() >= 5
                                    ? new BlockPos(ctx.intArg(2), ctx.intArg(3), ctx.intArg(4))
                                    : ctx.player().blockPosition();
                            SchematicPlacement placement = SchematicPlacement.createFor(schematic, origin,
                                    schematic.getMetadata().getName(), true, true);
                            manager.addSchematicPlacement(placement, true);
                            manager.setSelectedSchematicPlacement(placement);
                            ctx.reply("已加载 " + placement.getName() + " @ " + origin.toShortString());
                        }
                        case "list" -> {
                            for (SchematicPlacement placement : manager.getAllSchematicsPlacements()) {
                                ctx.reply(placement.getName() + " @ " + placement.getOrigin().toShortString()
                                        + (placement == manager.getSelectedSchematicPlacement() ? " [selected]" : ""));
                            }
                            ctx.reply("共 " + manager.getAllSchematicsPlacements().size() + " 个放置");
                        }
                        case "unload" -> {
                            SchematicPlacement placement = ctx.args().size() > 1
                                    ? manager.getAllSchematicsPlacements().stream().filter(p -> p.getName().equals(ctx.arg(1))).findFirst().orElse(null)
                                    : manager.getSelectedSchematicPlacement();
                            if (placement == null) throw new IllegalArgumentException("没有匹配的投影放置");
                            manager.removeSchematicPlacement(placement);
                            ctx.reply("已卸载 " + placement.getName());
                        }
                        case "easyplace" -> {
                            config.get("easyPlaceMode").set(new JsonPrimitive(ctx.booleanArg(1)));
                            config.save(root);
                            ctx.reply("easyPlaceMode -> " + config.get("easyPlaceMode").value());
                        }
                        case "render" -> {
                            config.get("enableRendering").set(new JsonPrimitive(ctx.booleanArg(1)));
                            config.save(root);
                            ctx.reply("enableRendering -> " + config.get("enableRendering").value());
                        }
                        default -> throw new IllegalArgumentException("litematica load|list|unload|easyplace|render");
                    }
                }).build());
    }
}
