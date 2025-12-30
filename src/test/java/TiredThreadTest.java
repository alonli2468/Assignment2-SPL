import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scheduling.TiredThread;
import java.util.concurrent.TimeUnit;

class TiredThreadTest {

    private TiredThread thread;
    private final double FATIGUE_FACTOR = 1.0;

    @BeforeEach
    void setUp() {
        thread = new TiredThread(1, FATIGUE_FACTOR);
        thread.start();
    }

    @AfterEach
    void shutDown() throws InterruptedException {
        if (thread != null && thread.isAlive()) {
            thread.shutdown();
            thread.join(1000);
        }
    }

    @Test
    void testNullTaskSubmissionThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            thread.newTask(null);
        });
    }

    @Test
    void testNewTaskSubmissionWhenWorkerIsNotAlive() {
        thread.shutdown();
        assertThrows(IllegalStateException.class, () -> {
            thread.newTask(() -> {
            });
        });
    }

    @Test
    void testInitialState() {
        assertEquals(1, thread.getWorkerId());
        assertEquals(0, thread.getTimeUsed());
        assertEquals(0, thread.getTimeIdle());
        assertEquals(0.0, thread.getFatigue());
        assertFalse(thread.isBusy());
    }

    @Test
    void testFatigueCalculationAfterTask() throws InterruptedException {
        long sleepTimeMs = 100;
        thread.newTask(() -> {
            try {
                Thread.sleep(sleepTimeMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        while (!thread.isBusy()) { Thread.sleep(10); } 
        while (thread.isBusy()) { Thread.sleep(10); }
        assertTrue(thread.getTimeUsed() > 0, "Time used didn't change");
        double expectedMinFatigue = FATIGUE_FACTOR * TimeUnit.MILLISECONDS.toNanos(sleepTimeMs);
        assertTrue(thread.getFatigue() >= expectedMinFatigue, 
            "Fatigue isn't at least (factor * sleepTime)");
    }

    @Test
    void testIsBusyAfterTask() throws InterruptedException {
        long sleepTimeMs = 100;
        thread.newTask(() -> {
            try {
                Thread.sleep(sleepTimeMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        while (!thread.isBusy()) { Thread.sleep(10); } 
        while (thread.isBusy()) { Thread.sleep(10); }
        assertFalse(thread.isBusy() ,  "Thread is busy after finishing task");
    }

    @Test
    void testCompareTo() throws InterruptedException{
        TiredThread easyThread = new TiredThread(2, 0.5);
        thread.newTask(() -> {
            try { Thread.sleep(50); 

            } catch (InterruptedException e) {}
        });
        while (thread.getFatigue() == 0) { Thread.sleep(10); }
        assertTrue(thread.compareTo(easyThread) > 0, "Incorrect compare");
        assertTrue(easyThread.compareTo(thread) < 0, "Incorrect compare");
    }

    @Test
    void testShutdownWithPoisonPill() throws InterruptedException {
        assertTrue(thread.isAlive());
        thread.shutdown();
        thread.join(2000); 
        assertFalse(thread.isAlive(), "Thread is Alive after shutdown");
    }
}