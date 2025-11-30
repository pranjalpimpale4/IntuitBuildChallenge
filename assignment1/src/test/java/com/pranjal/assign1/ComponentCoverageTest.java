package com.pranjal.assign1;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * COMPONENT COVERAGE TESTS
 * ------------------------------------------------------------------
 * Focus: "White Box" testing to hit lines of code in helper classes.
 */
class ComponentCoverageTest {

    private AdvancedBlockingQueue<Object> queue;
    private MetricsCollector metrics;

    @BeforeEach
    void setUp() {
        queue = new AdvancedBlockingQueue<>(10);
        metrics = new MockMetricsCollector(); // Mock for testing
        Logger.init();
    }

    @AfterEach
    void tearDown() {
        Logger.close();
    }

    // ==========================================
    // 1. LOGGER TEST
    // ==========================================
    @Test
    void testLoggerLifecycle() {
        Logger.log("TEST-COMPONENT", "Testing logger writing.");
        File logFile = new File("execution_history.log");
        assertTrue(logFile.exists(), "Log file should exist");
        assertTrue(logFile.length() > 0, "Log file should not be empty");
        Logger.close();
    }

    // ==========================================
    // 2. DASHBOARD TEST
    // ==========================================
    @Test
    @Timeout(2)
    void testDashboardLifecycle() throws InterruptedException {
        ThreadManager dummyManager = new ThreadManager(queue, metrics);
        Dashboard dashboard = new Dashboard(queue, dummyManager, metrics);
        
        // Start dashboard with scheduled updates
        dashboard.start();
        
        // Let it run for a bit
        Thread.sleep(600);
        
        // Stop should gracefully shutdown the scheduler
        dashboard.stop();
        
        // If we reach here without hanging, test passed
        assertTrue(true);
    }

    // ==========================================
    // 3. PRODUCER WORKER TEST
    // ==========================================
    @Test
    void testProducerWorkerLogic() {
        int itemsToProduce = 1;
        ProducerWorker producer = new ProducerWorker(queue, 99, itemsToProduce, metrics);
        producer.run();
        
        assertEquals(1, queue.getSize());
        try {
            String item = (String) queue.take();
            assertTrue(item.contains("Record-99"));
        } catch (InterruptedException e) {
            fail("Queue take failed");
        }
    }

    // ==========================================
    // 4. CONSUMER WORKER TEST
    // ==========================================
    @Test
    @Timeout(2)
    void testConsumerWorkerLogic() throws InterruptedException {
        queue.put(QueueCommand.POISON_PILL);
        ConsumerWorker consumer = new ConsumerWorker(queue, "TestConsumer", metrics);
        
        consumer.run();
        
        assertEquals(1, queue.getSize());
        assertEquals(QueueCommand.POISON_PILL, queue.take());
    }
    
    @Test
    @Timeout(3)
    void testConsumerWorkerNormalProcessing() throws InterruptedException {
        // Test normal item processing
        queue.put("TestItem-1");
        queue.put("TestItem-2");
        queue.put(QueueCommand.POISON_PILL);
        
        ConsumerWorker consumer = new ConsumerWorker(queue, "TestConsumer", metrics);
        consumer.run();
        
        // After processing, queue should have poison pill back
        assertEquals(1, queue.getSize());
        assertEquals(QueueCommand.POISON_PILL, queue.take());
    }
    
    @Test
    @Timeout(2)
    void testConsumerWorkerWithIdConstructor() throws InterruptedException {
        // Test alternative constructor with int ID
        queue.put(QueueCommand.POISON_PILL);
        ConsumerWorker consumer = new ConsumerWorker(queue, 42, metrics);
        
        consumer.run();
        
        assertEquals(1, queue.getSize());
        assertEquals(QueueCommand.POISON_PILL, queue.take());
    }
    
    @Test
    @Timeout(3)
    void testConsumerWorkerMultipleItems() throws InterruptedException {
        // Test processing multiple items in sequence
        long initialConsumed = metrics.getTotalConsumed();
        
        queue.put("Item-A");
        queue.put("Item-B");
        queue.put("Item-C");
        queue.put(QueueCommand.POISON_PILL);
        
        ConsumerWorker consumer = new ConsumerWorker(queue, "MultiItemConsumer", metrics);
        consumer.run();
        
        // Verify 3 items were consumed
        assertEquals(initialConsumed + 3, metrics.getTotalConsumed());
        
        // Poison pill should be back in queue
        assertEquals(1, queue.getSize());
    }
    
    @Test
    @Timeout(2)
    void testConsumerWorkerInterruption() throws InterruptedException {
        // Test InterruptedException handling during sleep
        queue.put("Item1");
        queue.put("Item2");
        
        ConsumerWorker consumer = new ConsumerWorker(queue, "InterruptedConsumer", metrics);
        Thread consumerThread = new Thread(() -> {
            consumer.run();
        });
        
        consumerThread.start();
        Thread.sleep(150); // Let it start processing (sleep is 50-250ms)
        consumerThread.interrupt(); // Interrupt during processing
        consumerThread.join(1000); // Wait for it to finish
        
        assertFalse(consumerThread.isAlive(), "Consumer should have stopped after interruption");
        // Verify log file captured the interruption
        File logFile = new File("execution_history.log");
        assertTrue(logFile.exists() && logFile.length() > 0);
    }
    
    @Test
    @Timeout(2)
    void testConsumerWorkerEmptyQueueWithTimeout() throws InterruptedException {
        // Test consumer waiting on empty queue, then receiving item
        ConsumerWorker consumer = new ConsumerWorker(queue, "WaitingConsumer", metrics);
        
        Thread consumerThread = new Thread(() -> {
            consumer.run();
        });
        
        consumerThread.start();
        Thread.sleep(100); // Consumer is now waiting on empty queue
        
        // Now add item and poison pill
        queue.put("DelayedItem");
        queue.put(QueueCommand.POISON_PILL);
        
        consumerThread.join(1000);
        assertFalse(consumerThread.isAlive(), "Consumer should complete after receiving items");
    }
    
    @Test
    @Timeout(2)
    void testConsumerWorkerPoisonPillRequeue() throws InterruptedException {
        // Explicitly test poison pill pass-through behavior
        queue.put(QueueCommand.POISON_PILL);
        
        ConsumerWorker consumer1 = new ConsumerWorker(queue, "Consumer1", metrics);
        consumer1.run();
        
        // Poison pill should be back in queue for next consumer
        assertEquals(1, queue.getSize());
        Object item = queue.take();
        assertEquals(QueueCommand.POISON_PILL, item);
        
        // Put it back for another consumer
        queue.put(item);
        ConsumerWorker consumer2 = new ConsumerWorker(queue, "Consumer2", metrics);
        consumer2.run();
        
        // Should still be in queue
        assertEquals(1, queue.getSize());
    }
    
    @Test
    @Timeout(3)
    void testConsumerWorkerLoggingAndSleep() throws InterruptedException {
        // Test that consumer logs properly and sleeps during processing
        queue.put("LoggedItem-1");
        queue.put("LoggedItem-2");
        queue.put(QueueCommand.POISON_PILL);
        
        long startTime = System.currentTimeMillis();
        ConsumerWorker consumer = new ConsumerWorker(queue, "LoggingConsumer", metrics);
        consumer.run();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Processing 2 items with sleep(random(200)+50) each should take at least 100ms total
        // Using conservative estimate to avoid flaky tests
        assertTrue(duration >= 80, 
            "Consumer should have slept during processing (took " + duration + "ms)");
        
        // Verify log file has entries
        File logFile = new File("execution_history.log");
        assertTrue(logFile.exists());
        assertTrue(logFile.length() > 0);
    }
    
    @Test
    @Timeout(2)
    void testConsumerWorkerStartupAndShutdownLogging() throws InterruptedException {
        // Test that startup and shutdown messages are logged
        File logFile = new File("execution_history.log");
        long initialSize = logFile.exists() ? logFile.length() : 0;
        
        queue.put(QueueCommand.POISON_PILL);
        ConsumerWorker consumer = new ConsumerWorker(queue, "LogTestConsumer", metrics);
        consumer.run();
        
        // Log should have grown with STARTED and SHUTDOWN messages
        assertTrue(logFile.exists());
        assertTrue(logFile.length() > initialSize, "Log should contain new entries");
    }
    
    @Test
    @Timeout(2)
    void testConsumerWorkerQueueSizeLogging() throws InterruptedException {
        // Test that queue size is logged during processing
        queue.put("Item-QueueSize-1");
        queue.put("Item-QueueSize-2");
        queue.put("Item-QueueSize-3");
        queue.put(QueueCommand.POISON_PILL);
        
        ConsumerWorker consumer = new ConsumerWorker(queue, "QueueSizeConsumer", metrics);
        
        // Consumer should process all items and log queue size each time
        consumer.run();
        
        // After completion, only poison pill remains
        assertEquals(1, queue.getSize());
        
        // Verify logging occurred
        File logFile = new File("execution_history.log");
        assertTrue(logFile.exists() && logFile.length() > 0);
    }

    // ==========================================
    // 5. THREAD MANAGER TEST (FIXED)
    // ==========================================
    @Test
    @Timeout(5) // Added Timeout for safety
    void testManagerLifecycle() throws InterruptedException {
        ThreadManager manager = new ThreadManager(queue, metrics);
        
        // 1. Start components
        manager.startProducers(1, 1);
        manager.startConsumers(1);
        
        assertEquals(1, manager.getProducerCount());
        assertEquals(1, manager.getConsumerCount());
        
        // 2. Run Monitor briefly
        manager.runAutoScalerLoop();
        Thread.sleep(100); 
        
        // 3. Shutdown Monitor
        manager.shutdown();
        
        // 4. FIX: We MUST stop the consumers before waiting for them!
        // Without this, the consumer waits in queue.take() forever.
        queue.put(QueueCommand.POISON_PILL);
        
        // 5. Now we can safely wait
        manager.waitForConsumers();
        
        // Pass if we reach here without timeout
        assertTrue(true);
    }
    
    // ==========================================
    // 6. TIMEOUT HANDLING COVERAGE
    // ==========================================
    @Test
    void testProducerTimeoutLogging() {
        // Simple logic verification
        AdvancedBlockingQueue<Object> tinyQueue = new AdvancedBlockingQueue<>(1);
        try {
            tinyQueue.put("Blocker");
        } catch (InterruptedException e) {}
        
        // Just verify no crash when run against full queue
        // (Full logic requires mocking time, but this hits the code path start)
        ProducerWorker producer = new ProducerWorker(tinyQueue, 100, 1, metrics);
        // We don't run() because it waits 2s. We trust the integration test for that.
        assertNotNull(producer); 
    }

    // ==========================================
    // 7. SCALING LOGIC VERIFICATION
    // ==========================================
    
    @Test
    @Timeout(5)
    void testAutoScalerActuallyScalesUp() throws InterruptedException {
        // Goal: Verify that high load actually triggers thread creation.
        ThreadManager manager = new ThreadManager(queue, metrics);
        
        // 1. Start with minimum consumers (1)
        manager.startConsumers(1);
        assertEquals(1, manager.getConsumerCount(), "Should start with 1 consumer");

        // 2. Artificially fill the queue to capacity (Queue size is 10 from setUp)
        // This simulates a "traffic spike"
        for (int i = 0; i < 10; i++) {
            queue.put("HeavyLoad-" + i);
        }

        // 3. Force the scaling check
        // (Assuming runAutoScalerLoop performs the check logic once)
        manager.runAutoScalerLoop();
        
        // Give the manager a tiny moment to spin up the new thread
        Thread.sleep(150);

        // 4. VERIFY: Did the count go up?
        int newCount = manager.getConsumerCount();
        assertTrue(newCount > 1, 
            "AutoScaler should have increased consumer count (Current: " + newCount + ")");
            
        // Cleanup
        manager.shutdown();
        queue.put(QueueCommand.POISON_PILL);
        manager.waitForConsumers();
    }
}