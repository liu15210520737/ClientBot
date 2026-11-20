package dev.clientbot.extension.servux;

import com.google.gson.JsonPrimitive;
import dev.clientbot.api.ClientBotExtension;
import dev.clientbot.api.ExtensionContext;
import dev.clientbot.command.BotCommand;
import dev.clientbot.util.ReflectedConfig;

import java.util.Set;

/** Uses MiniHUD's client-side Servux protocol state; Servux itself belongs on the server. */
public final class ServuxExtension implements ClientBotExtension {
    public String id() { return "servux"; }
    public Set<String> requiredMods() { return Set.of("minihud"); }

    public void initialize(ExtensionContext context) throws Exception {
        String configRoot = "fi.dy.masa.minihud.config.Configs";
        ReflectedConfig configs = ReflectedConfig.scan(configRoot);
        context.commands().register(BotCommand.builder("servux")
                .usage("status | sync <hud/entity> <on/off> | refresh <hud/structures/recipes> | save")
                .description("读取 Servux 状态并控制 MiniHUD 数据同步")
                .permission("extensions.servux")
                .handler(ctx -> {
                    switch (ctx.arg(0)) {
                        case "status" -> {
                            Object storage = invokeStatic("fi.dy.masa.minihud.util.DataStorage", "getInstance");
                            boolean present = (boolean) invoke(storage, "hasServuxServer");
                            ctx.reply("Servux=" + present + (present ? ", version=" + invoke(storage, "getServuxVersion") : ""));
                            ctx.reply("TPS=" + invoke(storage, "getServerTPS") + ", MSPT=" + invoke(storage, "getServerMSPT")
                                    + ", hudDataSync=" + configs.get("hudDataSync").value()
                                    + ", entityDataSync=" + configs.get("entityDataSync").value());
                            Object hud = invokeStatic("fi.dy.masa.minihud.data.HudDataManager", "getInstance");
                            ctx.reply("spawn=" + invoke(hud, "getWorldSpawnAsString") + ", spawnChunks=" + invoke(hud, "getSpawnChunkRadius")
                                    + ", seedKnown=" + invoke(hud, "hasStoredWorldSeed"));
                        }
                        case "sync" -> {
                            String option = switch (ctx.arg(1)) {
                                case "hud" -> "hudDataSync";
                                case "entity" -> "entityDataSync";
                                default -> throw new IllegalArgumentException("sync hud|entity");
                            };
                            configs.get(option).set(new JsonPrimitive(ctx.booleanArg(2)));
                            configs.save(configRoot);
                            ctx.reply(option + " -> " + configs.get(option).value());
                        }
                        case "refresh" -> {
                            switch (ctx.arg(1)) {
                                case "hud" -> invoke(invokeStatic("fi.dy.masa.minihud.data.HudDataManager", "getInstance"), "requestMetadata");
                                case "recipes" -> invoke(invokeStatic("fi.dy.masa.minihud.data.HudDataManager", "getInstance"), "requestRecipeManager");
                                case "structures" -> invoke(invokeStatic("fi.dy.masa.minihud.util.DataStorage", "getInstance"), "setStructuresNeedUpdating");
                                default -> throw new IllegalArgumentException("refresh hud|structures|recipes");
                            }
                            ctx.reply("已发送刷新请求/失效标记: " + ctx.arg(1));
                        }
                        case "save" -> { configs.save(configRoot); ctx.reply("MiniHUD 配置已保存"); }
                        default -> throw new IllegalArgumentException("servux status|sync|refresh|save");
                    }
                }).build());
    }

    private static Object invokeStatic(String className, String method) {
        try {
            return Class.forName(className).getMethod(method).invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("MiniHUD Servux API 不兼容: " + className + "." + method, e);
        }
    }

    private static Object invoke(Object target, String method) {
        try {
            return target.getClass().getMethod(method).invoke(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Servux 调用失败: " + method, e);
        }
    }
}
