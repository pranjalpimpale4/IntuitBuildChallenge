package com.pranjal.assign1;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Real-time dashboard for monitoring system status.
 * 
 * Updates every 500ms to display queue status, thread counts,
 * and production/consumption metrics.
 * 
 * @author Pranjal
 * @version 2.1
 */
public class Dashboard {

    private final AdvancedBlockingQueue<Object> queue;
    private final ThreadManager manager;
    private final MetricsCollector metrics;
    private final ScheduledExecutorService scheduler;

    /**
     * Constructs dashboard with all required dependencies.
     * 
     * @param queue shared queue to monitor
     * @param manager thread manager to monitor
     * @param metrics metrics collector to monitor
     */
    public Dashboard(AdvancedBlockingQueue<Object> queue, ThreadManager manager, 
                    MetricsCollector metrics) {
        this.queue = queue;
        this.manager = manager;
        this.metrics = metrics;
        
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("Dashboard-Updater");
            t.setDaemon(true); // Won't prevent JVM shutdown
            return t;
        });
    }

    /**
     * Starts dashboard with 500ms update interval.
     */
    public void start() {
        System.out.println("\n\n\n");
        
        // Update every 500ms (2 FPS)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                printFrame();
            } catch (Exception e) {
                System.err.println("Dashboard error: " + e.getMessage());
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops dashboard and prints final state.
     */
    public void stop() {
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            printFrame();
            System.out.println("\n=== DASHBOARD CLOSED ===");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduler.shutdownNow();
        }
    }

    /**
     * Renders a single dashboard frame with progress bar and metrics.
     */
    private void printFrame() {
        int size = queue.getSize();
        int capacity = queue.getCapacity();
        int barLength = 20;
        int fill = (int) ((double) size / capacity * barLength);
        
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            bar.append(i < fill ? "#" : "-");
        }
        bar.append("]");

        String status = String.format(
            "\r [SYSTEM STATUS] Queue: %-22s %3d/%d | Producers: %d | Consumers: %d | Total In: %d | Total Out: %d",
            bar.toString(), size, capacity, 
            manager.getProducerCount(), manager.getConsumerCount(),
            metrics.getTotalProduced(), metrics.getTotalConsumed()
        );
        
        System.out.print(status);
    }
}
