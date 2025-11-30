package com.pranjal.assign2.model;

import java.math.BigDecimal;

/**
 * Immutable Domain Object representing a single subscription telemetry point.
 * <p>Implemented as a Java Record (JDK 16+) to ensure data immutability
 * and thread safety during parallel Stream processing. This model serves
 * as the Single Source of Truth for the analytics pipeline.</p>
 * 
 * <p>Design Benefits:
 * <ul>
 *   <li>Immutability: Records are inherently immutable, preventing data corruption</li>
 *   <li>Thread Safety: Safe for concurrent access in parallel streams</li>
 *   <li>Value Semantics: Automatic equals(), hashCode(), and toString()</li>
 *   <li>Type Safety: Strongly-typed fields prevent runtime errors</li>
 * </ul>
 * </p>
 * 
 * <p>Financial Precision:
 * <ul>
 *   <li>Uses BigDecimal for MRR to avoid floating-point precision issues</li>
 *   <li>Critical for accurate financial calculations and reporting</li>
 * </ul>
 * </p>
 * 
 * @param customerId       Unique identifier string for the customer entity
 * @param segment          Market segment classification (SMB, CONSUMER, ACCOUNTANT)
 * @param product          Specific Intuit product SKU (e.g., QB_ONLINE, TURBOTAX, MAILCHIMP)
 * @param mrr              Monthly Recurring Revenue (BigDecimal for financial precision)
 * @param activeUsers      Number of provisioned seats/licenses for this subscription
 * @param aiEnabled        Flag indicating adoption of "Intuit Assist" AI features
 * @param liveExpert       Flag indicating utilization of Live Bookkeeper/CPA services
 * @param dataConnections  Count of active 3rd party integrations (Bank feeds, CRM, etc.)
 * @param isTrial          True if subscription is in $0 trial period, false if paid
 * @param churnSignal      True if cancellation event occurred in current billing cycle
 * @param daysInactive     Days elapsed since last authenticated user session
 * 
 * @author Pranjal
 * @version 1.3
 */
public record SubscriptionRecord(
    String customerId,
    String segment,
    String product,
    BigDecimal mrr,
    int activeUsers,
    boolean aiEnabled,
    boolean liveExpert,
    int dataConnections,
    boolean isTrial,
    boolean churnSignal,
    int daysInactive
) {
    /**
     * Determines if this subscription represents a high-value account.
     * <p>High-value threshold is set at $50.00 MRR, which represents
     * strategic accounts requiring special attention in risk analysis
     * and retention efforts.</p>
     * 
     * <p>Usage:
     * <ul>
     *   <li>Risk analysis filtering</li>
     *   <li>Retention prioritization</li>
     *   <li>Revenue impact calculations</li>
     * </ul>
     * </p>
     * 
     * @return true if MRR exceeds the strategic threshold ($50.00), false otherwise
     */
    public boolean isHighValue() {
        return mrr.compareTo(new BigDecimal("50.00")) > 0;
    }
}