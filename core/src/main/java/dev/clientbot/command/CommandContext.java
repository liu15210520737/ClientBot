package dev.clientbot.command;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;
import java.util.function.Consumer;

public record CommandContext(
        Minecraft client,
        CommandSender sender,
        List<String> args,
        Consumer<String> output,
        String rawCommand) {

    public void reply(String message) {
        output.accept(message);
    }

    public LocalPlayer player() {
        if (client == null || client.player == null) {
            throw new IllegalStateException("当前不在世界中");
        }
        return client.player;
    }

    public String arg(int index) {
        if (index >= args.size()) {
            throw new IllegalArgumentException("缺少参数 #" + (index + 1));
        }
        return args.get(index);
    }

    public int intArg(int index) {
        return Integer.parseInt(arg(index));
    }

    public double doubleArg(int index) {
        return Double.parseDouble(arg(index));
    }

    public boolean booleanArg(int index) {
        return switch (arg(index).toLowerCase()) {
            case "on", "true", "yes", "1", "开" -> true;
            case "off", "false", "no", "0", "关" -> false;
            default -> throw new IllegalArgumentException("布尔值应为 on/off");
        };
    }

    public String joined(int start) {
        return String.join(" ", args.subList(Math.min(start, args.size()), args.size()));
    }
}
