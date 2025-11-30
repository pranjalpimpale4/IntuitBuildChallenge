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
        int producers = readIntegerInput(scanner, "Enter Number of Producers: ");
        int itemsPerProducer = readIntegerInput(scanner, "Enter Items per Producer: ");
        int consumers = readIntegerInput(scanner, "Enter Number of Consumers: ");
        int capacity = readIntegerInput(scanner, "Enter Queue Capacity: ");
        
        System.out.println("\nConfiguration Loaded.");
        return new Configuration(producers, itemsPerProducer, consumers, capacity);
    }
    
    /**
     * Reads and validates integer input from the user.
     * Rejects non-integer input (alphabets, special characters), numbers too large for int,
     * and zero or negative numbers.
     * Prompts the user again until valid input is provided.
     * 
     * @param scanner Scanner instance for reading input
     * @param prompt Message to display to the user
     * @return valid positive integer value
     */
    private static int readIntegerInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                
                // Check if input is empty
                if (input.isEmpty()) {
                    System.out.println("ERROR: Input cannot be empty. Please enter a valid integer.");
                    continue;
                }
                
                // Try to parse as integer
                try {
                    int value = Integer.parseInt(input);
                    
                    // Check if value is positive (greater than 0)
                    if (value <= 0) {
                        System.out.println("ERROR: Input must be greater than 0. Please enter a positive integer.");
                        continue;
                    }
                    
                    return value;
                } catch (NumberFormatException e) {
                    // Check if it contains alphabets or special characters
                    if (!input.matches("^-?\\d+$")) {
                        // Contains non-digit characters
                        if (input.matches(".*[a-zA-Z].*")) {
                            System.out.println("ERROR: Input contains alphabets. Please enter a valid integer.");
                        } else {
                            System.out.println("ERROR: Input contains special characters. Please enter a valid integer.");
                        }
                    } else {
                        // Number is too large for int
                        System.out.println("ERROR: Number is too large. Please enter a number between " + 
                                         Integer.MIN_VALUE + " and " + Integer.MAX_VALUE + ".");
                    }
                    continue;
                }
            } catch (Exception e) {
                System.out.println("ERROR: Invalid input. Please enter a valid integer.");
                // Clear the scanner buffer in case of any issues
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }
            }
        }
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
