package com.pranjal.assign1;

/**
 * Interface for collecting production and consumption metrics.
 * 
 * Enables dependency injection - workers depend on this abstraction
 * rather than concrete implementations like Dashboard.
 * All methods must be thread-safe.
 * 
 * @author Pranjal
 * @version 2.1
 */
public interface MetricsCollector {
    
    /**
     * Records that an item was produced.
     * Thread-safe and non-blocking.
     */
    void recordProduction();
    
    /**
     * Records that an item was consumed.
     * Thread-safe and non-blocking.
     */
    void recordConsumption();
    
    /**
     * Returns total items produced since system start.
     * 
     * @return cumulative production count, never negative
     */
    long getTotalProduced();
    
    /**
     * Returns total items consumed since system start.
     * 
     * @return cumulative consumption count, never negative
     */
    long getTotalConsumed();
}
