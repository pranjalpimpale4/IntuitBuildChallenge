package com.pranjal.assign1;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Centralized metrics collection using AtomicLong for thread safety.
 * 
 * Breaks circular dependency between Dashboard and ThreadManager
 * by providing shared state both can reference.
 * 
 * @author Pranjal
 * @version 2.1
 */
public class SystemMetrics implements MetricsCollector {
    
    // AtomicLong provides lock-free thread-safe increments
    private final AtomicLong producedTotal = new AtomicLong(0);
    private final AtomicLong consumedTotal = new AtomicLong(0);
    
    @Override
    public void recordProduction() {
        producedTotal.incrementAndGet();
    }
    
    @Override
    public void recordConsumption() {
        consumedTotal.incrementAndGet();
    }
    
    @Override
    public long getTotalProduced() {
        return producedTotal.get();
    }
    
    @Override
    public long getTotalConsumed() {
        return consumedTotal.get();
    }
}
