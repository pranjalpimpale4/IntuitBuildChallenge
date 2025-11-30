package com.pranjal.assign2.service;

import com.pranjal.assign2.model.SubscriptionRecord;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core Analytics Engine utilizing Functional Programming Paradigms.
 * <p>This service implements the "Prosperity Intelligence" analytics logic using 
 * Java Stream API pipelines. It performs complex aggregations, grouping, 
 * and predictive filtering without maintaining mutable state, ensuring
 * thread-safety and functional purity.</p>
 * 
 * <p>Architecture Principles:
 * <ul>
 *   <li>Immutable data structures for thread-safety</li>
 *   <li>Functional transformations via Stream API</li>
 *   <li>Stateless operations for scalability</li>
 *   <li>Declarative programming style</li>
 * </ul>
 * </p>
 * 
 * <p>Analytics Modules:
 * <ul>
 *   <li>Module A: Ecosystem Depth & Product Stickiness</li>
 *   <li>Module B: Revenue Matrix (Product x Segment)</li>
 *   <li>Module C: Golden Bundle Identification</li>
 *   <li>Module D: Strategic Risk Alerts</li>
 *   <li>Module E: Next Best Action Engine</li>
 *   <li>Module F: Segment Unit Economics</li>
 * </ul>
 * </p>
 * 
 * @author Pranjal
 * @version 1.3
 */
public class ProsperityAnalyticsService {

    /**
     * Immutable dataset of subscription records for analysis.
     * Final field ensures reference immutability and thread-safety.
     */
    private final List<SubscriptionRecord> data;

    /**
     * Initializes the analytics engine with the ingested dataset.
     * <p>The provided data list should be immutable or treated as read-only
     * to maintain functional programming principles.</p>
     * 
     * @param data Immutable list of subscription records to analyze
     */
    public ProsperityAnalyticsService(List<SubscriptionRecord> data) {
        this.data = data;
    }

    /**
     * MODULE A: Ecosystem Depth Analysis.
     * <p>Calculates the distribution of products per customer to measure "Stickiness".
     * Higher product counts indicate stronger customer engagement and reduced churn risk.
     * This metric is critical for understanding the "Network Effect" in the ecosystem.</p>
     * 
     * <p>Algorithm:
     * <ol>
     *   <li>Group records by customer ID and collect unique products per customer</li>
     *   <li>Count how many customers have 1 product, 2 products, 3+ products</li>
     *   <li>Return distribution map for visualization</li>
     * </ol>
     * </p>
     * 
     * <p>Time Complexity: O(n) where n is the number of subscription records</p>
     * 
     * @return Map of (Product Count -> Number of Customers) for distribution analysis
     */
    public Map<Integer, Long> getProductCountDistribution() {
        // Step 1: Group by customer and collect unique products using Set
        // Set automatically handles duplicate product deduplication per customer
        Map<String, Set<String>> productsPerCustomer = data.stream()
            .collect(Collectors.groupingBy(
                SubscriptionRecord::customerId,
                Collectors.mapping(SubscriptionRecord::product, Collectors.toSet())
            ));

        // Step 2: Transform to distribution: Count customers by product bundle size
        // This creates the final distribution map (1 product -> X customers, etc.)
        return productsPerCustomer.values().stream()
            .collect(Collectors.groupingBy(Set::size, Collectors.counting()));
    }

    /**
     * MODULE B: Revenue Matrix (Pivot Table).
     * <p>Generates a nested pivot table of Revenue by Product within each Segment.
     * This two-dimensional analysis enables strategic decision-making by identifying
     * which products drive revenue in which market segments.</p>
     * 
     * <p>Structure:
     * <ul>
     *   <li>Outer Map Key: Product SKU (e.g., "QB_ONLINE")</li>
     *   <li>Inner Map Key: Market Segment (e.g., "SMB", "CONSUMER")</li>
     *   <li>Value: Aggregated MRR for that product-segment combination</li>
     * </ul>
     * </p>
     * 
     * <p>Use Cases:
     * <ul>
     *   <li>Product performance analysis by segment</li>
     *   <li>Revenue optimization opportunities</li>
     *   <li>Market penetration insights</li>
     * </ul>
     * </p>
     * 
     * @return Nested Map structure: (Product -> (Segment -> Total MRR))
     */
    public Map<String, Map<String, BigDecimal>> getRevenueMatrix() {
        // Two-level grouping: First by product, then by segment
        // Uses BigDecimal reduction for precise financial aggregation
        return data.stream()
            .collect(Collectors.groupingBy(
                SubscriptionRecord::product,
                Collectors.groupingBy(
                    SubscriptionRecord::segment,
                    Collectors.reducing(BigDecimal.ZERO, SubscriptionRecord::mrr, BigDecimal::add)
                )
            ));
    }

    /**
     * MODULE C: Golden Bundle Finder.
     * <p>Identifies the highest revenue-generating product combinations across all customers.
     * This analysis helps identify upsell opportunities and optimal product bundles
     * for marketing campaigns.</p>
     * 
     * <p>Algorithm:
     * <ol>
     *   <li>Group products by customer to identify bundles</li>
     *   <li>Normalize bundle keys by sorting (A+B == B+A)</li>
     *   <li>Aggregate revenue per unique bundle across all customers</li>
     *   <li>Sort by total revenue descending and return top 3</li>
     * </ol>
     * </p>
     * 
     * <p>Key Features:
     * <ul>
     *   <li>Bundle normalization prevents duplicate counting</li>
     *   <li>Revenue aggregation across multiple customers with same bundle</li>
     *   <li>Top-N selection for actionable insights</li>
     * </ul>
     * </p>
     * 
     * @return List of formatted strings describing top 3 revenue-generating bundles
     */
    public List<String> getGoldenBundles() {
        // Step 1: Group by customer to identify their product stack
        // Using List to preserve product order initially (will be sorted later)
        Map<String, List<String>> customerBundles = data.stream()
            .collect(Collectors.groupingBy(
                SubscriptionRecord::customerId,
                Collectors.mapping(SubscriptionRecord::product, Collectors.toList())
            ));

        // Step 2: Aggregate revenue per unique bundle key
        // Multiple customers with same bundle will have revenues merged
        Map<String, BigDecimal> bundleRevenue = new HashMap<>();
        
        customerBundles.forEach((custId, products) -> {
            // Only process multi-product bundles (single products excluded)
            if (products.size() > 1) {
                // Normalize bundle key by sorting: ensures A+B == B+A
                Collections.sort(products);
                String key = String.join(" + ", products);
                
                // Calculate total MRR for this customer across all their products
                BigDecimal totalMrr = data.stream()
                    .filter(r -> r.customerId().equals(custId))
                    .map(SubscriptionRecord::mrr)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Merge: If bundle exists, add to existing revenue; otherwise create new entry
                bundleRevenue.merge(key, totalMrr, BigDecimal::add);
            }
        });

        // Step 3: Sort by revenue descending and select top 3 bundles
        return bundleRevenue.entrySet().stream()
            .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
            .limit(3)
            .map(e -> String.format("%-30s :: $ %.2f MRR", e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * MODULE D: Strategic Risk Alerts (Churn Detection).
     * <p>Detects high-value customers showing churn signals through inactivity or
     * explicit cancellation indicators. This proactive alerting enables retention
     * teams to intervene before revenue loss occurs.</p>
     * 
     * <p>Risk Criteria:
     * <ul>
     *   <li>High Value: MRR > $50.00 (focus on revenue impact)</li>
     *   <li>Churn Signal: Explicit cancellation flag OR inactivity > 45 days</li>
     * </ul>
     * </p>
     * 
     * <p>Output:
     * <ul>
     *   <li>Formatted alert messages with customer context</li>
     *   <li>Limited to top 5 alerts for dashboard clarity</li>
     *   <li>Distinct customers only (no duplicates)</li>
     * </ul>
     * </p>
     * 
     * @return List of formatted alert messages for dashboard display
     */
    public List<String> getAtRiskCustomers() {
        return data.stream()
            .filter(r -> r.mrr().doubleValue() > 50.00) // Focus on high-value customers
            .filter(r -> r.churnSignal() || r.daysInactive() > 45) // Risk detection logic
            .map(r -> String.format("(!) ALERT: %s [%s] - Risk: %d days inactive, MRR: $%.2f",
                    r.customerId(), r.segment(), r.daysInactive(), r.mrr()))
            .distinct() // Remove duplicate alerts for same customer
            .limit(5) // Top 5 alerts for dashboard visibility
            .collect(Collectors.toList());
    }

    /**
     * MODULE E: Next Best Action (NBA) Engine.
     * <p>Simulates AI-driven recommendations based on customer usage patterns and
     * behavioral signals. This prescriptive analytics module identifies specific
     * upsell and cross-sell opportunities for each customer.</p>
     * 
     * <p>Processing:
     * <ol>
     *   <li>Apply rule engine to each subscription record</li>
     *   <li>Filter out records with no actionable recommendations</li>
     *   <li>Return distinct recommendations limited to top 5</li>
     * </ol>
     * </p>
     * 
     * @return List of formatted recommendation strings for dashboard display
     */
    public List<String> getNextBestActions() {
        return data.stream()
            .map(this::applyAiRules) // Apply rule-based recommendation engine
            .filter(Objects::nonNull) // Filter out customers needing no action
            .distinct() // Remove duplicate recommendations
            .limit(5) // Top 5 recommendations for actionable focus
            .collect(Collectors.toList());
    }

    /**
     * Rule Engine for Next Best Action Module.
     * <p>Applies business rules to identify specific upsell/cross-sell opportunities.
     * Rules are evaluated in priority order, returning the first matching recommendation.</p>
     * 
     * <p>Business Rules:
     * <ol>
     *   <li>SMB High Usage: SMB customers with QB_ONLINE and >5 users → Payroll upsell</li>
     *   <li>Consumer Loyalty: CONSUMER with TURBOTAX without AI → Credit Karma cross-sell</li>
     *   <li>Data Volume: Any customer with >5 data connections without AI → Enable AI</li>
     * </ol>
     * </p>
     * 
     * @param r The subscription record to analyze for recommendations
     * @return Formatted recommendation string, or null if no action needed
     */
    private String applyAiRules(SubscriptionRecord r) {
        // Rule 1: SMB High Usage Pattern → Payroll Upsell Opportunity
        // Logic: High user count indicates growing business, likely needs payroll
        if (r.segment().equals("SMB") && r.product().equals("QB_ONLINE") && r.activeUsers() > 5) {
            return String.format("%-10s | SMB | QB_ONLINE     | + ADD PAYROLL       | Users > 5", r.customerId());
        }
        
        // Rule 2: Consumer Loyalty Pattern → Credit Karma Cross-sell
        // Logic: Long-term TurboTax users without AI are prime for financial services
        if (r.segment().equals("CONSUMER") && r.product().equals("TURBOTAX") && !r.aiEnabled()) {
             return String.format("%-10s | CONS| TURBOTAX      | + CREDIT KARMA      | Loyal User", r.customerId());
        }
        
        // Rule 3: High Data Volume Pattern → AI Enablement
        // Logic: Multiple integrations indicate complexity, AI can help automate
        if (r.dataConnections() > 5 && !r.aiEnabled()) {
             return String.format("%-10s | ALL | %-12s| + ENABLE AI ASSIST| High Data Vol", r.customerId(), r.product());
        }
        
        // No matching rule: return null to indicate no action needed
        return null;
    }

    /**
     * MODULE F: Deep Statistical Analysis (Unit Economics).
     * <p>Uses DoubleSummaryStatistics to calculate comprehensive statistical metrics
     * for each market segment in a single O(n) pass. This efficient approach
     * provides count, sum, min, max, and average MRR without multiple iterations.</p>
     * 
     * <p>Statistical Metrics Provided:
     * <ul>
     *   <li>Count: Number of subscription records per segment</li>
     *   <li>Sum: Total MRR aggregated per segment</li>
     *   <li>Average: Mean MRR (ARPU - Average Revenue Per User)</li>
     *   <li>Min: Minimum MRR value in segment</li>
     *   <li>Max: Maximum MRR value in segment (identifies "whales")</li>
     * </ul>
     * </p>
     * 
     * <p>Use Cases:
     * <ul>
     *   <li>Segment profitability analysis</li>
     *   <li>ARPU calculation for financial reporting</li>
     *   <li>Identification of high-value customers (max MRR)</li>
     *   <li>Volume analysis (count)</li>
     * </ul>
     * </p>
     * 
     * <p>Performance: O(n) time complexity - single pass through data</p>
     * 
     * @return Map of (Segment -> DoubleSummaryStatistics) containing all metrics
     */
    public Map<String, DoubleSummaryStatistics> getSegmentStatistics() {
        // Group by segment and collect comprehensive statistics in one pass
        // DoubleSummaryStatistics automatically calculates all metrics efficiently
        return data.stream()
            .collect(Collectors.groupingBy(
                SubscriptionRecord::segment,
                Collectors.summarizingDouble(r -> r.mrr().doubleValue())
            ));
    }
}