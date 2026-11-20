package dev.clientbot.task;

import dev.clientbot.core.ClientBotMod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TaskManager {
    private final Map<String, BotTask> tasks = new LinkedHashMap<>();

    public synchronized void submit(BotTask task) {
        if (!task.exclusiveGroup().isBlank()) {
            tasks.values().removeIf(existing -> {
                if (existing.exclusiveGroup().equals(task.exclusiveGroup())) {
                    existing.stop();
                    return true;
                }
                return false;
            });
        }
        BotTask previous = tasks.remove(task.id());
        if (previous != null) previous.stop();
        tasks.put(task.id(), task);
        task.start();
    }

    public synchronized boolean stop(String id) {
        BotTask removed = tasks.remove(id);
        if (removed == null) return false;
        removed.stop();
        return true;
    }

    public synchronized void stopAll() {
        tasks.values().forEach(BotTask::stop);
        tasks.clear();
    }

    public synchronized Optional<BotTask> get(String id) {
        return Optional.ofNullable(tasks.get(id));
    }

    public synchronized List<BotTask> list() {
        return List.copyOf(tasks.values());
    }

    public synchronized void tick() {
        for (BotTask task : new ArrayList<>(tasks.values())) {
            boolean alive;
            try {
                alive = task.tick();
            } catch (Exception e) {
                ClientBotMod.LOGGER.error("任务 {} 异常，已停止", task.id(), e);
                alive = false;
            }
            if (!alive && tasks.get(task.id()) == task) {
                tasks.remove(task.id());
                task.stop();
            }
        }
    }
}
