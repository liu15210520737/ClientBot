package dev.clientbot.command;

/** A command with a fine-grained permission node. Dangerous commands require confirmation remotely. */
public record BotCommand(
        String name,
        String usage,
        String description,
        String permission,
        boolean dangerous,
        Handler handler) {

    @FunctionalInterface
    public interface Handler {
        void execute(CommandContext context) throws Exception;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private final String name;
        private String usage = "";
        private String description = "";
        private String permission;
        private boolean dangerous;
        private Handler handler;

        private Builder(String name) {
            this.name = name;
            this.permission = name;
        }

        public Builder usage(String value) { this.usage = value; return this; }
        public Builder description(String value) { this.description = value; return this; }
        public Builder permission(String value) { this.permission = value; return this; }
        public Builder dangerous() { this.dangerous = true; return this; }
        public Builder handler(Handler value) { this.handler = value; return this; }

        public BotCommand build() {
            if (handler == null) throw new IllegalStateException("Missing handler for " + name);
            return new BotCommand(name, usage, description, permission, dangerous, handler);
        }
    }
}
