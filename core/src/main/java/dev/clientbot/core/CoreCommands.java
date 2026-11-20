package dev.clientbot.core;

import dev.clientbot.api.ExtensionContext;
import dev.clientbot.command.BotCommand;
import dev.clientbot.command.CommandDispatcher;
import dev.clientbot.command.CommandRegistry;
import dev.clientbot.integration.IntegrationRegistry;
import dev.clientbot.task.BotTask;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Locale;

public final class CoreCommands {
    private CoreCommands() {}

    public static void register(ExtensionContext context, CommandDispatcher dispatcher,
                                ExtensionManager extensions, RenderController rendering,
                                ActionController actions) {
        CommandRegistry r = context.commands();
        r.register(BotCommand.builder("help").usage("[过滤]").description("列出指令与用法").permission("core.status")
                .handler(ctx -> {
                    String filter = ctx.args().isEmpty() ? "" : ctx.arg(0).toLowerCase(Locale.ROOT);
                    var matches = r.all().stream().filter(c -> (c.name() + c.description()).toLowerCase(Locale.ROOT).contains(filter)).toList();
                    matches.stream().limit(25).forEach(c -> ctx.reply(c.name() + " " + c.usage() + " — " + c.description()));
                    ctx.reply("共匹配 " + matches.size() + " 条" + (matches.size() > 25 ? "，仅显示前 25 条" : ""));
                }).build());
        r.register(BotCommand.builder("ping").description("连通性测试").permission("core.status")
                .handler(ctx -> ctx.reply("pong；玩家=" + ctx.player().getGameProfile().name())).build());
        r.register(BotCommand.builder("status").description("位置、生命、维度、任务和渲染状态").permission("core.status")
                .handler(ctx -> {
                    var p = ctx.player();
                    ctx.reply(String.format("%s | %.1f/%.1f HP | %s %.1f %.1f %.1f | 渲染=%s | FPS=%s | 任务=%d",
                            p.getGameProfile().name(), p.getHealth(), p.getMaxHealth(), p.level().dimension().identifier(),
                            p.getX(), p.getY(), p.getZ(), rendering.isSuppressed() ? "关闭" : "开启",
                            context.config().rendering.limitFps && rendering.isSuppressed() ? context.config().rendering.backgroundFps : context.config().rendering.normalFps,
                            context.tasks().list().size()));
                }).build());
        r.register(BotCommand.builder("mods").usage("[过滤]").description("列出已加载 Mod").permission("core.status")
                .handler(ctx -> {
                    String filter = ctx.args().isEmpty() ? "" : ctx.arg(0);
                    var mods = FabricLoader.getInstance().getAllMods().stream()
                            .filter(m -> m.getMetadata().getId().contains(filter)).toList();
                    mods.stream().limit(30).forEach(m -> ctx.reply(m.getMetadata().getId() + " " + m.getMetadata().getVersion().getFriendlyString()));
                    ctx.reply("匹配 " + mods.size() + " 个 Mod");
                }).build());
        r.register(BotCommand.builder("extensions").description("查看扩展加载结果").permission("core.status")
                .handler(ctx -> extensions.statuses().forEach(s -> ctx.reply((s.loaded() ? "[✓] " : "[✗] ") + s.id() + ": " + s.detail()))).build());
        r.register(BotCommand.builder("integrations").usage("list | set <ID> <on/off>").description("统一开关其他 Mod 已注册的功能").permission("core.render")
                .handler(ctx -> {
                    String action = ctx.args().isEmpty() ? "list" : ctx.arg(0);
                    if (action.equals("list")) {
                        for (IntegrationRegistry.Toggle toggle : context.integrations().all()) {
                            ctx.reply(toggle.id() + " = " + toggle.getter().getAsBoolean() + " (" + toggle.ownerMod() + ")");
                        }
                    } else if (action.equals("set")) {
                        String id = ctx.arg(1);
                        boolean value = ctx.booleanArg(2);
                        IntegrationRegistry.Toggle found = context.integrations().all().stream().filter(t -> t.id().equals(id)).findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("未知集成功能: " + id));
                        found.setter().accept(value);
                        ctx.reply(id + " -> " + value);
                    } else throw new IllegalArgumentException("integrations list|set");
                }).build());
        r.register(BotCommand.builder("render").usage("on|off|toggle|fps <1-60> | normalfps <5-1000>").description("关闭世界渲染并限制 FPS，网络与 Tick 保持运行").permission("core.render")
                .handler(ctx -> {
                    String value = ctx.arg(0).toLowerCase(Locale.ROOT);
                    switch (value) {
                        case "on" -> rendering.setSuppressed(false);
                        case "off" -> rendering.setSuppressed(true);
                        case "toggle" -> rendering.toggle();
                        case "fps" -> { context.config().rendering.backgroundFps = ctx.intArg(1); rendering.applyFrameLimit(); }
                        case "normalfps" -> { context.config().rendering.normalFps = ctx.intArg(1); rendering.applyFrameLimit(); }
                        default -> throw new IllegalArgumentException("render on|off|toggle|fps|normalfps");
                    }
                    context.config().rendering.backgroundFps = Math.max(1, Math.min(60, context.config().rendering.backgroundFps));
                    ctx.reply("世界渲染=" + (!rendering.isSuppressed()) + "，后台 FPS=" + context.config().rendering.backgroundFps);
                }).build());
        r.register(BotCommand.builder("say").usage("<聊天内容>").description("以当前玩家发送聊天").permission("core.chat")
                .handler(ctx -> { ctx.player().connection.sendChat(ctx.joined(0)); ctx.reply("已发送"); }).build());
        r.register(BotCommand.builder("whisper").usage("<玩家> <内容>").description("以当前玩家发送私聊").permission("core.chat")
                .handler(ctx -> { ctx.player().connection.sendCommand("msg " + ctx.arg(0) + " " + ctx.joined(1)); ctx.reply("已私聊 " + ctx.arg(0)); }).build());
        r.register(BotCommand.builder("cmd").usage("<服务器命令，不带/>").description("执行服务器命令；受黑名单和确认保护").permission("core.command").dangerous()
                .handler(ctx -> {
                    String command = ctx.joined(0).replaceFirst("^/", "");
                    String root = command.split(" ", 2)[0].toLowerCase(Locale.ROOT);
                    String unnamespacedRoot = root.substring(root.lastIndexOf(':') + 1);
                    if (context.config().blockedServerCommands.stream().filter(java.util.Objects::nonNull)
                            .map(s -> s.toLowerCase(Locale.ROOT)).anyMatch(blocked -> blocked.equals(root) || blocked.equals(unnamespacedRoot))) {
                        throw new IllegalArgumentException("服务器命令被 core.json 黑名单阻止: " + root);
                    }
                    ctx.player().connection.sendCommand(command);
                    ctx.reply("已执行服务器命令 /" + command);
                }).build());
        r.register(BotCommand.builder("look").usage("<yaw> <pitch>").description("设置视角").permission("core.movement")
                .handler(ctx -> { ctx.player().setYRot((float) ctx.doubleArg(0)); ctx.player().setXRot((float) ctx.doubleArg(1)); ctx.reply("视角已更新"); }).build());
        r.register(BotCommand.builder("move").usage("<forward/back/left/right/jump/sneak/sprint> <on/off> | clear").description("持续移动键；停止任务或 disconnect 会清理").permission("core.movement")
                .handler(ctx -> {
                    if (ctx.arg(0).equalsIgnoreCase("clear")) actions.clear();
                    else actions.set(ctx.arg(0), ctx.booleanArg(1));
                    ctx.reply("移动状态已更新");
                }).build());
        r.register(BotCommand.builder("walk").usage("<x> <y> <z> [容差]").description("直线行走（无绕障寻路）").permission("core.movement")
                .handler(ctx -> {
                    BlockPos target = new BlockPos(ctx.intArg(0), ctx.intArg(1), ctx.intArg(2));
                    double tolerance = ctx.args().size() > 3 ? ctx.doubleArg(3) : 0.7;
                    context.tasks().submit(ActionTasks.walkTo(context.client(), actions, target, tolerance));
                    ctx.reply("开始直线走向 " + target.toShortString());
                }).build());
        r.register(BotCommand.builder("follow").usage("<玩家名> [距离]").description("直线跟随可见玩家（无绕障寻路）").permission("core.movement")
                .handler(ctx -> {
                    context.tasks().submit(ActionTasks.follow(context.client(), actions, ctx.arg(0), ctx.args().size() > 1 ? ctx.doubleArg(1) : 2.5));
                    ctx.reply("开始跟随 " + ctx.arg(0));
                }).build());
        r.register(BotCommand.builder("slot").usage("<0-8>").description("切换快捷栏").permission("core.inventory")
                .handler(ctx -> {
                    int slot = ctx.intArg(0);
                    if (slot < 0 || slot > 8) throw new IllegalArgumentException("槽位范围为 0-8");
                    ctx.player().getInventory().setSelectedSlot(slot);
                    ctx.reply("快捷栏 -> " + slot);
                }).build());
        r.register(BotCommand.builder("inventory").description("查看快捷栏物品").permission("core.inventory")
                .handler(ctx -> {
                    for (int i = 0; i < 9; i++) {
                        ItemStack stack = ctx.player().getInventory().getItem(i);
                        if (!stack.isEmpty()) ctx.reply(i + ": " + stack.getHoverName().getString() + " x" + stack.getCount());
                    }
                }).build());
        r.register(BotCommand.builder("drop").usage("<true=整组/false=一个>").description("丢弃当前手持物品").permission("core.inventory").dangerous()
                .handler(ctx -> { ctx.player().drop(ctx.args().isEmpty() || ctx.booleanArg(0)); ctx.reply("已发送丢弃动作"); }).build());
        r.register(BotCommand.builder("use").usage("[ticks]").description("使用主手物品一次").permission("core.interact")
                .handler(ctx -> {
                    int ticks = ctx.args().isEmpty() ? 1 : ctx.intArg(0);
                    context.tasks().submit(ActionTasks.use(context.client(), ticks));
                    ctx.reply("使用动作已排队");
                }).build());
        r.register(BotCommand.builder("attack").usage("once | start [间隔tick] | stop").description("攻击准星实体").permission("core.interact")
                .handler(ctx -> {
                    switch (ctx.arg(0)) {
                        case "once" -> {
                            HitResult hit = context.client().hitResult;
                            if (!(hit instanceof EntityHitResult entityHit)) throw new IllegalArgumentException("准星没有指向实体");
                            context.client().gameMode.attack(ctx.player(), entityHit.getEntity());
                            ctx.player().swing(InteractionHand.MAIN_HAND);
                        }
                        case "start" -> context.tasks().submit(ActionTasks.attackCrosshair(context.client(), ctx.args().size() > 1 ? ctx.intArg(1) : 10));
                        case "stop" -> context.tasks().stop("attack");
                        default -> throw new IllegalArgumentException("attack once|start|stop");
                    }
                    ctx.reply("攻击状态已更新");
                }).build());
        r.register(BotCommand.builder("mine").usage("crosshair | <x> <y> <z> | stop").description("持续挖掘指定方块；仅 reach 范围内").permission("core.interact")
                .handler(ctx -> {
                    if (ctx.arg(0).equals("stop")) context.tasks().stop("mine");
                    else if (ctx.arg(0).equals("crosshair")) context.tasks().submit(ActionTasks.mineCrosshair(context.client()));
                    else context.tasks().submit(ActionTasks.mine(context.client(), new BlockPos(ctx.intArg(0), ctx.intArg(1), ctx.intArg(2))));
                    ctx.reply("挖掘状态已更新");
                }).build());
        r.register(BotCommand.builder("tasks").description("列出后台任务").permission("core.task")
                .handler(ctx -> { for (BotTask task : context.tasks().list()) ctx.reply(task.id() + ": " + task.description()); }).build());
        r.register(BotCommand.builder("stop").usage("<任务ID> | all").description("停止任务；all 同时清理移动").permission("core.task")
                .handler(ctx -> {
                    if (ctx.arg(0).equals("all")) { context.tasks().stopAll(); actions.clear(); }
                    else if (!context.tasks().stop(ctx.arg(0))) throw new IllegalArgumentException("任务不存在");
                    ctx.reply("已停止");
                }).build());
        r.register(BotCommand.builder("after").usage("<ticks> <指令及参数>").description("延迟执行另一条 ClientBot 指令").permission("core.task")
                .handler(ctx -> {
                    int delay = Math.max(1, ctx.intArg(0));
                    String raw = ctx.joined(1);
                    context.tasks().submit(new BotTask() {
                        int remaining = delay;
                        public String id() { return "after-" + System.nanoTime(); }
                        public String description() { return delay + " tick 后执行 " + raw; }
                        public boolean tick() {
                            if (--remaining > 0) return true;
                            if (ctx.sender().local()) dispatcher.executeLocal(raw);
                            else dispatcher.handleRemote(ctx.sender().uuid(), ctx.sender().name(), context.config().commandPrefix + " " + raw);
                            return false;
                        }
                    });
                    ctx.reply("已安排 " + delay + " tick 后执行: " + raw);
                }).build());
    }
}
