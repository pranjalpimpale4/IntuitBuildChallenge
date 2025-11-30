package com.pranjal.assign1;

/**
 * Main entry point for the Producer-Consumer Simulation System.
 * 
 * Supports three execution modes:
 * - Interactive: java Main (prompts for input)
 * - Default: java Main --default (uses defaults)
 * - Custom: java Main 4 20 2 10 (four integers)
 * 
 * @author Pranjal
 * @version 2.1
 */
public class Main {

    /**
     * Main method - initializes logger, runs simulation, ensures cleanup.
     * 
     * @param args command-line arguments for configuration
     */
    public static void main(String[] args) {
        Logger.init();
        
        try {
            Configuration config = getConfiguration(args);
            SimulationEngine engine = new SimulationEngine(config);
            engine.runSimulation();
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            Logger.log("SYSTEM", "Fatal error: " + e.getMessage());
        } finally {
            Logger.close();
            System.out.println("\n=== System Exited ===");
        }
    }
    
    /**
     * Determines configuration based on command-line arguments.
     * 
     * @param args command-line arguments
     * @return configuration instance
     * @throws IllegalArgumentException if arguments invalid
     */
    private static Configuration getConfiguration(String[] args) {
        if (args.length == 0) {
            // Interactive mode
            return Configuration.fromUserInput();
            
        } else if (args.length == 1 && args[0].equals("--default")) {
            // Default mode
            System.out.println("\nUsing default configuration...");
            Configuration config = Configuration.withDefaults();
            System.out.println(config);
            return config;
            
        } else if (args.length == 4) {
            // Custom mode
            try {
                int producers = Integer.parseInt(args[0]);
                int itemsPerProducer = Integer.parseInt(args[1]);
                int consumers = Integer.parseInt(args[2]);
                int capacity = Integer.parseInt(args[3]);
                
                Configuration config = Configuration.custom(producers, itemsPerProducer, 
                                                           consumers, capacity);
                System.out.println("\nUsing command-line configuration:");
                System.out.println(config);
                return config;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid numeric arguments");
            }
        } else {
            printUsage();
            throw new IllegalArgumentException("Invalid arguments");
        }
    }
    
    /**
     * Prints usage information.
     */
    private static void printUsage() {
        System.out.println("\n=== USAGE ===");
        System.out.println("Interactive mode:  java Main");
        System.out.println("Default config:    java Main --default");
        System.out.println("Custom config:     java Main <producers> <itemsPerProducer> <consumers> <capacity>");
        System.out.println("\nExample: java Main 4 20 2 10");
    }
}
