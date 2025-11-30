package com.pranjal.assign2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Telemetry Simulation Utility for Synthetic Dataset Generation.
 * <p>Generates a high-fidelity synthetic dataset with realistic variance and entropy.
 * This utility creates subscription telemetry data that simulates real-world
 * customer behavior patterns across multiple market segments.</p>
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Randomized customer profile distribution to prevent clustering</li>
 *   <li>Realistic revenue variance using statistical distributions</li>
 *   <li>Strategic scenario injection for testing analytics algorithms</li>
 *   <li>Multi-segment support: SMB, Consumer, and Accountant segments</li>
 * </ul>
 * </p>
 * 
 * <p>Data Quality:
 * <ul>
 *   <li>High entropy: Shuffled customer IDs prevent pattern detection</li>
 *   <li>Realistic variance: Price fluctuations simulate market dynamics</li>
 *   <li>Legendary scenarios: Pre-configured test cases for validation</li>
 * </ul>
 * </p>
 * 
 * @author Pranjal
 * @version 1.3
 */
public class DataSeeder {

    /**
     * Output file path for the generated CSV dataset.
     * File will be created in the resources directory for application access.
     */
    private static final String FILE_PATH = "src/main/resources/prosperity_ecosystem_data.csv";
    
    /**
     * Random number generator for stochastic data generation.
     * Used for profile type selection, variance calculation, and probability-based decisions.
     */
    private static final Random random = new Random();
    
    /**
     * Total number of unique customers to generate in the dataset.
     * Each customer may have multiple product subscriptions, resulting in more rows.
     */
    private static final int TOTAL_CUSTOMERS = 500;

    /**
     * Main entry point for dataset generation.
     * Orchestrates the complete data generation pipeline:
     * 1. Generate randomized customer profiles
     * 2. Inject strategic test scenarios
     * 3. Write to CSV file
     * 
     * @param args Command-line arguments (currently unused)
     */
    public static void main(String[] args) {
        System.out.println("Generating High-Fidelity Intuit Ecosystem Dataset...");
        
        // Phase 1: Initialize data structures
        // Generate all customer rows in memory before writing for efficiency
        List<String> rawRows = new ArrayList<>();
        
        // CSV header row matching the expected schema
        String header = "customer_id,segment,product,mrr,active_users,ai_enabled,live_expert,data_connections,is_trial,churn_signal,days_inactive";

        // Phase 2: Create and randomize customer ID pool
        // Shuffling prevents clustering and ensures realistic data distribution
        List<Integer> ids = new ArrayList<>();
        for (int i = 1000; i < 1000 + TOTAL_CUSTOMERS; i++) {
            ids.add(i);
        }
        Collections.shuffle(ids); // CRITICAL: Randomize order to prevent pattern detection

        // Phase 3: Generate customer profiles
        // Each customer may have multiple products, creating multiple rows per customer
        for (int id : ids) {
            rawRows.addAll(generateRandomProfile("C-" + id));
        }

        // Phase 4: Inject strategic test scenarios
        // These "legendary" scenarios are used for validating analytics algorithms
        injectLegendaryScenarios(rawRows);

        // Phase 5: Persist to CSV file
        // Use try-with-resources for automatic resource management
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(header + "\n");
            for (String row : rawRows) {
                writer.write(row + "\n");
            }
            System.out.println("SUCCESS: Generated " + rawRows.size() + " lines of high-entropy data.");
        } catch (IOException e) {
            System.err.println("ERROR: Failed to write dataset file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generates a random customer profile based on statistical distribution.
     * Creates subscription records for a single customer with realistic product combinations.
     * 
     * <p>Distribution Strategy:
     * <ul>
     *   <li>45% SMB: Core revenue segment with multi-product bundles</li>
     *   <li>40% Consumer: High volume, freemium model with upsell opportunities</li>
     *   <li>15% Accountant: Premium segment with high MRR and advanced features</li>
     * </ul>
     * </p>
     * 
     * @param custId Unique customer identifier (e.g., "C-1001")
     * @return List of CSV row strings representing this customer's subscriptions
     */
    private static List<String> generateRandomProfile(String custId) {
        List<String> rows = new ArrayList<>();
        // Use 0-99 range for finer granularity in probability distribution
        int profileType = random.nextInt(100);

        if (profileType < 45) { 
            // SMB Segment (45% probability) - The Core Revenue Driver
            // Base Product: QuickBooks Online with realistic price variance
            rows.add(createRow(custId, "SMB", "QB_ONLINE", 85.00 + variance(5), 3, 
                randBool(0.4), randBool(0.2), randInt(1, 8), false, false, randInt(0, 5)));
            
            // Upsell Opportunity 1: Payroll (50% attach rate)
            if (random.nextBoolean()) {
                rows.add(createRow(custId, "SMB", "QB_PAYROLL", 65.00 + variance(2), 3, 
                    randBool(0.4), false, randInt(1, 8), false, false, randInt(0, 5)));
            }
            // Upsell Opportunity 2: Mailchimp Integration (30% attach rate)
            if (random.nextDouble() < 0.3) {
                rows.add(createRow(custId, "SMB", "MAILCHIMP", 45.00 + variance(5), 1, 
                    false, false, 0, false, false, randInt(0, 5)));
            }

        } else if (profileType < 85) {
            // Consumer Segment (40% probability) - High Volume, Mixed Revenue Model
            // Freemium Strategy: 20% convert to paid TurboTax Live, 80% remain free
            boolean isPaid = random.nextDouble() < 0.2;
            double ttPrice = isPaid ? 120.00 : 0.00;
            String ttProduct = isPaid ? "TURBOTAX_LIVE" : "TURBOTAX";
            
            rows.add(createRow(custId, "CONSUMER", ttProduct, ttPrice, 1, false, isPaid, 0, 
                false, false, randInt(10, 300)));

            // Cross-sell: Credit Karma (40% adoption, 20% premium conversion)
            if (random.nextDouble() < 0.4) {
                boolean isCkPremium = random.nextDouble() < 0.2;
                double ckPrice = isCkPremium ? 10.00 : 0.00; // Premium tier pricing
                rows.add(createRow(custId, "CONSUMER", "CREDIT_KARMA", ckPrice, 1, true, false, 1, 
                    false, false, randInt(1, 7)));
            }

        } else {
            // Accountant Segment (15% probability) - Premium "Whale" Customers
            // High-value segment with advanced features and maximum product adoption
            rows.add(createRow(custId, "ACCOUNTANT", "QB_ONLINE_ADVANCED", 250.00 + variance(20), 
                15, true, true, 25, false, false, 0));
        }
        return rows;
    }

    /**
     * Injects strategic test scenarios into the dataset at random positions.
     * These "legendary" scenarios are designed to validate analytics algorithms
     * and ensure edge cases are properly handled.
     * 
     * <p>Injected Scenarios:
     * <ul>
     *   <li>Golden Bundle: Ideal customer with full product stack</li>
     *   <li>Churn Risk: High-value customer showing danger signals</li>
     *   <li>Next Best Action: Customers primed for specific upsells</li>
     * </ul>
     * </p>
     * 
     * @param rows The list of CSV rows to inject scenarios into
     */
    private static void injectLegendaryScenarios(List<String> rows) {
        // Insert at random positions to prevent pattern detection in analytics
        
        // Scenario 1: The "Golden Bundle" Customer
        // Ideal SMB customer with complete product stack and high engagement
        String goldId = "C-GOLD-1";
        rows.add(random.nextInt(rows.size()), createRow(goldId, "SMB", "QB_ONLINE", 85.00, 10, true, true, 12, false, false, 1));
        rows.add(random.nextInt(rows.size()), createRow(goldId, "SMB", "QB_PAYROLL", 65.00, 10, true, true, 12, false, false, 1));
        rows.add(random.nextInt(rows.size()), createRow(goldId, "SMB", "MAILCHIMP", 45.00, 10, true, true, 12, false, false, 1));

        // Scenario 2: The "Churn Risk" Customer
        // High-value customer with churn signal and extended inactivity
        rows.add(random.nextInt(rows.size()), createRow("C-RISK-99", "SMB", "QB_ONLINE", 250.00, 5, false, false, 0, false, true, 46));

        // Scenario 3: Next Best Action Targets
        // Target 1: SMB customer with high usage, needs Payroll upsell
        rows.add(random.nextInt(rows.size()), createRow("C-NBA-100", "SMB", "QB_ONLINE", 90.00, 8, false, false, 5, false, false, 2));
        
        // Target 2: Consumer with high tenure, needs Credit Karma cross-sell
        rows.add(random.nextInt(rows.size()), createRow("C-NBA-200", "CONSUMER", "TURBOTAX", 0.00, 1, false, false, 0, false, false, 5));
    }

    /**
     * Creates a formatted CSV row string from individual field values.
     * Ensures consistent formatting across all generated records.
     * 
     * @param id Customer identifier
     * @param seg Market segment (SMB, CONSUMER, ACCOUNTANT)
     * @param prod Product SKU
     * @param mrr Monthly Recurring Revenue
     * @param users Number of active users
     * @param ai AI features enabled flag
     * @param expert Live expert services flag
     * @param conn Number of data connections
     * @param trial Trial subscription flag
     * @param churn Churn signal flag
     * @param days Days since last activity
     * @return Formatted CSV row string
     */
    private static String createRow(String id, String seg, String prod, double mrr, int users, 
            boolean ai, boolean expert, int conn, boolean trial, boolean churn, int days) {
        return String.format("%s,%s,%s,%.2f,%d,%b,%b,%d,%b,%b,%d",
                id, seg, prod, mrr, users, ai, expert, conn, trial, churn, days);
    }

    /**
     * Generates random variance for price values to simulate realistic market fluctuations.
     * Returns a value in the range [-range, +range] for adding to base prices.
     * 
     * @param range Maximum variance amount (e.g., 5.00 for +/- $5.00)
     * @return Random variance value in the specified range
     */
    private static double variance(double range) {
        return (random.nextDouble() * range * 2) - range;
    }

    /**
     * Generates a random boolean value based on probability threshold.
     * 
     * @param probability Probability threshold (0.0 to 1.0)
     * @return true if random value is below threshold, false otherwise
     */
    private static boolean randBool(double probability) {
        return random.nextDouble() < probability;
    }
    
    /**
     * Generates a random integer within the specified inclusive range.
     * 
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return Random integer in [min, max]
     */
    private static int randInt(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }
}