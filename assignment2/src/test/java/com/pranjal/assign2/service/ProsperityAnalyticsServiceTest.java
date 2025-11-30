package com.pranjal.assign2.service;

import com.pranjal.assign2.model.SubscriptionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ProsperityAnalyticsService.
 * Tests functional programming patterns, stream operations, data aggregation, and lambda expressions.
 */
@DisplayName("ProsperityAnalyticsService Test Suite")
public class ProsperityAnalyticsServiceTest {

    private ProsperityAnalyticsService service;
    private List<SubscriptionRecord> testData;

    @BeforeEach
    void setUp() {
        testData = new ArrayList<>();
        service = new ProsperityAnalyticsService(testData);
    }

    // ==================== MODULE A: Product Count Distribution ====================

    @Test
    @DisplayName("getProductCountDistribution - Single product per customer")
    void testProductCountDistribution_SingleProduct() {
        // Arrange: Create customers with single products
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 5, false, false, 0));
        testData.add(new SubscriptionRecord("C-2", "SMB", "QB_PAYROLL", new BigDecimal("65.00"), 2, false, false, 3, false, false, 0));
        testData.add(new SubscriptionRecord("C-3", "CONSUMER", "TURBOTAX", new BigDecimal("0.00"), 1, false, false, 0, false, false, 10));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        Map<Integer, Long> distribution = service.getProductCountDistribution();

        // Assert
        assertNotNull(distribution);
        assertEquals(1, distribution.size());
        assertEquals(3L, distribution.get(1));
    }

    @Test
    @DisplayName("getProductCountDistribution - Multiple products per customer")
    void testProductCountDistribution_MultipleProducts() {
        // Arrange: Customer with multiple products
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 5, false, false, 0));
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_PAYROLL", new BigDecimal("65.00"), 3, false, false, 5, false, false, 0));
        testData.add(new SubscriptionRecord("C-1", "SMB", "MAILCHIMP", new BigDecimal("45.00"), 3, false, false, 5, false, false, 0));
        testData.add(new SubscriptionRecord("C-2", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 2, false, false, 3, false, false, 0));
        testData.add(new SubscriptionRecord("C-2", "SMB", "QB_PAYROLL", new BigDecimal("65.00"), 2, false, false, 3, false, false, 0));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        Map<Integer, Long> distribution = service.getProductCountDistribution();

        // Assert
        assertNotNull(distribution);
        assertEquals(2, distribution.size());
        assertEquals(1L, distribution.get(3)); // C-1 has 3 products
        assertEquals(1L, distribution.get(2)); // C-2 has 2 products
    }

    @Test
    @DisplayName("getProductCountDistribution - Empty dataset")
    void testProductCountDistribution_EmptyData() {
        // Act
        Map<Integer, Long> distribution = service.getProductCountDistribution();

        // Assert
        assertNotNull(distribution);
        assertTrue(distribution.isEmpty());
    }

    @Test
    @DisplayName("getProductCountDistribution - Duplicate products for same customer")
    void testProductCountDistribution_DuplicateProducts() {
        // Arrange: Same customer, same product (should be deduplicated by Set)
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 5, false, false, 0));
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 5, false, false, 0));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        Map<Integer, Long> distribution = service.getProductCountDistribution();

        // Assert: Should count as 1 product (Set deduplication)
        assertEquals(1L, distribution.get(1));
    }

    // ==================== MODULE B: Revenue Matrix ====================

    @Test
    @DisplayName("getRevenueMatrix - Single product, single segment")
    void testRevenueMatrix_SingleProductSegment() {
        // Arrange
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 5, false, false, 0));
        testData.add(new SubscriptionRecord("C-2", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 2, false, false, 3, false, false, 0));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        Map<String, Map<String, BigDecimal>> matrix = service.getRevenueMatrix();

        // Assert
        assertNotNull(matrix);
        assertTrue(matrix.containsKey("QB_ONLINE"));
        assertEquals(new BigDecimal("170.00"), matrix.get("QB_ONLINE").get("SMB"));
    }

    @Test
    @DisplayName("getRevenueMatrix - Multiple products and segments")
    void testRevenueMatrix_MultipleProductsSegments() {
        // Arrange
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 5, false, false, 0));
        testData.add(new SubscriptionRecord("C-2", "CONSUMER", "TURBOTAX", new BigDecimal("0.00"), 1, false, false, 0, false, false, 10));
        testData.add(new SubscriptionRecord("C-3", "SMB", "QB_PAYROLL", new BigDecimal("65.00"), 2, false, false, 3, false, false, 0));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        Map<String, Map<String, BigDecimal>> matrix = service.getRevenueMatrix();

        // Assert
        assertNotNull(matrix);
        assertEquals(3, matrix.size());
        assertTrue(matrix.containsKey("QB_ONLINE"));
        assertTrue(matrix.containsKey("TURBOTAX"));
        assertTrue(matrix.containsKey("QB_PAYROLL"));
        assertEquals(new BigDecimal("85.00"), matrix.get("QB_ONLINE").get("SMB"));
        assertEquals(new BigDecimal("0.00"), matrix.get("TURBOTAX").get("CONSUMER"));
    }

    @Test
    @DisplayName("getRevenueMatrix - Aggregation of multiple records")
    void testRevenueMatrix_Aggregation() {
        // Arrange: Multiple records for same product-segment combination
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 5, false, false, 0));
        testData.add(new SubscriptionRecord("C-2", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 2, false, false, 3, false, false, 0));
        testData.add(new SubscriptionRecord("C-3", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 4, false, false, 6, false, false, 0));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        Map<String, Map<String, BigDecimal>> matrix = service.getRevenueMatrix();

        // Assert: Should aggregate to 255.00
        assertEquals(new BigDecimal("255.00"), matrix.get("QB_ONLINE").get("SMB"));
    }

    @Test
    @DisplayName("getRevenueMatrix - Empty dataset")
    void testRevenueMatrix_EmptyData() {
        // Act
        Map<String, Map<String, BigDecimal>> matrix = service.getRevenueMatrix();

        // Assert
        assertNotNull(matrix);
        assertTrue(matrix.isEmpty());
    }

    // ==================== MODULE C: Golden Bundles ====================

    @Test
    @DisplayName("getGoldenBundles - Single bundle with multiple products")
    void testGoldenBundles_SingleBundle() {
        // Arrange: Customer with multiple products
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 5, false, false, 0));
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_PAYROLL", new BigDecimal("65.00"), 3, false, false, 5, false, false, 0));
        testData.add(new SubscriptionRecord("C-1", "SMB", "MAILCHIMP", new BigDecimal("45.00"), 3, false, false, 5, false, false, 0));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        List<String> bundles = service.getGoldenBundles();

        // Assert
        assertNotNull(bundles);
        assertEquals(1, bundles.size());
        // Products are sorted alphabetically, so order is: MAILCHIMP, QB_ONLINE, QB_PAYROLL
        String bundleString = bundles.get(0);
        assertTrue(bundleString.contains("MAILCHIMP") && bundleString.contains("QB_ONLINE") && bundleString.contains("QB_PAYROLL"),
                "Bundle should contain all three products: " + bundleString);
        assertTrue(bundleString.contains("195.00"), "Bundle should contain total MRR 195.00: " + bundleString);
    }

    @Test
    @DisplayName("getGoldenBundles - Multiple bundles, top 3")
    void testGoldenBundles_MultipleBundlesTop3() {
        // Arrange: Multiple customers with different bundles
        // Bundle 1: High revenue
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("250.00"), 10, false, false, 12, false, false, 0));
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_PAYROLL", new BigDecimal("200.00"), 10, false, false, 12, false, false, 0));
        
        // Bundle 2: Medium revenue
        testData.add(new SubscriptionRecord("C-2", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 5, false, false, 0));
        testData.add(new SubscriptionRecord("C-2", "SMB", "QB_PAYROLL", new BigDecimal("65.00"), 3, false, false, 5, false, false, 0));
        
        // Bundle 3: Lower revenue
        testData.add(new SubscriptionRecord("C-3", "SMB", "QB_ONLINE", new BigDecimal("50.00"), 2, false, false, 3, false, false, 0));
        testData.add(new SubscriptionRecord("C-3", "SMB", "MAILCHIMP", new BigDecimal("30.00"), 2, false, false, 3, false, false, 0));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        List<String> bundles = service.getGoldenBundles();

        // Assert
        assertNotNull(bundles);
        assertTrue(bundles.size() <= 3, "Should have at most 3 bundles");
        assertTrue(bundles.size() >= 1, "Should have at least 1 bundle");
        
        // First bundle should be highest revenue
        // C-1: 250 + 200 = 450, C-2: 85 + 65 = 150
        // Since both have QB_ONLINE + QB_PAYROLL, they merge: 450 + 150 = 600
        String firstBundle = bundles.get(0);
        // Check for the total revenue (600.00) in the formatted string
        assertTrue(firstBundle.contains("600.00") || firstBundle.matches(".*\\$\\s*600\\.00.*"),
                "First bundle should contain total revenue 600.00 (merged from C-1 and C-2): " + firstBundle);
        // Verify it contains products from the bundle (sorted alphabetically)
        assertTrue(firstBundle.contains("QB_ONLINE") && firstBundle.contains("QB_PAYROLL"),
                "First bundle should contain QB_ONLINE and QB_PAYROLL: " + firstBundle);
    }

    @Test
    @DisplayName("getGoldenBundles - No bundles (single products only)")
    void testGoldenBundles_NoBundles() {
        // Arrange: All customers have single products
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 5, false, false, 0));
        testData.add(new SubscriptionRecord("C-2", "SMB", "QB_PAYROLL", new BigDecimal("65.00"), 2, false, false, 3, false, false, 0));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        List<String> bundles = service.getGoldenBundles();

        // Assert
        assertNotNull(bundles);
        assertTrue(bundles.isEmpty());
    }

    @Test
    @DisplayName("getGoldenBundles - Bundle normalization (A+B == B+A)")
    void testGoldenBundles_BundleNormalization() {
        // Arrange: Same products but different order should normalize
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 5, false, false, 0));
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_PAYROLL", new BigDecimal("65.00"), 3, false, false, 5, false, false, 0));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        List<String> bundles = service.getGoldenBundles();

        // Assert: Should be normalized (sorted)
        assertNotNull(bundles);
        assertEquals(1, bundles.size());
        // Bundle key should be sorted alphabetically
        assertTrue(bundles.get(0).contains("QB_ONLINE + QB_PAYROLL") || bundles.get(0).contains("QB_PAYROLL + QB_ONLINE"));
    }

    // ==================== MODULE D: At Risk Customers ====================

    @Test
    @DisplayName("getAtRiskCustomers - High MRR with churn signal")
    void testAtRiskCustomers_ChurnSignal() {
        // Arrange: High value customer with churn signal
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("250.00"), 5, false, false, 0, false, true, 10));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        List<String> alerts = service.getAtRiskCustomers();

        // Assert
        assertNotNull(alerts);
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).contains("C-1"));
        assertTrue(alerts.get(0).contains("ALERT"));
        assertTrue(alerts.get(0).contains("250.00"));
    }

    @Test
    @DisplayName("getAtRiskCustomers - High MRR with inactivity > 45 days")
    void testAtRiskCustomers_HighInactivity() {
        // Arrange: High value customer with >45 days inactive
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("100.00"), 5, false, false, 0, false, false, 50));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        List<String> alerts = service.getAtRiskCustomers();

        // Assert
        assertNotNull(alerts);
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).contains("50 days inactive"));
    }

    @Test
    @DisplayName("getAtRiskCustomers - Low MRR (should not trigger)")
    void testAtRiskCustomers_LowMRR() {
        // Arrange: Low value customer with churn signal
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("30.00"), 2, false, false, 0, false, true, 10));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        List<String> alerts = service.getAtRiskCustomers();

        // Assert: Should not include low MRR customers
        assertNotNull(alerts);
        assertTrue(alerts.isEmpty());
    }

    @Test
    @DisplayName("getAtRiskCustomers - Limit to 5 alerts")
    void testAtRiskCustomers_Limit5() {
        // Arrange: Create 10 at-risk customers
        for (int i = 1; i <= 10; i++) {
            testData.add(new SubscriptionRecord("C-" + i, "SMB", "QB_ONLINE", new BigDecimal("100.00"), 5, false, false, 0, false, true, 10));
        }
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        List<String> alerts = service.getAtRiskCustomers();

        // Assert
        assertNotNull(alerts);
        assertTrue(alerts.size() <= 5);
    }

    @Test
    @DisplayName("getAtRiskCustomers - Distinct customers")
    void testAtRiskCustomers_Distinct() {
        // Arrange: Same customer with multiple products (should be distinct)
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("100.00"), 5, false, false, 0, false, true, 10));
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_PAYROLL", new BigDecimal("100.00"), 5, false, false, 0, false, true, 10));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        List<String> alerts = service.getAtRiskCustomers();

        // Assert: Should only appear once due to distinct()
        assertNotNull(alerts);
        long count = alerts.stream().filter(a -> a.contains("C-1")).count();
        assertTrue(count <= 1);
    }

    // ==================== MODULE E: Next Best Actions ====================

    @Test
    @DisplayName("getNextBestActions - SMB high usage rule")
    void testNextBestActions_SMBHighUsage() {
        // Arrange: SMB with QB_ONLINE and >5 users
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 6, false, false, 5, false, false, 0));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        List<String> actions = service.getNextBestActions();

        // Assert
        assertNotNull(actions);
        assertEquals(1, actions.size());
        assertTrue(actions.get(0).contains("C-1"));
        assertTrue(actions.get(0).contains("ADD PAYROLL"));
        assertTrue(actions.get(0).contains("Users > 5"));
    }

    @Test
    @DisplayName("getNextBestActions - Consumer TurboTax rule")
    void testNextBestActions_ConsumerTurboTax() {
        // Arrange: CONSUMER with TURBOTAX without AI
        testData.add(new SubscriptionRecord("C-1", "CONSUMER", "TURBOTAX", new BigDecimal("0.00"), 1, false, false, 0, false, false, 10));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        List<String> actions = service.getNextBestActions();

        // Assert
        assertNotNull(actions);
        assertEquals(1, actions.size());
        assertTrue(actions.get(0).contains("C-1"));
        assertTrue(actions.get(0).contains("CREDIT KARMA"));
        assertTrue(actions.get(0).contains("Loyal User"));
    }

    @Test
    @DisplayName("getNextBestActions - High data connections rule")
    void testNextBestActions_HighDataConnections() {
        // Arrange: High data connections without AI
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 6, false, false, 0));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        List<String> actions = service.getNextBestActions();

        // Assert
        assertNotNull(actions);
        assertEquals(1, actions.size());
        assertTrue(actions.get(0).contains("C-1"));
        assertTrue(actions.get(0).contains("ENABLE AI ASSIST"));
        assertTrue(actions.get(0).contains("High Data Vol"));
    }

    @Test
    @DisplayName("getNextBestActions - No matching rules")
    void testNextBestActions_NoMatches() {
        // Arrange: Customer that doesn't match any rules
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, true, false, 3, false, false, 0));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        List<String> actions = service.getNextBestActions();

        // Assert
        assertNotNull(actions);
        assertTrue(actions.isEmpty());
    }

    @Test
    @DisplayName("getNextBestActions - Limit to 5 actions")
    void testNextBestActions_Limit5() {
        // Arrange: Create 10 customers matching rules
        for (int i = 1; i <= 10; i++) {
            testData.add(new SubscriptionRecord("C-" + i, "SMB", "QB_ONLINE", new BigDecimal("85.00"), 6, false, false, 5, false, false, 0));
        }
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        List<String> actions = service.getNextBestActions();

        // Assert
        assertNotNull(actions);
        assertTrue(actions.size() <= 5);
    }

    @Test
    @DisplayName("getNextBestActions - Multiple rules, priority order")
    void testNextBestActions_MultipleRules() {
        // Arrange: Customer matching multiple rules (SMB high usage + high data connections)
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 6, false, false, 6, false, false, 0));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        List<String> actions = service.getNextBestActions();

        // Assert: Should match first rule (SMB high usage)
        assertNotNull(actions);
        assertTrue(actions.size() >= 1);
        // First rule should be SMB high usage
        assertTrue(actions.stream().anyMatch(a -> a.contains("ADD PAYROLL")));
    }

    // ==================== MODULE F: Segment Statistics ====================

    @Test
    @DisplayName("getSegmentStatistics - Single segment")
    void testSegmentStatistics_SingleSegment() {
        // Arrange
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 5, false, false, 0));
        testData.add(new SubscriptionRecord("C-2", "SMB", "QB_PAYROLL", new BigDecimal("65.00"), 2, false, false, 3, false, false, 0));
        testData.add(new SubscriptionRecord("C-3", "SMB", "QB_ONLINE", new BigDecimal("100.00"), 4, false, false, 6, false, false, 0));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        Map<String, DoubleSummaryStatistics> stats = service.getSegmentStatistics();

        // Assert
        assertNotNull(stats);
        assertTrue(stats.containsKey("SMB"));
        DoubleSummaryStatistics smbStats = stats.get("SMB");
        assertEquals(3, smbStats.getCount());
        assertEquals(250.00, smbStats.getSum(), 0.01);
        assertEquals(83.33, smbStats.getAverage(), 0.01);
        assertEquals(100.00, smbStats.getMax(), 0.01);
        assertEquals(65.00, smbStats.getMin(), 0.01);
    }

    @Test
    @DisplayName("getSegmentStatistics - Multiple segments")
    void testSegmentStatistics_MultipleSegments() {
        // Arrange
        testData.add(new SubscriptionRecord("C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 5, false, false, 0));
        testData.add(new SubscriptionRecord("C-2", "CONSUMER", "TURBOTAX", new BigDecimal("0.00"), 1, false, false, 0, false, false, 10));
        testData.add(new SubscriptionRecord("C-3", "ACCOUNTANT", "QB_ONLINE_ADVANCED", new BigDecimal("250.00"), 15, true, true, 25, false, false, 0));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        Map<String, DoubleSummaryStatistics> stats = service.getSegmentStatistics();

        // Assert
        assertNotNull(stats);
        assertEquals(3, stats.size());
        assertTrue(stats.containsKey("SMB"));
        assertTrue(stats.containsKey("CONSUMER"));
        assertTrue(stats.containsKey("ACCOUNTANT"));
        
        assertEquals(85.00, stats.get("SMB").getAverage(), 0.01);
        assertEquals(0.00, stats.get("CONSUMER").getAverage(), 0.01);
        assertEquals(250.00, stats.get("ACCOUNTANT").getAverage(), 0.01);
    }

    @Test
    @DisplayName("getSegmentStatistics - Empty dataset")
    void testSegmentStatistics_EmptyData() {
        // Act
        Map<String, DoubleSummaryStatistics> stats = service.getSegmentStatistics();

        // Assert
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
    }

    @Test
    @DisplayName("getSegmentStatistics - Zero MRR values")
    void testSegmentStatistics_ZeroMRR() {
        // Arrange: All zero MRR
        testData.add(new SubscriptionRecord("C-1", "CONSUMER", "TURBOTAX", new BigDecimal("0.00"), 1, false, false, 0, false, false, 10));
        testData.add(new SubscriptionRecord("C-2", "CONSUMER", "CREDIT_KARMA", new BigDecimal("0.00"), 1, true, false, 1, false, false, 1));
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        Map<String, DoubleSummaryStatistics> stats = service.getSegmentStatistics();

        // Assert
        assertNotNull(stats);
        DoubleSummaryStatistics consumerStats = stats.get("CONSUMER");
        assertEquals(2, consumerStats.getCount());
        assertEquals(0.00, consumerStats.getSum(), 0.01);
        assertEquals(0.00, consumerStats.getAverage(), 0.01);
        assertEquals(0.00, consumerStats.getMax(), 0.01);
        assertEquals(0.00, consumerStats.getMin(), 0.01);
    }

    @Test
    @DisplayName("getSegmentStatistics - Large dataset aggregation")
    void testSegmentStatistics_LargeDataset() {
        // Arrange: Create 100 records for SMB segment
        for (int i = 1; i <= 100; i++) {
            testData.add(new SubscriptionRecord("C-" + i, "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 5, false, false, 0));
        }
        
        service = new ProsperityAnalyticsService(testData);

        // Act
        Map<String, DoubleSummaryStatistics> stats = service.getSegmentStatistics();

        // Assert
        assertNotNull(stats);
        DoubleSummaryStatistics smbStats = stats.get("SMB");
        assertEquals(100, smbStats.getCount());
        assertEquals(8500.00, smbStats.getSum(), 0.01);
        assertEquals(85.00, smbStats.getAverage(), 0.01);
    }

    // ==================== Edge Cases and Integration Tests ====================

    @Test
    @DisplayName("Constructor - Null data handling")
    void testConstructor_NullData() {
        // Act & Assert: Constructor accepts null (no validation)
        // Note: This will cause NPE when methods are called, but constructor itself doesn't throw
        assertDoesNotThrow(() -> {
            ProsperityAnalyticsService service = new ProsperityAnalyticsService(null);
            assertNotNull(service);
        });
    }

    @Test
    @DisplayName("Integration - All modules with comprehensive dataset")
    void testIntegration_AllModules() {
        // Arrange: Comprehensive dataset covering all scenarios
        testData.add(new SubscriptionRecord("C-9999", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 10, true, true, 12, false, false, 1));
        testData.add(new SubscriptionRecord("C-9999", "SMB", "QB_PAYROLL", new BigDecimal("65.00"), 10, true, true, 12, false, false, 1));
        testData.add(new SubscriptionRecord("C-9999", "SMB", "MAILCHIMP", new BigDecimal("45.00"), 10, true, true, 12, false, false, 1));
        testData.add(new SubscriptionRecord("C-8821", "SMB", "QB_ONLINE", new BigDecimal("250.00"), 5, false, false, 0, false, true, 45));
        testData.add(new SubscriptionRecord("C-7721", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 12, false, false, 5, false, false, 2));
        
        service = new ProsperityAnalyticsService(testData);

        // Act: Test all modules
        Map<Integer, Long> distribution = service.getProductCountDistribution();
        Map<String, Map<String, BigDecimal>> matrix = service.getRevenueMatrix();
        List<String> bundles = service.getGoldenBundles();
        List<String> alerts = service.getAtRiskCustomers();
        List<String> actions = service.getNextBestActions();
        Map<String, DoubleSummaryStatistics> stats = service.getSegmentStatistics();

        // Assert: All modules return non-null results
        assertNotNull(distribution);
        assertNotNull(matrix);
        assertNotNull(bundles);
        assertNotNull(alerts);
        assertNotNull(actions);
        assertNotNull(stats);
        
        // Verify specific expectations
        assertTrue(bundles.size() > 0);
        assertTrue(alerts.size() > 0);
        assertTrue(actions.size() > 0);
    }
}

