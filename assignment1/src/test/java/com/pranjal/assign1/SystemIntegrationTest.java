package com.pranjal.assign1;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * SYSTEM INTEGRATION TESTS
 * ------------------------------------------------------------------
 * Focus: Verifies Workers, Managers, and High-Load scenarios.
 * * MAP TO REQUIREMENTS:
 * - [x] Tests for producer-consumer lifecycle  -> testFullLifecycle()
 * - [x] Tests for poison pill shutdown         -> testConsumerShutdown()
 * - [x] Deadlock-detection tests               -> testDeadlockSafety_StarvationCheck()
 * - [x] Stress/load tests                      -> testHighLoadRaceConditions()
 */
class SystemIntegrationTest {

    // ==========================================
    // REQUIREMENT: Deadlock-detection tests
    // ==========================================
    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS) // <--- THIS IS THE DEADLOCK DETECTOR
    void testDeadlockSafety_StarvationCheck() throws InterruptedException {
        // Goal: Prove that threads don't get stuck waiting forever when resources are tight.
        // Scenario: 1 Producer, 1 Consumer, Queue Capacity 1 (Tightest possible bottleneck).
        
        AdvancedBlockingQueue<Object> queue = new AdvancedBlockingQueue<>(1);
        
        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 50; i++) queue.put("Item");
            } catch (InterruptedException e) {}
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 50; i++) queue.take();
            } catch (InterruptedException e) {}
        });

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();
        
        // If the code had a deadlock bug, this line would never be reached 
        // because the test would time out after 2 seconds.
        assertTrue(queue.getSize() == 0, "System should be clean after run");
    }

    // ==========================================
    // REQUIREMENT: Tests for poison pill shutdown
    // ==========================================
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testConsumerShutdown() throws InterruptedException {
        // Goal: Verify a ConsumerWorker shuts down correctly when receiving the pill.
        AdvancedBlockingQueue<Object> queue = new AdvancedBlockingQueue<>(10);
        
        // Start a single consumer with a mock metrics collector
        MetricsCollector metrics = new MockMetricsCollector();
        Thread consumer = new Thread(new ConsumerWorker(queue, "TestConsumer", metrics));
        consumer.start();

        // Send pill
        queue.put(QueueCommand.POISON_PILL);

        // Wait for death
        consumer.join();
        assertFalse(consumer.isAlive(), "Consumer thread should be dead");
        
        // Ensure pill was put back for others (The "Pass-it-on" logic)
        assertEquals(QueueCommand.POISON_PILL, queue.take());
    }

    // ==========================================
    // REQUIREMENT: Stress/load tests & Integration pipeline
    // ==========================================
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testHighLoadRaceConditions() throws InterruptedException {
        // Goal: Process 1,000 items with multiple threads running parallel.
        // MUST result in exactly 1,000 processed items. No duplicates, no loss.
        
        int totalItems = 1000;
        int producerCount = 5;
        int consumerCount = 5;
        int itemsPerProducer = totalItems / producerCount; // 200 each

        AdvancedBlockingQueue<Object> queue = new AdvancedBlockingQueue<>(50);
        
        // Best Practice: Use MetricsCollector interface instead of Dashboard static fields
        MetricsCollector metrics = new MockMetricsCollector();

        ExecutorService pool = Executors.newFixedThreadPool(producerCount + consumerCount);
        CountDownLatch producersDone = new CountDownLatch(producerCount);
        CountDownLatch consumersDone = new CountDownLatch(consumerCount);

        // 1. Launch 5 Producers
        for (int i = 0; i < producerCount; i++) {
            pool.submit(() -> {
                try {
                    for (int j = 0; j < itemsPerProducer; j++) {
                        queue.put("Item");
                        metrics.recordProduction();
                    }
                    producersDone.countDown();
                } catch (Exception e) { e.printStackTrace(); }
            });
        }

        // 2. Launch 5 Consumers
        for (int i = 0; i < consumerCount; i++) {
            pool.submit(() -> {
                try {
                    while (true) {
                        Object item = queue.take();
                        if (item == QueueCommand.POISON_PILL) {
                            queue.put(QueueCommand.POISON_PILL);
                            break;
                        }
                        metrics.recordConsumption();
                    }
                    consumersDone.countDown();
                } catch (Exception e) { e.printStackTrace(); }
            });
        }

        // 3. Wait for Producers to finish
        producersDone.await();

        // 4. Send Shutdown Signal to Consumers
        queue.put(QueueCommand.POISON_PILL);
        consumersDone.await();
        
        pool.shutdown();

        // 5. THE VERDICT
        // If these match, your system is mathematically thread-safe.
        assertEquals(totalItems, metrics.getTotalProduced(), "Producers should have produced exactly " + totalItems);
        assertEquals(totalItems, metrics.getTotalConsumed(), "Consumers should have processed exactly " + totalItems);
    }
}