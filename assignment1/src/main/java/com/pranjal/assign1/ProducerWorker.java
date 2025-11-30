package com.pranjal.assign1;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Producer worker that generates items and adds them to the queue.
 * 
 * Uses dependency injection for metrics collection and ThreadLocalRandom
 * for better performance in concurrent environments.
 * 
 * @author Pranjal
 * @version 2.1
 */
public class ProducerWorker implements Runnable {

    private final AdvancedBlockingQueue<Object> queue;
    private final int id;
    private final int itemsToProduce;
    private final MetricsCollector metrics;

    /**
     * Constructs a producer worker.
     * 
     * @param queue the shared queue to add items to
     * @param id unique identifier for this producer
     * @param itemsToProduce number of items to generate
     * @param metrics metrics collector for recording events
     */
    public ProducerWorker(AdvancedBlockingQueue<Object> queue, int id, 
                         int itemsToProduce, MetricsCollector metrics) {
        this.queue = queue;
        this.id = id;
        this.itemsToProduce = itemsToProduce;
        this.metrics = metrics;
    }

    @Override
    public void run() {
        try {
            Logger.log("Producer-" + id, "STARTED.");
            
            for (int i = 1; i <= itemsToProduce; i++) {
                // ThreadLocalRandom eliminates contention vs shared Random
                Thread.sleep(ThreadLocalRandom.current().nextInt(50, 150)); 
                
                String data = "Record-" + id + "-" + i;
                
                // Use timeout to avoid indefinite blocking
                boolean success = queue.offer(data, 2, TimeUnit.SECONDS);
                
                if (success) {
                    metrics.recordProduction();
                    Logger.log("Producer-" + id, "ADDED " + data + " | Queue Size: " + queue.getSize());
                } else {
                    Logger.log("Producer-" + id, "TIMED OUT waiting to add " + data);
                }
            }
            
            Logger.log("Producer-" + id, "FINISHED work.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.log("Producer-" + id, "INTERRUPTED.");
        }
    }
}
