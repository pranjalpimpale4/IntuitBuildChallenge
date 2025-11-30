package com.pranjal.assign2;

import com.pranjal.assign2.service.DataIngestionService;
import com.pranjal.assign2.service.ProsperityAnalyticsService;
import com.pranjal.assign2.model.SubscriptionRecord;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * Application Entry Point: ProsperityConnect Dashboard.
 * <p>Orchestrates the complete data pipeline: Ingestion -> Analysis -> Visualization.
 * Renders a "Headless Dashboard" to the console simulating an internal
 * Intuit executive report with real-time analytics and strategic insights.</p>
 * 
 * <p>Architecture Pattern: Layered Architecture with clear separation of concerns:
 * <ul>
 *   <li>Data Layer: CSV ingestion via DataIngestionService</li>
 *   <li>Business Layer: Analytics via ProsperityAnalyticsService</li>
 *   <li>Presentation Layer: Console-based dashboard rendering</li>
 * </ul>
 * </p>
 * 
 * @author Pranjal
 * @version 1.3
 */
public class App {
    
    /**
     * Default path to the prosperity ecosystem dataset CSV file.
     * This file contains subscription telemetry data for analysis.
     */
    private static final String DATA_SOURCE = "src/main/resources/prosperity_ecosystem_data.csv";

    /**
     * Main entry point for the ProsperityConnect application.
     * Executes the complete analytics pipeline and renders the dashboard.
     * 
     * @param args Command-line arguments (currently unused)
     */
    public static void main(String[] args) {
        // Phase 1: Data Ingestion Layer
        // Load and parse CSV data into domain objects using parallel stream processing
        DataIngestionService loader = new DataIngestionService();
        List<SubscriptionRecord> data = loader.loadSubscriptions(DATA_SOURCE);
        
        // Validation: Ensure data was successfully loaded
        if (data.isEmpty()) {
            System.err.println("System Halt: No data found. Please run DataSeeder first.");
            return;
        }

        // Phase 2: Analytics Engine Initialization
        // Initialize the analytics service with ingested data for processing
        ProsperityAnalyticsService engine = new ProsperityAnalyticsService(data);

        // Phase 3: Presentation Layer
        // Render the comprehensive dashboard with all analytics modules
        renderDashboard(engine);
    }

    /**
     * Renders the comprehensive ProsperityConnect dashboard to the console.
     * Displays six analytical modules with color-coded output for enhanced readability.
     * 
     * @param engine The analytics service containing processed subscription data
     */
    private static void renderDashboard(ProsperityAnalyticsService engine) {
        // Dashboard Header with ANSI color coding for professional presentation
        System.out.println(ConsoleColors.CYAN_BOLD + "================================================================================" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN_BOLD + "   PROSPERITY CONNECT  ::  INTUIT ECOSYSTEM INTELLIGENCE PLATFORM (v1.3)" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN_BOLD + "   Context: Live Data Simulation | Period: Q4 2025" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN_BOLD + "================================================================================" + ConsoleColors.RESET);

        // MODULE 1: Ecosystem Depth Analysis
        // Measures customer "stickiness" by analyzing product adoption patterns
        System.out.println("\n[1] ECOSYSTEM DEPTH & STICKINESS (The \"Network Effect\")");
        Map<Integer, Long> dist = engine.getProductCountDistribution();
        
        // Calculate scaling factor for proportional bar chart visualization
        long maxCount = dist.values().stream().mapToLong(v -> v).max().orElse(1);
        final int maxBarLength = 30; // Maximum visual bar length in characters

        // Render proportional bar chart for each product count tier
        dist.forEach((count, users) -> {
            // Proportional scaling: (current_value / max_value) * max_bar_length
            int barLength = (int) ((double) users / maxCount * maxBarLength);
            // Ensure minimum visibility: at least 1 block for non-zero values
            barLength = Math.max(1, barLength);
            
            // Generate visual bar using Unicode block character
            String bar = "█".repeat(barLength);
            System.out.printf("    %d Product(s) : %-30s (%d Customers)%n", count, bar, users);
        });
        
        // MODULE 2: Revenue Matrix (Pivot Table)
        // Cross-tabulation of revenue by product and segment for strategic analysis
        System.out.println("\n[2] REVENUE MATRIX (Product x Segment Performance)");
        System.out.println("    ----------------------------------------------------------------------------");
        System.out.printf("    %-20s | %-15s | %-15s%n", "PRODUCT", "SMB MRR", "CONSUMER MRR");
        System.out.println("    ----------------------------------------------------------------------------");
        
        Map<String, Map<String, BigDecimal>> matrix = engine.getRevenueMatrix();
        // Iterate through products and display segment-specific revenue breakdown
        matrix.forEach((product, segmentMap) -> {
            BigDecimal smb = segmentMap.getOrDefault("SMB", BigDecimal.ZERO);
            BigDecimal cons = segmentMap.getOrDefault("CONSUMER", BigDecimal.ZERO);
            // Filter: Only display products with non-zero revenue
            if (smb.add(cons).doubleValue() > 0) {
                 System.out.printf("    %-20s | $ %-13s | $ %-13s%n", product, smb, cons);
            }
        });

        // MODULE 3: Golden Bundles Analysis
        // Identifies highest-value product combinations for upsell opportunities
        System.out.println("\n[3] GOLDEN BUNDLES (Highest Revenue Combinations)");
        engine.getGoldenBundles().forEach(bundle -> System.out.println("    " + bundle));

        // MODULE 4: Strategic Risk Alerts (Color: RED for urgency)
        // Proactive churn detection for high-value customers showing risk signals
        System.out.println("\n[4] STRATEGIC HEALTH ALERTS (Churn Risk Detection)");
        engine.getAtRiskCustomers().forEach(alert -> 
            System.out.println("    " + ConsoleColors.RED_BOLD + alert + ConsoleColors.RESET));

        // MODULE 5: Next Best Action Engine (Color: GREEN for opportunities)
        // AI-driven recommendation system for cross-sell and upsell opportunities
        System.out.println("\n[5] \"NEXT BEST ACTION\" ENGINE (AI Recommendations)");
        System.out.println("    CUSTOMER   | SEGMENT | CURRENT STACK   | RECOMMENDATION      | LOGIC");
        System.out.println("    ----------------------------------------------------------------------------");
        engine.getNextBestActions().forEach(action -> 
            System.out.println("    " + ConsoleColors.GREEN_BOLD + action + ConsoleColors.RESET));

        // MODULE 6: Segment Unit Economics (Color: YELLOW for metrics)
        // Statistical summary using DoubleSummaryStatistics for comprehensive analysis
        System.out.println("\n[6] SEGMENT UNIT ECONOMICS (Statistical Breakdown)");
        System.out.println("    SEGMENT    |  AVG MRR  |  MAX MRR  |  VOLUME");
        System.out.println("    ------------------------------------------------");
        engine.getSegmentStatistics().forEach((segment, stats) -> {
            // Display key metrics: Average MRR, Maximum MRR, and Customer Volume
            System.out.printf("    " + ConsoleColors.YELLOW + "%-10s | $%7.2f  | $%7.2f  | %5d" + ConsoleColors.RESET + "%n",
                segment, stats.getAverage(), stats.getMax(), stats.getCount());
        });

        // Dashboard Footer
        System.out.println("\n================================================================================");
        System.out.println("   SYSTEM STATUS: Analysis Complete.");
        System.out.println("================================================================================");
    }
}