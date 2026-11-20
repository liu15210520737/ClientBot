package dev.clientbot.command;

import java.util.List;
import java.util.UUID;

/** Authenticated command origin. Remote instances are created only after ACL checks. */
public record CommandSender(UUID uuid, String name, boolean local) {
    public static CommandSender localSender() {
        return new CommandSender(UUID.fromString("00000000-0000-0000-0000-000000000000"), "local", true);
    }
}
