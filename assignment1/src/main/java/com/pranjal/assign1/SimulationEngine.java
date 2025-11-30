package com.pranjal.assign1;

/**
 * Orchestrates the complete producer-consumer simulation lifecycle.
 * 
 * Creates and wires all components using constructor injection,
 * implements safe shutdown pattern to prevent deadlocks.
 * 
 * @author Pranjal
 * @version 2.1
 */
public class SimulationEngine {
    
    private final Configuration config;
    private final AdvancedBlockingQueue<Object> queue;
    private final ThreadManager threadManager;
    private final Dashboard dashboard;
    private final SystemMetrics metrics;
    
    /**
     * Constructs simulation engine with validated configuration.
     * Creates and wires all components with proper dependency injection.
     * 
     * @param config simulation parameters
     * @throws IllegalArgumentException if configuration invalid
     */
    public SimulationEngine(Configuration config) {
        this.config = config;
        config.validate();
        
        // Create shared components (no dependencies)
        this.queue = new AdvancedBlockingQueue<>(config.getQueueCapacity());
        this.metrics = new SystemMetrics(); // Breaks circular dependency
        
        // Create dependent components
        this.threadManager = new ThreadManager(queue, metrics);
        this.dashboard = new Dashboard(queue, threadManager, metrics);
    }
    
    /**
     * Starts dashboard, consumers, producers, and auto-scaler.
     */
    public void start() {
        Logger.log("SYSTEM", "Initializing simulation with " + config);
        System.out.println("Starting System...");
        
        dashboard.start();
        threadManager.startConsumers(config.getNumberOfConsumers());
        threadManager.startProducers(config.getNumberOfProducers(), 
                                     config.getItemsPerProducer());
        threadManager.runAutoScalerLoop();
    }
    
    /**
     * Waits for all expected items to be consumed.
     * Polls metrics every 100ms.
     * 
     * @throws InterruptedException if interrupted while waiting
     */
    public void waitForCompletion() throws InterruptedException {
        int totalExpected = config.getTotalExpectedItems();
        Logger.log("SYSTEM", "Waiting for " + totalExpected + " items to be processed...");
        
        while (metrics.getTotalConsumed() < totalExpected) {
            Thread.sleep(100);
        }
        
        Logger.log("SYSTEM", "All " + totalExpected + " items processed.");
    }
    
    /**
     * Gracefully shuts down using safe shutdown pattern.
     * 
     * Safe Shutdown Pattern (prevents deadlock):
     * 1. Stop producers and wait for them to finish
     * 2. Insert poison pill (safe because queue can only shrink)
     * 3. Wait for consumers to drain and exit
     * 
     * @throws InterruptedException if interrupted during shutdown
     */
    public void shutdown() throws InterruptedException {
        System.out.println("\n\nSystem Stopping...");
        Logger.log("SYSTEM", "Initiating graceful shutdown...");
        
        // Phase 1: Stop producers
        Logger.log("SYSTEM", "Stopping producers...");
        dashboard.stop();
        threadManager.shutdown(); // Waits for producers to finish
        Logger.log("SYSTEM", "All producers stopped. Queue can only shrink now.");
        
        // Phase 2: Insert poison pill (safe now)
        Logger.log("SYSTEM", "Inserting poison pill for consumers...");
        queue.put(QueueCommand.POISON_PILL);
        
        // Phase 3: Wait for consumers
        Logger.log("SYSTEM", "Waiting for consumers to finish draining queue...");
        threadManager.waitForConsumers();
        
        Logger.log("SYSTEM", "Shutdown complete - all workers stopped gracefully.");
    }
    
    /**
     * Runs complete simulation: start, wait, shutdown.
     */
    public void runSimulation() {
        try {
            start();
            waitForCompletion();
            shutdown();
            printAnalysisResults();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.log("SYSTEM", "Simulation interrupted: " + e.getMessage());
        }
    }
    
    /**
     * Prints comprehensive analysis results to console.
     * Shows configuration, execution results, and system statistics.
     */
    void printAnalysisResults() {
        System.out.println("\n\n");
        System.out.println("=".repeat(70));
        System.out.println("                    SIMULATION ANALYSIS RESULTS");
        System.out.println("=".repeat(70));
        
        // Configuration Summary
        System.out.println("\n[CONFIGURATION]");
        System.out.println("  Producers Started:        " + config.getNumberOfProducers());
        System.out.println("  Items per Producer:       " + config.getItemsPerProducer());
        System.out.println("  Consumers Started:        " + config.getNumberOfConsumers());
        System.out.println("  Emergency Consumers Added: " + threadManager.getEmergencyConsumerCount());
        System.out.println("  Queue Capacity:           " + config.getQueueCapacity());
        System.out.println("  Total Items Expected:     " + config.getTotalExpectedItems());
        
        // Execution Results
        System.out.println("\n[EXECUTION RESULTS]");
        System.out.println("  Items Produced:           " + metrics.getTotalProduced());
        System.out.println("  Items Consumed:           " + metrics.getTotalConsumed());
        System.out.println("  Final Queue Size:         " + queue.getSize());
        
        // Calculate success metrics
        int expected = config.getTotalExpectedItems();
        long produced = metrics.getTotalProduced();
        long consumed = metrics.getTotalConsumed();
        boolean success = (consumed == expected);
        
        System.out.println("\n[ANALYSIS]");
        System.out.println("  Production Rate:          " + 
            String.format("%.1f%%", (produced * 100.0 / expected)));
        System.out.println("  Consumption Rate:         " + 
            String.format("%.1f%%", (consumed * 100.0 / expected)));
        System.out.println("  Status:                   " + 
            (success ? "SUCCESS - All items processed" : "INCOMPLETE"));
        
        // System Summary
        int totalConsumersUsed = config.getNumberOfConsumers() + threadManager.getEmergencyConsumerCount();
        System.out.println("\n[SYSTEM SUMMARY]");
        System.out.println("  Total Consumers Used:     " + totalConsumersUsed + 
            " (" + config.getNumberOfConsumers() + " initial + " + 
            threadManager.getEmergencyConsumerCount() + " emergency)");
        System.out.println("  Shutdown Status:          " + 
            (threadManager.getProducerCount() == 0 && threadManager.getConsumerCount() == 0 ? 
            "Clean (All threads terminated)" : "WARNING: Threads still active"));
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("=".repeat(70) + "\n");
        
        // Highlight log file information - IMPORTANT OUTPUT COMPONENT
        System.out.println("█".repeat(70));
        System.out.println("█" + " ".repeat(68) + "█");
        System.out.println("█" + " ".repeat(15) + "*** IMPORTANT: EXECUTION LOG FILE ***" + " ".repeat(15) + "█");
        System.out.println("█" + " ".repeat(68) + "█");
        System.out.println("█".repeat(70));
        System.out.println();
        System.out.println("  >>> LOG FILE LOCATION: assignment1/execution_history.log <<<");
        System.out.println();
        System.out.println("  This log file is one of the MOST IMPORTANT parts of the output!");
        System.out.println();
        System.out.println("  WHAT IT CONTAINS:");
        System.out.println("    • Timestamped events for all producers and consumers");
        System.out.println("    • Queue operations and state changes");
        System.out.println("    • Auto-scaling decisions and emergency consumer deployments");
        System.out.println("    • System lifecycle events (startup, shutdown, errors)");
        System.out.println("    • Detailed debugging information for every operation");
        System.out.println();
        System.out.println("  WHY IT'S IMPORTANT:");
        System.out.println("    • This log file was used extensively for debugging the system");
        System.out.println("    • It provides complete traceability of all system operations");
        System.out.println("    • Essential for analyzing system behavior and performance");
        System.out.println("    • Critical for troubleshooting and verification");
        System.out.println();
        System.out.println("  WHY IT'S NOT PRINTED TO CONSOLE:");
        System.out.println("    • The log file can be VERY LARGE (thousands of lines)");
        System.out.println("    • Printing it to console would clutter and overwhelm the output");
        System.out.println("    • Console output is meant for summary and real-time status");
        System.out.println("    • Detailed logs are better viewed in a text editor or log viewer");
        System.out.println();
        System.out.println("  TO VIEW THE LOGS:");
        System.out.println("    Open the file: assignment1/execution_history.log");
        System.out.println("    (Use a text editor or log viewer for best experience)");
        System.out.println();
        System.out.println("█".repeat(70));
        System.out.println();
        
        Logger.log("SYSTEM", "Analysis results printed to console.");
    }
    
    public AdvancedBlockingQueue<Object> getQueue() {
        return queue;
    }
    
    public ThreadManager getThreadManager() {
        return threadManager;
    }
    
    public Configuration getConfig() {
        return config;
    }
}
