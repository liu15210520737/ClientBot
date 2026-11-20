package dev.clientbot.core;

import dev.clientbot.task.BotTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Built-in cooperative tasks. Walking is deliberately straight-line movement, not pathfinding. */
public final class ActionTasks {
    private ActionTasks() {}

    public static BotTask walkTo(Minecraft client, ActionController actions, BlockPos target, double tolerance) {
        return new BotTask() {
            public String id() { return "walk"; }
            public String description() { return "直线走向 " + target.toShortString(); }
            public String exclusiveGroup() { return "movement"; }
            public boolean tick() {
                if (client.player == null) return false;
                LocalPlayer player = client.player;
                double dx = target.getX() + 0.5 - player.getX();
                double dz = target.getZ() + 0.5 - player.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance <= tolerance || Math.abs(target.getY() - player.getY()) > 12) return false;
                ActionController.lookAt(player, target.getX() + 0.5, player.getEyeY(), target.getZ() + 0.5);
                actions.set("forward", true);
                if (player.horizontalCollision) actions.set("jump", true); else actions.set("jump", false);
                return true;
            }
            public void stop() { actions.clear(); }
        };
    }

    public static BotTask follow(Minecraft client, ActionController actions, String name, double distance) {
        return new BotTask() {
            public String id() { return "follow"; }
            public String description() { return "跟随 " + name; }
            public String exclusiveGroup() { return "movement"; }
            public boolean tick() {
                if (client.player == null || client.level == null) return false;
                Entity target = null;
                for (Entity entity : client.level.entitiesForRendering()) {
                    if (entity.getName().getString().equalsIgnoreCase(name)) { target = entity; break; }
                }
                if (target == null) { actions.clear(); return true; }
                double current = client.player.distanceTo(target);
                ActionController.lookAt(client.player, target.getX(), target.getEyeY(), target.getZ());
                actions.set("forward", current > distance);
                actions.set("sprint", current > distance + 4);
                if (client.player.horizontalCollision) actions.set("jump", true); else actions.set("jump", false);
                return true;
            }
            public void stop() { actions.clear(); }
        };
    }

    public static BotTask mine(Minecraft client, BlockPos pos) {
        return new BotTask() {
            private boolean started;
            public String id() { return "mine"; }
            public String description() { return "挖掘 " + pos.toShortString(); }
            public String exclusiveGroup() { return "break"; }
            public boolean tick() {
                if (client.player == null || client.level == null || client.gameMode == null) return false;
                if (client.level.getBlockState(pos).isAir()) return false;
                if (client.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > 36) return false;
                ActionController.lookAt(client.player, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                if (!started) {
                    client.gameMode.startDestroyBlock(pos, Direction.UP);
                    started = true;
                } else {
                    client.gameMode.continueDestroyBlock(pos, Direction.UP);
                }
                client.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                return true;
            }
            public void stop() { if (client.gameMode != null) client.gameMode.stopDestroyBlock(); }
        };
    }

    public static BotTask use(Minecraft client, int ticks) {
        return new BotTask() {
            private int remaining = Math.max(1, ticks);
            public String id() { return "use"; }
            public String description() { return "使用主手物品"; }
            public String exclusiveGroup() { return "use"; }
            public boolean tick() {
                if (client.player == null || client.gameMode == null) return false;
                if (remaining == Math.max(1, ticks)) client.gameMode.useItem(client.player, net.minecraft.world.InteractionHand.MAIN_HAND);
                remaining--;
                return remaining > 0;
            }
        };
    }

    public static BotTask attackCrosshair(Minecraft client, int interval) {
        return new BotTask() {
            private int cooldown;
            public String id() { return "attack"; }
            public String description() { return "攻击准星实体（每 " + interval + " tick）"; }
            public String exclusiveGroup() { return "attack"; }
            public boolean tick() {
                if (client.player == null || client.gameMode == null) return false;
                if (--cooldown > 0) return true;
                cooldown = Math.max(1, interval);
                HitResult hit = client.hitResult;
                if (hit != null && hit.getType() == HitResult.Type.ENTITY && hit instanceof EntityHitResult entityHit) {
                    client.gameMode.attack(client.player, entityHit.getEntity());
                    client.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                }
                return true;
            }
        };
    }

    public static BotTask mineCrosshair(Minecraft client) {
        HitResult hit = client.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            throw new IllegalArgumentException("准星没有指向方块");
        }
        return mine(client, ((BlockHitResult) hit).getBlockPos());
    }
}
