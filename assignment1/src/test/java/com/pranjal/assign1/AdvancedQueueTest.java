package com.pranjal.assign1;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * UNIT TESTS for AdvancedBlockingQueue
 * ------------------------------------------------------------------
 * Focus: Verifies the internal logic of the queue data structure.
 * Covers: 
 * - [x] FIFO Ordering
 * - [x] Capacity Constraints
 * - [x] Blocking Mechanics (Wait/Notify)
 * - [x] Edge-Case Handling (Timeouts)
 */
class AdvancedQueueTest {

    // ==========================================
    // 1. FUNCTIONAL INTEGRITY TESTS ("The Basics")
    // ==========================================

    @Test
    void testFIFOOrdering() throws InterruptedException {
        // Goal: Verify data comes out in the same order it went in.
        AdvancedBlockingQueue<String> queue = new AdvancedBlockingQueue<>(10);

        queue.put("First");
        queue.put("Second");
        queue.put("Third");

        assertEquals("First", queue.take());
        assertEquals("Second", queue.take());
        assertEquals("Third", queue.take());
    }

    @Test
    void testCapacityLimit() throws InterruptedException {
        // Goal: Verify the queue reports size correctly.
        AdvancedBlockingQueue<Integer> queue = new AdvancedBlockingQueue<>(2);

        assertEquals(0, queue.getSize());
        queue.put(1);
        assertEquals(1, queue.getSize());
        queue.put(2);
        assertEquals(2, queue.getSize());
        
        // Remove one and verify size drops
        queue.take();
        assertEquals(1, queue.getSize());
    }

    // ==========================================
    // 2. CONCURRENCY LOGIC TESTS ("The Hard Stuff")
    // ==========================================

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testBlockingTakeOnEmpty() {
        // Goal: Verify take() BLOCKS if the queue is empty (doesn't return null immediately).
        AdvancedBlockingQueue<String> queue = new AdvancedBlockingQueue<>(10);

        // Start a thread that will add data in 500ms
        Thread delayedProducer = new Thread(() -> {
            try {
                Thread.sleep(500); 
                queue.put("Data");
            } catch (InterruptedException e) {}
        });
        delayedProducer.start();

        long start = System.currentTimeMillis();
        try {
            String result = queue.take(); // Should wait here
            assertEquals("Data", result);
        } catch (InterruptedException e) {
            fail("Interrupted");
        }
        long duration = System.currentTimeMillis() - start;

        // Assertion: If it was instant (wrong), duration would be ~0ms.
        assertTrue(duration >= 400, "Consumer should have waited for at least ~500ms");
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testBlockingPutOnFull() throws InterruptedException {
        // Goal: Verify put() BLOCKS if the queue is full.
        AdvancedBlockingQueue<String> queue = new AdvancedBlockingQueue<>(1);
        queue.put("Full"); // Queue is now full (Capacity 1)

        // Start a thread that will free space in 500ms
        Thread delayedConsumer = new Thread(() -> {
            try {
                Thread.sleep(500);
                queue.take(); // Frees space
            } catch (InterruptedException e) {}
        });
        delayedConsumer.start();

        long start = System.currentTimeMillis();
        queue.put("New Data"); // Should wait here
        long duration = System.currentTimeMillis() - start;

        assertTrue(duration >= 400, "Producer should have waited for space to open up");
    }

    // ==========================================
    // 3. TIMEOUT & EDGE CASE TESTS (Legendary Features)
    // ==========================================

    @Test
    void testOfferTimeout() throws InterruptedException {
        // Goal: Verify offer() returns FALSE after timeout if queue remains full.
        // This tests "Backpressure Handling".
        AdvancedBlockingQueue<String> queue = new AdvancedBlockingQueue<>(1);
        queue.put("Blocker");

        long start = System.currentTimeMillis();
        // Try to add "Fail" for 500ms, then give up
        boolean success = queue.offer("Fail", 500, TimeUnit.MILLISECONDS);
        long duration = System.currentTimeMillis() - start;

        assertFalse(success, "Offer should return false when timed out");
        assertTrue(duration >= 400, "Should have waited for the full timeout duration");
    }

    @Test
    void testPollTimeout() throws InterruptedException {
        // Goal: Verify poll() returns NULL after timeout if queue remains empty.
        AdvancedBlockingQueue<String> queue = new AdvancedBlockingQueue<>(10);

        long start = System.currentTimeMillis();
        // Try to take data for 500ms, then give up
        String result = queue.poll(500, TimeUnit.MILLISECONDS);
        long duration = System.currentTimeMillis() - start;

        assertNull(result, "Poll should return null when timed out");
        assertTrue(duration >= 400, "Should have waited for the full timeout duration");
    }

    // ==========================================
    // 4. DEFENSIVE CODING CHECKS (The 10/10 Edge Cases)
    // ==========================================

    @Test
    void testNullInsertionThrowsException() {
        // Goal: Verify the queue rejects nulls immediately.
        AdvancedBlockingQueue<String> queue = new AdvancedBlockingQueue<>(5);
        
        // These assertions check that your code throws NullPointerException
        // instad of silently failing or adding null.
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            queue.put(null);
        });
        
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            queue.offer(null, 1, TimeUnit.SECONDS);
        });
    }

    @Test
    void testConstructorValidation() {
        // Goal: Verify we cannot create broken queues.
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new AdvancedBlockingQueue<>(0);
        });
        
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new AdvancedBlockingQueue<>(-1);
        });
    }
}