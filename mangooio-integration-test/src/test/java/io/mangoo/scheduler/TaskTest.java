package io.mangoo.scheduler;

import io.mangoo.TestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith({TestExtension.class})
class TaskTest {

    public static class Job {
        public void failing() {
            throw new IllegalStateException("Intentionally failing job");
        }

        @SuppressWarnings("unused")
        private void notPublic() {
            // Not invocable by Task
        }
    }

    @Test
    void testNotInvocableMethodDoesNotThrow() {
        assertDoesNotThrow(() -> new Task(Job.class, "notPublic").run());
    }

    @Test
    void testFailingJobDoesNotThrow() {
        assertDoesNotThrow(() -> new Task(Job.class, "failing").run());
    }
}
