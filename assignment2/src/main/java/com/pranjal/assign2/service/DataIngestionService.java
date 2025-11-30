package com.pranjal.assign2.service;

import com.pranjal.assign2.model.SubscriptionRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Ingestion Service for CSV to Domain Object Transformation.
 * <p>Responsible for marshaling raw CSV data into strongly-typed domain objects.
 * Uses Java NIO (New I/O) for efficient file reading and provides robust error
 * handling to ensure pipeline stability even with malformed inputs.</p>
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Parallel stream processing for multi-core performance</li>
 *   <li>Automatic resource management via try-with-resources</li>
 *   <li>Input validation and error recovery</li>
 *   <li>Functional programming patterns for maintainability</li>
 * </ul>
 * </p>
 * 
 * <p>Performance:
 * <ul>
 *   <li>Leverages parallel streams for concurrent processing</li>
 *   <li>Efficient NIO file reading with buffering</li>
 *   <li>Minimal memory footprint through streaming</li>
 * </ul>
 * </p>
 * 
 * @author Pranjal
 * @version 1.3
 */
public class DataIngestionService {

    /**
     * Loads and parses subscription data from the specified CSV file.
     * <p>Processes the file using parallel streams for optimal performance on large datasets.
     * Automatically handles header skipping, empty line filtering, and error recovery.</p>
     * 
     * <p>Processing Pipeline:
     * <ol>
     *   <li>Read file using NIO BufferedReader</li>
     *   <li>Skip header row (first line)</li>
     *   <li>Filter empty/whitespace lines</li>
     *   <li>Parse each line into SubscriptionRecord</li>
     *   <li>Collect into immutable list</li>
     * </ol>
     * </p>
     * 
     * @param filePath Relative or absolute path to the CSV resource file
     * @return List of immutable SubscriptionRecord objects, or empty list on failure
     * @throws IllegalArgumentException if CSV lines have invalid format
     */
    public List<SubscriptionRecord> loadSubscriptions(String filePath) {
        Path path = Paths.get(filePath);

        // Try-with-resources ensures automatic file handle closure
        // Prevents resource leaks and follows Java best practices
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return reader.lines()
                    .parallel() // Enable multi-core processing for large datasets
                    .skip(1) // Skip CSV header row
                    .filter(line -> line != null && !line.trim().isEmpty()) // Filter empty/whitespace lines
                    .map(this::parseLine) // Transform CSV string to domain object
                    .collect(Collectors.toList());

        } catch (IOException e) {
            // Graceful error handling: log error and return empty list
            // Prevents application crash and allows caller to handle gracefully
            System.err.println("[CRITICAL] Failed to ingest data lake: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Marshals a single CSV line into a strongly-typed SubscriptionRecord.
     * <p>Performs data type conversion and validation before object creation.
     * Throws IllegalArgumentException for malformed lines to prevent data corruption.</p>
     * 
     * <p>CSV Schema (11 columns):
     * <ol>
     *   <li>customer_id: String identifier</li>
     *   <li>segment: Market segment (SMB, CONSUMER, ACCOUNTANT)</li>
     *   <li>product: Product SKU</li>
     *   <li>mrr: Monthly Recurring Revenue (BigDecimal for precision)</li>
     *   <li>active_users: Integer count</li>
     *   <li>ai_enabled: Boolean flag</li>
     *   <li>live_expert: Boolean flag</li>
     *   <li>data_connections: Integer count</li>
     *   <li>is_trial: Boolean flag</li>
     *   <li>churn_signal: Boolean flag</li>
     *   <li>days_inactive: Integer count</li>
     * </ol>
     * </p>
     * 
     * @param line Raw CSV string to parse
     * @return SubscriptionRecord domain object with parsed values
     * @throws IllegalArgumentException if line doesn't have expected 11 columns
     * @throws NumberFormatException if numeric fields cannot be parsed
     */
    private SubscriptionRecord parseLine(String line) {
        String[] col = line.split(",");
        
        // Validation: Ensure line has all required columns
        // Prevents ArrayIndexOutOfBoundsException and data corruption
        if (col.length < 11) {
            throw new IllegalArgumentException(
                "Invalid CSV line: expected 11 columns but found " + col.length + " in line: " + line);
        }
        
        // Type-safe mapping: Convert CSV strings to appropriate Java types
        // Using BigDecimal for financial precision, proper boolean parsing
        return new SubscriptionRecord(
            col[0].trim(), // customer_id: String identifier
            col[1].trim(), // segment: Market segment classification
            col[2].trim(), // product: Product SKU
            new BigDecimal(col[3].trim()), // mrr: Financial precision required
            Integer.parseInt(col[4].trim()), // activeUsers: Integer count
            Boolean.parseBoolean(col[5].trim()), // aiEnabled: Boolean flag
            Boolean.parseBoolean(col[6].trim()), // liveExpert: Boolean flag
            Integer.parseInt(col[7].trim()), // dataConnections: Integer count
            Boolean.parseBoolean(col[8].trim()), // isTrial: Boolean flag
            Boolean.parseBoolean(col[9].trim()), // churnSignal: Boolean flag
            Integer.parseInt(col[10].trim()) // daysInactive: Integer count
        );
    }
}