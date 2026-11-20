package dev.clientbot.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/** Applies remote movement flags while preserving unrelated physical key states. */
public final class ActionController {
    public enum Move {
        FORWARD, BACKWARD, LEFT, RIGHT, JUMP, SNEAK, SPRINT
    }

    private final Map<Move, Boolean> overrides = new EnumMap<>(Move.class);

    public void set(String name, boolean value) {
        String normalized = name.toLowerCase(Locale.ROOT);
        Move move = switch (normalized) {
            case "forward", "w" -> Move.FORWARD;
            case "back", "backward", "s" -> Move.BACKWARD;
            case "left", "a" -> Move.LEFT;
            case "right", "d" -> Move.RIGHT;
            case "jump", "space" -> Move.JUMP;
            case "sneak", "shift" -> Move.SNEAK;
            case "sprint", "ctrl" -> Move.SPRINT;
            default -> throw new IllegalArgumentException("未知移动键: " + name);
        };
        overrides.put(move, value);
    }

    public void clear() {
        overrides.clear();
    }

    public void apply(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || player.input == null) {
            overrides.clear();
            return;
        }
        Input old = player.input.keyPresses;
        player.input.keyPresses = new Input(
                value(Move.FORWARD, old.forward()),
                value(Move.BACKWARD, old.backward()),
                value(Move.LEFT, old.left()),
                value(Move.RIGHT, old.right()),
                value(Move.JUMP, old.jump()),
                value(Move.SNEAK, old.shift()),
                value(Move.SPRINT, old.sprint()));
    }

    private boolean value(Move key, boolean original) {
        return overrides.getOrDefault(key, original);
    }

    public static void lookAt(LocalPlayer player, double x, double y, double z) {
        double dx = x - player.getX();
        double dz = z - player.getZ();
        double dy = y - player.getEyeY();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        player.setYRot(yaw);
        player.setXRot(pitch);
    }
}
