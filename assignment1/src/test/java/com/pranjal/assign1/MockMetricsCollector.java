package com.pranjal.assign1;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple mock implementation of MetricsCollector for testing.
 * Demonstrates how easy it is to create alternative implementations.
 */
public class MockMetricsCollector implements MetricsCollector {
    
    private final AtomicLong produced = new AtomicLong(0);
    private final AtomicLong consumed = new AtomicLong(0);
    
    @Override
    public void recordProduction() {
        produced.incrementAndGet();
    }
    
    @Override
    public void recordConsumption() {
        consumed.incrementAndGet();
    }
    
    @Override
    public long getTotalProduced() {
        return produced.get();
    }
    
    @Override
    public long getTotalConsumed() {
        return consumed.get();
    }
}

