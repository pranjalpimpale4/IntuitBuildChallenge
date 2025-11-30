package com.pranjal.assign1;

import java.util.Scanner;

/**
 * Immutable configuration for simulation parameters.
 * 
 * Provides factory methods for different creation modes:
 * interactive, default, and custom programmatic configuration.
 * 
 * @author Pranjal
 * @version 2.1
 */
public class Configuration {
    
    private final int numberOfProducers;
    private final int itemsPerProducer;
    private final int numberOfConsumers;
    private final int queueCapacity;
    
    private Configuration(int numberOfProducers, int itemsPerProducer, 
                         int numberOfConsumers, int queueCapacity) {
        this.numberOfProducers = numberOfProducers;
        this.itemsPerProducer = itemsPerProducer;
        this.numberOfConsumers = numberOfConsumers;
        this.queueCapacity = queueCapacity;
    }
    
    /**
     * Creates configuration from user input via System.in.
     * 
     * @return configuration with user-provided values
     */
    @SuppressWarnings("resource") // Scanner wraps System.in, should not be closed
    public static Configuration fromUserInput() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n=== SIMULATION CONFIGURATION ===\n");
        System.out.print("Enter Number of Producers: ");
        int producers = scanner.nextInt();
        System.out.print("Enter Items per Producer: ");
        int itemsPerProducer = scanner.nextInt();
        System.out.print("Enter Number of Consumers: ");
        int consumers = scanner.nextInt();
        System.out.print("Enter Queue Capacity: ");
        int capacity = scanner.nextInt();
        
        System.out.println("\nConfiguration Loaded.");
        return new Configuration(producers, itemsPerProducer, consumers, capacity);
    }
    
    /**
     * Creates configuration with default values.
     * Defaults: 4 producers, 20 items each, 2 consumers, capacity 10.
     * 
     * @return configuration with default values
     */
    public static Configuration withDefaults() {
        return new Configuration(4, 20, 2, 10);
    }
    
    /**
     * Creates configuration with custom values.
     * 
     * @param producers number of producer threads
     * @param itemsPerProducer items each producer generates
     * @param consumers number of consumer threads
     * @param capacity queue capacity
     * @return configuration with specified values
     */
    public static Configuration custom(int producers, int itemsPerProducer, 
                                      int consumers, int capacity) {
        return new Configuration(producers, itemsPerProducer, consumers, capacity);
    }
    
    public int getNumberOfProducers() {
        return numberOfProducers;
    }
    
    public int getItemsPerProducer() {
        return itemsPerProducer;
    }
    
    public int getNumberOfConsumers() {
        return numberOfConsumers;
    }
    
    public int getQueueCapacity() {
        return queueCapacity;
    }
    
    /**
     * Calculates total items to be produced.
     * 
     * @return producers * itemsPerProducer
     */
    public int getTotalExpectedItems() {
        return numberOfProducers * itemsPerProducer;
    }
    
    @Override
    public String toString() {
        return String.format(
            "Configuration{producers=%d, itemsPerProducer=%d, consumers=%d, capacity=%d, totalItems=%d}",
            numberOfProducers, itemsPerProducer, numberOfConsumers, queueCapacity, getTotalExpectedItems()
        );
    }
    
    /**
     * Validates all configuration values are positive.
     * 
     * @throws IllegalArgumentException if any value is not positive
     */
    public void validate() {
        if (numberOfProducers <= 0 || itemsPerProducer <= 0 || 
            numberOfConsumers <= 0 || queueCapacity <= 0) {
            throw new IllegalArgumentException("All configuration values must be positive");
        }
    }
}
