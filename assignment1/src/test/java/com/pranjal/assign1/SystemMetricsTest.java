package com.pranjal.assign1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for SystemMetrics class.
 * Tests basic functionality and thread safety.
 */
class SystemMetricsTest {

    private SystemMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new SystemMetrics();
    }

    // ==========================================
    // BASIC FUNCTIONALITY TESTS
    // ==========================================

    @Test
    void testInitialState() {
        // Verify metrics start at zero
        assertEquals(0, metrics.getTotalProduced());
        assertEquals(0, metrics.getTotalConsumed());
    }

    @Test
    void testRecordProduction() {
        metrics.recordProduction();
        assertEquals(1, metrics.getTotalProduced());
        assertEquals(0, metrics.getTotalConsumed());

        metrics.recordProduction();
        metrics.recordProduction();
        assertEquals(3, metrics.getTotalProduced());
    }

    @Test
    void testRecordConsumption() {
        metrics.recordConsumption();
        assertEquals(0, metrics.getTotalProduced());
        assertEquals(1, metrics.getTotalConsumed());

        metrics.recordConsumption();
        metrics.recordConsumption();
        assertEquals(3, metrics.getTotalConsumed());
    }

    @Test
    void testBothOperations() {
        metrics.recordProduction();
        metrics.recordProduction();
        metrics.recordConsumption();

        assertEquals(2, metrics.getTotalProduced());
        assertEquals(1, metrics.getTotalConsumed());
    }

    @Test
    void testLargeNumbers() {
        for (int i = 0; i < 1000; i++) {
            metrics.recordProduction();
        }
        for (int i = 0; i < 750; i++) {
            metrics.recordConsumption();
        }

        assertEquals(1000, metrics.getTotalProduced());
        assertEquals(750, metrics.getTotalConsumed());
    }

    // ==========================================
    // THREAD SAFETY TESTS
    // ==========================================

    @Test
    @Timeout(5)
    void testConcurrentProduction() throws InterruptedException {
        int threadCount = 10;
        int incrementsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    metrics.recordProduction();
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        // Verify no race conditions - all increments recorded
        assertEquals(threadCount * incrementsPerThread, metrics.getTotalProduced());
    }

    @Test
    @Timeout(5)
    void testConcurrentConsumption() throws InterruptedException {
        int threadCount = 10;
        int incrementsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    metrics.recordConsumption();
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        assertEquals(threadCount * incrementsPerThread, metrics.getTotalConsumed());
    }

    @Test
    @Timeout(5)
    void testConcurrentMixedOperations() throws InterruptedException {
        int threadCount = 20; // 10 producers, 10 consumers
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Start 10 producer threads
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    metrics.recordProduction();
                }
                latch.countDown();
            });
        }

        // Start 10 consumer threads
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    metrics.recordConsumption();
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        // Verify atomicity
        assertEquals(10 * operationsPerThread, metrics.getTotalProduced());
        assertEquals(10 * operationsPerThread, metrics.getTotalConsumed());
    }

    @Test
    @Timeout(5)
    void testConcurrentReadsAndWrites() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(4);

        // Writer thread 1
        executor.submit(() -> {
            for (int i = 0; i < 500; i++) {
                metrics.recordProduction();
            }
            latch.countDown();
        });

        // Writer thread 2
        executor.submit(() -> {
            for (int i = 0; i < 500; i++) {
                metrics.recordConsumption();
            }
            latch.countDown();
        });

        // Reader thread 1
        executor.submit(() -> {
            for (int i = 0; i < 1000; i++) {
                long produced = metrics.getTotalProduced();
                // Reading should not throw exceptions or return negative
                assertTrue(produced >= 0);
            }
            latch.countDown();
        });

        // Reader thread 2
        executor.submit(() -> {
            for (int i = 0; i < 1000; i++) {
                long consumed = metrics.getTotalConsumed();
                assertTrue(consumed >= 0);
            }
            latch.countDown();
        });

        latch.await();
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        // Final values should be consistent
        assertEquals(500, metrics.getTotalProduced());
        assertEquals(500, metrics.getTotalConsumed());
    }

    // ==========================================
    // INTERFACE COMPLIANCE TESTS
    // ==========================================

    @Test
    void testImplementsMetricsCollector() {
        // Verify it implements the interface
        assertTrue(metrics instanceof MetricsCollector);
    }

    @Test
    void testCanBeUsedPolymorphically() {
        MetricsCollector collector = new SystemMetrics();
        collector.recordProduction();
        collector.recordConsumption();

        assertEquals(1, collector.getTotalProduced());
        assertEquals(1, collector.getTotalConsumed());
    }
}

