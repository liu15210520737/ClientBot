package dev.clientbot.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    @Test
    void sameExclusiveGroupReplacesAndStopsPreviousTask() {
        TaskManager manager = new TaskManager();
        TestTask first = new TestTask("first", "movement", true, false);
        TestTask second = new TestTask("second", "movement", true, false);
        manager.submit(first);
        manager.submit(second);

        assertTrue(first.started);
        assertTrue(first.stopped);
        assertTrue(second.started);
        assertFalse(second.stopped);
        assertEquals(1, manager.list().size());
        assertSame(second, manager.get("second").orElseThrow());
    }

    @Test
    void differentExclusiveGroupsCanRunTogether() {
        TaskManager manager = new TaskManager();
        manager.submit(new TestTask("walk", "movement", true, false));
        manager.submit(new TestTask("use", "interaction", true, false));
        assertEquals(2, manager.list().size());
    }

    @Test
    void duplicateIdStopsPreviousTask() {
        TaskManager manager = new TaskManager();
        TestTask first = new TestTask("same", "", true, false);
        TestTask second = new TestTask("same", "", true, false);
        manager.submit(first);
        manager.submit(second);
        assertTrue(first.stopped);
        assertSame(second, manager.get("same").orElseThrow());
    }

    @Test
    void completedAndFailingTasksAreStoppedAndRemoved() {
        TaskManager manager = new TaskManager();
        TestTask completed = new TestTask("completed", "", false, false);
        TestTask failing = new TestTask("failing", "", true, true);
        manager.submit(completed);
        manager.submit(failing);
        manager.tick();

        assertTrue(completed.stopped);
        assertTrue(failing.stopped);
        assertTrue(manager.list().isEmpty());
    }

    @Test
    void stopAllStopsEveryTask() {
        TaskManager manager = new TaskManager();
        TestTask first = new TestTask("first", "", true, false);
        TestTask second = new TestTask("second", "", true, false);
        manager.submit(first);
        manager.submit(second);
        manager.stopAll();
        assertTrue(first.stopped);
        assertTrue(second.stopped);
        assertTrue(manager.list().isEmpty());
        assertFalse(manager.stop("missing"));
    }

    private static final class TestTask implements BotTask {
        private final String id;
        private final String group;
        private final boolean alive;
        private final boolean fail;
        private boolean started;
        private boolean stopped;

        private TestTask(String id, String group, boolean alive, boolean fail) {
            this.id = id;
            this.group = group;
            this.alive = alive;
            this.fail = fail;
        }

        @Override public String id() { return id; }
        @Override public String description() { return id; }
        @Override public String exclusiveGroup() { return group; }
        @Override public boolean tick() {
            if (fail) throw new IllegalStateException("test failure");
            return alive;
        }
        @Override public void start() { started = true; }
        @Override public void stop() { stopped = true; }
    }
}
