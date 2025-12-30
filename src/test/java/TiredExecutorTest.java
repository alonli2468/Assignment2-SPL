import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import scheduling.TiredExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

class TiredExecutorTest {

    private TiredExecutor executor;
    private final int NUM_THREADS = 4;

    @BeforeEach
    void setUp() {
        executor = new TiredExecutor(NUM_THREADS);
    }

    @AfterEach
    void shutDown() throws InterruptedException {
        if (executor != null) {
            executor.shutdown();
        }
    }

    @Test
    void testZeroThreadsThrows() {
        assertThrows(IllegalArgumentException.class, () -> new TiredExecutor(0));
    }

    @Test
    void testNegativeThreadsThrows() {
        assertThrows(IllegalArgumentException.class, () -> new TiredExecutor(-5));
    }

    @Test
    void submitNullTask_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> executor.submit(null));
    }

    @Test
    void testSubmitSingleTask() throws InterruptedException {
        final boolean[] taskDone = {false};
        executor.submit(() -> {
            taskDone[0] = true;
        });
        executor.submitAll(new ArrayList<>());
        assertTrue(taskDone[0], "Task didn't execute");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testSubmitAllAndInFlight() {
        int numTasks = 10;
        List<Runnable> tasks = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < numTasks; i++) {
            tasks.add(() -> {
                try {
                    Thread.sleep(100); 
                    counter.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        executor.submitAll(tasks);
        assertEquals(numTasks, counter.get(), "Not all tasks finished");
    }

    @Test
    void testShutdownSingleTasks() throws InterruptedException {
        final boolean[] taskDone = {false};
        executor.submit(() -> {
            taskDone[0] = true;
        });
        executor.submitAll(new ArrayList<>());
        executor.shutdown();
        assertTrue(taskDone[0], "Task didn't execute before shutdown");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testShutdownMultipleTasks() throws InterruptedException {
        int numTasks = 10;
        List<Runnable> tasks = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < numTasks; i++) {
            tasks.add(() -> {
                try {
                    Thread.sleep(100); 
                    counter.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        executor.submitAll(tasks);
        executor.shutdown();
        assertEquals(numTasks, counter.get(), "Not all tasks finished before shutdown");
    }

    @Test
    void testFatigueReporting() {
        String report = executor.getWorkerReport();
        assertTrue(report.contains("Fatigue"), "Report doesn't contain fatigue levels");
        assertTrue(report.contains("Time Used"), "Report doesn't contain timing data");
    }
}