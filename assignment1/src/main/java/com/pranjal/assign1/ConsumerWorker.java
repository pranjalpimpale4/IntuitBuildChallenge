package com.pranjal.assign1;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Consumer worker that processes items from the queue.
 * 
 * Continues processing until receiving POISON_PILL signal,
 * which it relays to other consumers before exiting.
 * 
 * @author Pranjal
 * @version 2.1
 */
public class ConsumerWorker implements Runnable {

    private final AdvancedBlockingQueue<Object> queue;
    private final String name;
    private final MetricsCollector metrics;

    /**
     * Constructs a consumer with a custom name.
     * 
     * @param queue the shared queue to consume from
     * @param name consumer name for logging
     * @param metrics metrics collector for recording events
     */
    public ConsumerWorker(AdvancedBlockingQueue<Object> queue, String name, 
                         MetricsCollector metrics) {
        this.queue = queue;
        this.name = name;
        this.metrics = metrics;
    }
    
    /**
     * Constructs a consumer with auto-generated name.
     * 
     * @param queue the shared queue to consume from
     * @param id numeric identifier for name generation
     * @param metrics metrics collector for recording events
     */
    public ConsumerWorker(AdvancedBlockingQueue<Object> queue, int id, 
                         MetricsCollector metrics) {
        this(queue, "Consumer-" + id, metrics);
    }

    @Override
    public void run() {
        try {
            Logger.log(name, "STARTED.");
            
            while (true) {
                Object item = queue.take();
                
                // Check for shutdown signal
                if (item == QueueCommand.POISON_PILL) {
                    Logger.log(name, "Received POISON PILL. Stopping.");
                    queue.put(QueueCommand.POISON_PILL); // Relay to other consumers
                    break; 
                }

                metrics.recordConsumption();
                Logger.log(name, "PROCESSED " + item + " | Queue Size: " + queue.getSize());

                // Simulate processing time
                Thread.sleep(ThreadLocalRandom.current().nextInt(50, 250));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.log(name, "INTERRUPTED.");
        }
        Logger.log(name, "SHUTDOWN complete.");
    }
}
