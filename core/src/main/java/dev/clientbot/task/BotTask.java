package dev.clientbot.task;

/** Cooperative client-tick task. Implementations must be cheap and non-blocking. */
public interface BotTask {
    String id();
    String description();

    /** Tasks in the same exclusive group replace each other. */
    default String exclusiveGroup() { return ""; }

    /** Return false when the task has finished. */
    boolean tick();

    default void start() {}
    default void stop() {}
}
