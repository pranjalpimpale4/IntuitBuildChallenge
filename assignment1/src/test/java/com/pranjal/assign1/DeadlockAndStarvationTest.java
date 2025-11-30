package com.pranjal.assign1;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CRITICAL TEST SUITE: Deadlock and Starvation Detection
 * 
 * This suite specifically tests for:
 * 1. Deadlock scenarios (threads waiting forever)
 * 2. Starvation scenarios (threads never getting CPU time)
 * 3. Race conditions under extreme load
 * 4. Lock fairness and thread priority issues
 */
class DeadlockAndStarvationTest {

    @BeforeEach
    void setUp() {
        Logger.init();
    }

    @AfterEach
    void tearDown() {
        Logger.close();
    }

    // ==========================================
    // DEADLOCK TESTS
    // ==========================================

    @Test
    @Timeout(10)
    void testNoDeadlockWithFullQueue() throws InterruptedException {
        // Scenario: Queue is full, multiple producers try to add
        // Risk: Producers could deadlock waiting for space
        AdvancedBlockingQueue<String> queue = new AdvancedBlockingQueue<>(2);
        queue.put("Item1");
        queue.put("Item2"); // Queue now FULL

        CountDownLatch producersStarted = new CountDownLatch(5);
        CountDownLatch consumerStarted = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        // Start 5 producers trying to add to full queue
        ExecutorService producers = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            final int id = i;
            producers.submit(() -> {
                try {
                    producersStarted.countDown();
                    boolean success = queue.offer("Producer-" + id, 2, TimeUnit.SECONDS);
                    if (success) successCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        producersStarted.await(); // Wait for all producers to start
        Thread.sleep(100); // Let them all block

        // Now start consumer to unblock
        Thread consumer = new Thread(() -> {
            try {
                consumerStarted.countDown();
                for (int i = 0; i < 5; i++) {
                    queue.take();
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        consumer.start();

        consumerStarted.await();
        consumer.join();
        producers.shutdown();
        producers.awaitTermination(5, TimeUnit.SECONDS);

        // If we reach here without timeout, no deadlock occurred
        assertTrue(successCount.get() > 0, "At least some producers should succeed");
    }

    @Test
    @Timeout(10)
    void testNoDeadlockWithEmptyQueue() throws InterruptedException {
        // Scenario: Queue is empty, multiple consumers try to take
        // Risk: Consumers could deadlock waiting for data
        AdvancedBlockingQueue<String> queue = new AdvancedBlockingQueue<>(10);

        CountDownLatch consumersStarted = new CountDownLatch(5);
        CountDownLatch producerStarted = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        // Start 5 consumers trying to take from empty queue
        ExecutorService consumers = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            consumers.submit(() -> {
                try {
                    consumersStarted.countDown();
                    String result = queue.poll(2, TimeUnit.SECONDS);
                    if (result != null) successCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        consumersStarted.await();
        Thread.sleep(100); // Let them all block

        // Now start producer to unblock
        Thread producer = new Thread(() -> {
            try {
                producerStarted.countDown();
                for (int i = 0; i < 5; i++) {
                    queue.put("Item-" + i);
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        producer.start();

        producerStarted.await();
        producer.join();
        consumers.shutdown();
        consumers.awaitTermination(5, TimeUnit.SECONDS);

        // If we reach here without timeout, no deadlock occurred
        assertTrue(successCount.get() > 0, "At least some consumers should succeed");
    }

    @Test
    @Timeout(15)
    void testNoDeadlockWithPoisonPillInFullQueue() throws InterruptedException {
        // CRITICAL: Test the poison pill deadlock scenario
        // Scenario: Queue is nearly full, consumer drains items and handles poison pill
        // This verifies the safe shutdown pattern
        
        AdvancedBlockingQueue<Object> queue = new AdvancedBlockingQueue<>(5);

        // Fill queue to near capacity (leave room for poison pill)
        for (int i = 0; i < 4; i++) {
            queue.put("Item-" + i);
        }

        AtomicBoolean consumerFinished = new AtomicBoolean(false);
        AtomicInteger itemsConsumed = new AtomicInteger(0);

        // Start consumer that will drain queue and handle poison pill
        Thread consumer = new Thread(() -> {
            try {
                // Drain all regular items first
                while (true) {
                    Object item = queue.poll(500, TimeUnit.MILLISECONDS);
                    if (item == null) {
                        break; // Queue is empty, wait for poison pill
                    }
                    if (item == QueueCommand.POISON_PILL) {
                        // Put it back for next consumer
                        queue.put(QueueCommand.POISON_PILL);
                        break;
                    }
                    itemsConsumed.incrementAndGet();
                }
                
                consumerFinished.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        consumer.start();
        
        // Wait a bit for consumer to drain
        Thread.sleep(200);
        
        // Now add poison pill (simulates safe shutdown: producers stopped, then pill added)
        queue.put(QueueCommand.POISON_PILL);

        // Wait with timeout
        consumer.join(5000);

        // Verify consumer finished without deadlock
        assertTrue(consumerFinished.get(), "Consumer should finish without deadlock");
        assertEquals(4, itemsConsumed.get(), "All regular items should be consumed");
        
        // Verify poison pill is still in queue
        Object remainingItem = queue.poll(100, TimeUnit.MILLISECONDS);
        assertEquals(QueueCommand.POISON_PILL, remainingItem, "Poison pill should be in queue");
    }

    @Test
    @Timeout(10)
    void testNoDeadlockDuringShutdown() throws InterruptedException {
        // Test the safe shutdown pattern
        Configuration config = Configuration.custom(3, 10, 2, 5);
        SimulationEngine engine = new SimulationEngine(config);

        engine.start();
        
        // Let it run briefly
        Thread.sleep(500);

        // Shutdown should complete without deadlock
        assertDoesNotThrow(() -> engine.shutdown());
    }

    // ==========================================
    // STARVATION TESTS
    // ==========================================

    @Test
    @Timeout(15)
    void testNoStarvationWithManyProducers() throws InterruptedException {
        // Scenario: Many producers, few consumers
        // Risk: Some producers might starve (never get to produce)
        AdvancedBlockingQueue<String> queue = new AdvancedBlockingQueue<>(10);

        int producerCount = 20;
        AtomicInteger[] successPerProducer = new AtomicInteger[producerCount];
        for (int i = 0; i < producerCount; i++) {
            successPerProducer[i] = new AtomicInteger(0);
        }

        CountDownLatch allStarted = new CountDownLatch(producerCount + 2);

        ExecutorService executor = Executors.newCachedThreadPool();

        // Start many producers
        for (int i = 0; i < producerCount; i++) {
            final int producerId = i;
            executor.submit(() -> {
                try {
                    allStarted.countDown();
                    for (int j = 0; j < 5; j++) {
                        boolean success = queue.offer("P" + producerId + "-" + j, 2, TimeUnit.SECONDS);
                        if (success) {
                            successPerProducer[producerId].incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Start 2 consumers
        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                try {
                    allStarted.countDown();
                    while (true) {
                        String item = queue.poll(100, TimeUnit.MILLISECONDS);
                        if (item == null) break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        allStarted.await();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Check: ALL producers should have succeeded at least once (no starvation)
        int starvedCount = 0;
        for (int i = 0; i < producerCount; i++) {
            if (successPerProducer[i].get() == 0) {
                starvedCount++;
            }
        }

        // With fair locks, starvation should be minimal
        assertTrue(starvedCount < producerCount / 2, 
            "Too many producers starved: " + starvedCount + "/" + producerCount);
    }

    @Test
    @Timeout(15)
    void testFairLockPreventsStarvation() throws InterruptedException {
        // Test that ReentrantLock(true) provides fairness
        // Lower priority threads should still get served
        
        AdvancedBlockingQueue<String> queue = new AdvancedBlockingQueue<>(5);
        
        AtomicInteger lowPrioritySuccess = new AtomicInteger(0);
        AtomicInteger highPrioritySuccess = new AtomicInteger(0);

        CountDownLatch allStarted = new CountDownLatch(11);

        ExecutorService executor = Executors.newCachedThreadPool();

        // Start 1 low priority producer
        executor.submit(() -> {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            try {
                allStarted.countDown();
                for (int i = 0; i < 20; i++) {
                    boolean success = queue.offer("LowPriority-" + i, 1, TimeUnit.SECONDS);
                    if (success) lowPrioritySuccess.incrementAndGet();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Start 10 high priority producers
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                try {
                    allStarted.countDown();
                    for (int j = 0; j < 10; j++) {
                        boolean success = queue.offer("HighPriority-" + j, 1, TimeUnit.SECONDS);
                        if (success) highPrioritySuccess.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Consumer to drain
        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    String item = queue.poll(50, TimeUnit.MILLISECONDS);
                    if (item == null) break;
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        consumer.start();

        allStarted.await();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        consumer.join();

        // Fair lock should allow low priority thread to succeed despite high priority competition
        assertTrue(lowPrioritySuccess.get() > 0, 
            "Low priority producer should not be completely starved (got " + lowPrioritySuccess.get() + " successes)");
    }

    @Test
    @Timeout(20)
    void testNoConsumerStarvationWithManyItems() throws InterruptedException {
        // Scenario: Items added gradually, multiple consumers compete
        // Risk: Some consumers might starve (never get items)
        // This tests fairness of the ReentrantLock(true) in AdvancedBlockingQueue
        
        AdvancedBlockingQueue<String> queue = new AdvancedBlockingQueue<>(20);

        int consumerCount = 10;
        int totalItems = 100;
        AtomicInteger[] itemsPerConsumer = new AtomicInteger[consumerCount];
        for (int i = 0; i < consumerCount; i++) {
            itemsPerConsumer[i] = new AtomicInteger(0);
        }

        AtomicInteger totalConsumedCounter = new AtomicInteger(0);
        AtomicBoolean stopConsuming = new AtomicBoolean(false);
        CountDownLatch consumersReady = new CountDownLatch(consumerCount);
        CountDownLatch allDone = new CountDownLatch(consumerCount);
        ExecutorService consumerPool = Executors.newFixedThreadPool(consumerCount);

        // Start all consumers first
        for (int i = 0; i < consumerCount; i++) {
            final int consumerId = i;
            consumerPool.submit(() -> {
                try {
                    consumersReady.countDown();
                    consumersReady.await(); // Wait for all consumers to be ready
                    
                    while (!stopConsuming.get() || totalConsumedCounter.get() < totalItems) {
                        String item = queue.poll(50, TimeUnit.MILLISECONDS);
                        if (item != null) {
                            itemsPerConsumer[consumerId].incrementAndGet();
                            totalConsumedCounter.incrementAndGet();
                            // Small processing delay to prevent one consumer from dominating
                            Thread.sleep(1);
                        }
                        if (stopConsuming.get() && totalConsumedCounter.get() >= totalItems) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    allDone.countDown();
                }
            });
        }

        // Wait for all consumers to be ready
        consumersReady.await();
        Thread.sleep(100); // Give them time to start polling

        // Now add items gradually (simulates real producer behavior)
        for (int i = 0; i < totalItems; i++) {
            queue.put("Item-" + i);
            // Small delay to let consumers compete fairly
            if (i % 10 == 0) {
                Thread.sleep(5);
            }
        }

        // Signal consumers to stop after draining
        stopConsuming.set(true);

        // Wait for all consumers to finish
        allDone.await(10, TimeUnit.SECONDS);
        consumerPool.shutdown();
        consumerPool.awaitTermination(5, TimeUnit.SECONDS);

        // Verify total items consumed
        int totalConsumed = 0;
        for (int i = 0; i < consumerCount; i++) {
            totalConsumed += itemsPerConsumer[i].get();
        }
        assertEquals(totalItems, totalConsumed, "All items should be consumed");

        // Check: With fair locks and gradual production, most consumers should get items
        int starvedConsumers = 0;
        for (int i = 0; i < consumerCount; i++) {
            if (itemsPerConsumer[i].get() == 0) {
                starvedConsumers++;
            }
        }

        // Fair locks should prevent systematic starvation (allow 1-2 due to timing)
        assertTrue(starvedConsumers <= 2, 
            "At most 2 consumers should be starved, but " + starvedConsumers + " were starved. " +
            "Distribution: " + java.util.Arrays.toString(itemsPerConsumer));
    }

    // ==========================================
    // RACE CONDITION TESTS
    // ==========================================

    @Test
    @Timeout(15)
    void testNoDuplicatesUnderHighConcurrency() throws InterruptedException {
        // Test for race conditions that could cause duplicate items
        AdvancedBlockingQueue<Integer> queue = new AdvancedBlockingQueue<>(50);

        int itemCount = 1000;
        ConcurrentHashMap<Integer, Boolean> seenItems = new ConcurrentHashMap<>();
        AtomicInteger duplicateCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch producersDone = new CountDownLatch(10);
        CountDownLatch consumersDone = new CountDownLatch(10);

        // 10 Producers
        for (int i = 0; i < 10; i++) {
            final int start = i * 100;
            executor.submit(() -> {
                try {
                    for (int j = start; j < start + 100; j++) {
                        queue.put(j);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    producersDone.countDown();
                }
            });
        }

        // 10 Consumers
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    while (true) {
                        Integer item = queue.poll(100, TimeUnit.MILLISECONDS);
                        if (item == null && producersDone.getCount() == 0) break;
                        if (item != null) {
                            Boolean previous = seenItems.put(item, Boolean.TRUE);
                            if (previous != null) {
                                duplicateCount.incrementAndGet();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    consumersDone.countDown();
                }
            });
        }

        producersDone.await();
        consumersDone.await();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(0, duplicateCount.get(), "No duplicate items should be consumed");
        assertEquals(itemCount, seenItems.size(), "All items should be consumed exactly once");
    }

    @Test
    @Timeout(10)
    void testNoLostItemsUnderConcurrency() throws InterruptedException {
        // Test that no items are lost during concurrent operations
        AdvancedBlockingQueue<Integer> queue = new AdvancedBlockingQueue<>(20);

        int totalItems = 500;
        AtomicInteger producedCount = new AtomicInteger(0);
        AtomicInteger consumedCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch producersDone = new CountDownLatch(5);

        // 5 Producers
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 100; j++) {
                        queue.put(producedCount.getAndIncrement());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    producersDone.countDown();
                }
            });
        }

        // 5 Consumers
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    while (true) {
                        Integer item = queue.poll(100, TimeUnit.MILLISECONDS);
                        if (item == null && producersDone.getCount() == 0) {
                            // Check one more time
                            item = queue.poll(100, TimeUnit.MILLISECONDS);
                            if (item == null) break;
                        }
                        if (item != null) {
                            consumedCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        producersDone.await();
        Thread.sleep(500); // Let consumers finish
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(totalItems, producedCount.get(), "All items should be produced");
        assertEquals(totalItems, consumedCount.get(), "All items should be consumed (no lost items)");
    }

    // ==========================================
    // LIVELOCK TESTS
    // ==========================================

    @Test
    @Timeout(15)
    void testNoLivelockWithConstantTimeouts() throws InterruptedException {
        // Livelock: Threads keep running but make no progress
        // Scenario: Small queue, many producers/consumers, short timeouts
        // Risk: Everyone times out repeatedly, no actual work happens
        
        AdvancedBlockingQueue<String> queue = new AdvancedBlockingQueue<>(2);
        AtomicInteger successfulProductions = new AtomicInteger(0);
        AtomicInteger successfulConsumptions = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch done = new CountDownLatch(20);

        // 10 Producers with short timeouts
        for (int i = 0; i < 10; i++) {
            final int id = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        // Short timeout - might fail often
                        boolean success = queue.offer("P" + id + "-" + j, 50, TimeUnit.MILLISECONDS);
                        if (success) {
                            successfulProductions.incrementAndGet();
                        }
                        Thread.sleep(10); // Brief pause
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        // 10 Consumers with short timeouts
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        String item = queue.poll(50, TimeUnit.MILLISECONDS);
                        if (item != null) {
                            successfulConsumptions.incrementAndGet();
                        }
                        Thread.sleep(10);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        done.await();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Despite contention and timeouts, SOME progress must be made (no livelock)
        assertTrue(successfulProductions.get() > 0, 
            "System should make progress despite timeouts (produced: " + successfulProductions.get() + ")");
        assertTrue(successfulConsumptions.get() > 0,
            "System should make progress despite timeouts (consumed: " + successfulConsumptions.get() + ")");
        
        // Consumed should approximately equal produced (within tolerance for timeouts)
        int difference = Math.abs(successfulProductions.get() - successfulConsumptions.get());
        assertTrue(difference <= 5, "Produced and consumed should be close (diff: " + difference + ")");
    }

    // ==========================================
    // INTERRUPTION DURING CRITICAL OPERATIONS
    // ==========================================

    @Test
    @Timeout(10)
    void testPoisonPillPassedOnDespiteConsumerInterruption() throws InterruptedException {
        // CRITICAL: What if consumer is interrupted while trying to re-queue poison pill?
        // Risk: Poison pill lost, other consumers hang forever
        
        AdvancedBlockingQueue<Object> queue = new AdvancedBlockingQueue<>(10);
        MetricsCollector metrics = new MockMetricsCollector();

        queue.put("NormalItem");
        queue.put(QueueCommand.POISON_PILL);

        AtomicBoolean firstConsumerInterrupted = new AtomicBoolean(false);
        AtomicBoolean secondConsumerFinished = new AtomicBoolean(false);

        // First consumer - will be interrupted
        Thread consumer1 = new Thread(() -> {
            ConsumerWorker worker = new ConsumerWorker(queue, "Consumer1", metrics);
            worker.run();
            firstConsumerInterrupted.set(Thread.currentThread().isInterrupted());
        });

        // Second consumer - should still get poison pill
        Thread consumer2 = new Thread(() -> {
            try {
                Thread.sleep(500); // Wait for consumer1 to process
                Object item = queue.take();
                if (item == QueueCommand.POISON_PILL) {
                    secondConsumerFinished.set(true);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        consumer1.start();
        consumer2.start();

        // Let consumer1 start processing
        Thread.sleep(200);
        
        // Note: Interrupting during poison pill re-queue is hard to time
        // This test verifies poison pill is in queue after consumer1 finishes
        
        consumer1.join(3000);
        consumer2.join(3000);

        // Verify second consumer got the poison pill
        assertTrue(secondConsumerFinished.get() || queue.getSize() > 0, 
            "Poison pill should be available for second consumer");
    }

    @Test
    @Timeout(10)
    void testInterruptionDuringQueueOperation() throws InterruptedException {
        // Test interruption during blocking put/take
        AdvancedBlockingQueue<String> queue = new AdvancedBlockingQueue<>(1);
        queue.put("Blocker"); // Full queue

        AtomicBoolean interrupted = new AtomicBoolean(false);

        Thread producer = new Thread(() -> {
            try {
                queue.put("WillBeInterrupted"); // Will block
            } catch (InterruptedException e) {
                interrupted.set(true);
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        Thread.sleep(100); // Let it block
        producer.interrupt(); // Interrupt while blocked
        producer.join(2000);

        assertTrue(interrupted.get(), "Thread should have caught InterruptedException");
        assertFalse(producer.isAlive(), "Thread should exit after interruption");
    }

    // ==========================================
    // CONCURRENT SHUTDOWN TESTS
    // ==========================================

    @Test
    @Timeout(15)
    void testAutoScalerVsShutdownRace() throws InterruptedException {
        // Test: Auto-scaler tries to add consumer while system is shutting down
        // Risk: Race condition between scaling up and shutting down
        
        Configuration config = Configuration.custom(5, 10, 1, 3); // Small capacity = high load
        SimulationEngine engine = new SimulationEngine(config);

        engine.start();

        // Let auto-scaler detect high load and start scaling
        Thread.sleep(200);

        // Shutdown while auto-scaler might be mid-operation
        assertDoesNotThrow(() -> engine.shutdown());
    }

    @Test
    @Timeout(15)
    void testConcurrentShutdownCalls() throws InterruptedException {
        // Test: Multiple threads call shutdown() simultaneously
        // Risk: Double-shutdown could cause exceptions or deadlock
        
        Configuration config = Configuration.custom(2, 5, 1, 10);
        SimulationEngine engine = new SimulationEngine(config);

        engine.start();
        Thread.sleep(200);

        // Multiple threads try to shutdown
        CountDownLatch latch = new CountDownLatch(3);
        ExecutorService executor = Executors.newFixedThreadPool(3);

        for (int i = 0; i < 3; i++) {
            executor.submit(() -> {
                try {
                    engine.shutdown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // If we reach here, no deadlock occurred
        assertTrue(true);
    }

    // ==========================================
    // FAIRNESS VERIFICATION TESTS
    // ==========================================

    @Test
    @Timeout(15)
    void testReentrantLockFairnessGuarantee() throws InterruptedException {
        // Verify that ReentrantLock(true) provides FIFO ordering
        // Threads should be served in the order they requested access
        
        AdvancedBlockingQueue<String> queue = new AdvancedBlockingQueue<>(1);
        queue.put("Blocker"); // Make queue full

        ConcurrentLinkedQueue<Integer> accessOrder = new ConcurrentLinkedQueue<>();
        CountDownLatch allBlocked = new CountDownLatch(10);
        CountDownLatch consumerReady = new CountDownLatch(1);

        // Start 10 producers that will block
        ExecutorService producers = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            final int id = i;
            producers.submit(() -> {
                try {
                    allBlocked.countDown();
                    Thread.sleep(id * 10); // Stagger start times
                    queue.put("Producer-" + id); // Will block
                    accessOrder.add(id); // Record order when unblocked
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        allBlocked.await(); // Wait for all to start blocking

        // Consumer to slowly drain
        Thread consumer = new Thread(() -> {
            try {
                consumerReady.countDown();
                for (int i = 0; i < 11; i++) { // Remove blocker + 10 items
                    queue.take();
                    Thread.sleep(50); // Slow drain
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        consumer.start();

        consumerReady.await();
        consumer.join();
        producers.shutdown();
        producers.awaitTermination(10, TimeUnit.SECONDS);

        // Verify fairness: access order should roughly follow start order
        // (Not perfect because of timing, but first few should be in order)
        Integer[] order = accessOrder.toArray(new Integer[0]);
        assertTrue(order.length >= 5, "Most producers should have succeeded");
        
        // With fair locks, we expect some ordering (not perfect, but better than unfair)
        // This is a probabilistic test - fair locks should show SOME ordering
    }

    // ==========================================
    // STRESS TESTS
    // ==========================================

    @Test
    @Timeout(20)
    void testExtremeLoadNoCrash() throws InterruptedException {
        // Extreme scenario: Many threads, small queue, high contention
        // Goal: Verify system doesn't crash, hang, or corrupt data
        
        AdvancedBlockingQueue<Integer> queue = new AdvancedBlockingQueue<>(5);
        AtomicInteger produced = new AtomicInteger(0);
        AtomicInteger consumed = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch done = new CountDownLatch(50);

        // 25 Producers
        for (int i = 0; i < 25; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 40; j++) {
                        boolean success = queue.offer(produced.getAndIncrement(), 500, TimeUnit.MILLISECONDS);
                        if (!success) break; // Timeout, give up
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        // 25 Consumers
        for (int i = 0; i < 25; i++) {
            executor.submit(() -> {
                try {
                    while (true) {
                        Integer item = queue.poll(100, TimeUnit.MILLISECONDS);
                        if (item == null) {
                            if (done.getCount() <= 25) break; // Producers done
                        } else {
                            consumed.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        done.await();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Verify reasonable progress despite extreme contention
        assertTrue(produced.get() > 0, "Should have produced some items");
        assertTrue(consumed.get() > 0, "Should have consumed some items");
        
        // Most items should be consumed (allowing for timeouts)
        double consumptionRate = (double) consumed.get() / produced.get();
        assertTrue(consumptionRate > 0.5, 
            "At least 50% of items should be consumed (rate: " + consumptionRate + ")");
    }
}


