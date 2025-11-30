package com.pranjal.assign1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages worker threads using ExecutorService framework.
 * 
 * Handles producers, consumers, and auto-scaling monitor using thread pools.
 * Implements safe shutdown pattern to prevent deadlocks.
 * 
 * @author Pranjal
 * @version 2.1
 */
public class ThreadManager {

    private final AdvancedBlockingQueue<Object> queue;
    private final MetricsCollector metrics;
    
    // Thread pools for different worker types
    private final ExecutorService producerPool;
    private final ExecutorService consumerPool;
    private final ScheduledExecutorService autoScalerPool;
    
    // Atomic counters for active thread tracking
    private final AtomicInteger producerCount = new AtomicInteger(0);
    private final AtomicInteger consumerCount = new AtomicInteger(0);
    private final AtomicInteger emergencyConsumerCount = new AtomicInteger(0);

    /**
     * Constructs ThreadManager with specified queue and metrics.
     * 
     * @param queue shared blocking queue
     * @param metrics metrics collector for workers
     */
    public ThreadManager(AdvancedBlockingQueue<Object> queue, MetricsCollector metrics) {
        this.queue = queue;
        this.metrics = metrics;
        
        // CachedThreadPool creates threads as needed, reuses idle threads
        this.producerPool = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setName("Producer-Pool-" + t.getId());
            return t;
        });
        
        this.consumerPool = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setName("Consumer-Pool-" + t.getId());
            return t;
        });
        
        this.autoScalerPool = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("AutoScaler-Monitor");
            return t;
        });
    }

    /**
     * Starts the specified number of producer threads.
     * 
     * @param count number of producers to start
     * @param itemsPerProducer items each producer generates
     */
    public void startProducers(int count, int itemsPerProducer) {
        for (int i = 1; i <= count; i++) {
            final int producerId = i;
            producerCount.incrementAndGet();
            
            producerPool.submit(() -> {
                try {
                    new ProducerWorker(queue, producerId, itemsPerProducer, metrics).run();
                } finally {
                    producerCount.decrementAndGet();
                }
            });
        }
    }

    /**
     * Starts the specified number of consumer threads.
     * 
     * @param count number of consumers to start
     */
    public void startConsumers(int count) {
        for (int i = 1; i <= count; i++) {
            startSingleConsumer("Consumer-" + i);
        }
    }

    /**
     * Starts a single consumer with the given name.
     * 
     * @param name consumer name for logging
     */
    private void startSingleConsumer(String name) {
        consumerCount.incrementAndGet();
        
        consumerPool.submit(() -> {
            try {
                new ConsumerWorker(queue, name, metrics).run();
            } finally {
                consumerCount.decrementAndGet();
            }
        });
    }

    /**
     * Starts the auto-scaling monitor that adds emergency consumers when load exceeds 75%.
     * Checks queue load every second and can deploy up to 3 emergency consumers.
     */
    public void runAutoScalerLoop() {
        Logger.log("AUTO-SCALER", "Monitor Started.");
        
        // ScheduledExecutorService is more efficient than manual sleep loops
        autoScalerPool.scheduleWithFixedDelay(() -> {
            try {
                int size = queue.getSize();
                int capacity = queue.getCapacity();
                double load = (double) size / capacity;

                if (load > 0.75 && emergencyConsumerCount.get() < 3) {
                    int id = emergencyConsumerCount.incrementAndGet();
                    String msg = "High Load detected (" + (load*100) + "%). Deploying Emergency Consumer " + id;
                    Logger.log("AUTO-SCALER", msg);
                    System.out.println(">>> [AUTO-SCALER] " + msg); 
                    startSingleConsumer("EmergencyConsumer-" + id);
                }
            } catch (Exception e) {
                Logger.log("AUTO-SCALER", "Error in monitoring loop: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
    
    /**
     * Shuts down producers and auto-scaler (Phase 1 of safe shutdown).
     * WAITS for all producers to finish before returning.
     * 
     * Safe Shutdown Pattern:
     * 1. Call this method - stops and waits for producers
     * 2. Insert poison pill - safe because queue can only shrink now
     * 3. Call waitForConsumers() - consumers drain and exit
     */
    public void shutdown() {
        Logger.log("AUTO-SCALER", "Initiating thread pool shutdown...");
        
        // Stop accepting new tasks
        autoScalerPool.shutdown();
        producerPool.shutdown();
        consumerPool.shutdown();
        
        try {
            // Wait for auto-scaler
            if (!autoScalerPool.awaitTermination(2, TimeUnit.SECONDS)) {
                autoScalerPool.shutdownNow();
            }
            
            // CRITICAL: Wait for all producers to finish
            Logger.log("AUTO-SCALER", "Waiting for producers to finish...");
            if (!producerPool.awaitTermination(10, TimeUnit.SECONDS)) {
                Logger.log("AUTO-SCALER", "Timeout - force-stopping producer pool");
                producerPool.shutdownNow();
                producerPool.awaitTermination(2, TimeUnit.SECONDS);
            }
            Logger.log("AUTO-SCALER", "All producers finished. Queue can only shrink now.");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            autoScalerPool.shutdownNow();
            producerPool.shutdownNow();
        }
        
        Logger.log("AUTO-SCALER", "Producer shutdown complete.");
    }

    /**
     * Waits for consumers to finish (Phase 3 of safe shutdown).
     * Must be called AFTER shutdown() and poison pill insertion.
     */
    public void waitForConsumers() {
        try {
            Logger.log("SYSTEM", "Waiting up to 30 seconds for consumers to drain queue...");
            
            if (!consumerPool.awaitTermination(30, TimeUnit.SECONDS)) {
                Logger.log("SYSTEM", "Timeout waiting for consumers, forcing shutdown");
                consumerPool.shutdownNow();
                
                if (!consumerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    Logger.log("SYSTEM", "Some consumers did not terminate gracefully");
                }
            }
            Logger.log("SYSTEM", "All consumers have shut down.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            consumerPool.shutdownNow();
        }
    }
    
    public int getProducerCount() { 
        return producerCount.get(); 
    }
    
    public int getConsumerCount() { 
        return consumerCount.get(); 
    }
    
    public int getEmergencyConsumerCount() {
        return emergencyConsumerCount.get();
    }
}
