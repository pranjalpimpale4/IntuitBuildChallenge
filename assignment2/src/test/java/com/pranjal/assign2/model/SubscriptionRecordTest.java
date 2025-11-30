package com.pranjal.assign2.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SubscriptionRecord model.
 * Tests record immutability, helper methods, and data integrity.
 */
@DisplayName("SubscriptionRecord Test Suite")
public class SubscriptionRecordTest {

    @Test
    @DisplayName("Record creation - All fields")
    void testRecordCreation_AllFields() {
        // Act
        SubscriptionRecord record = new SubscriptionRecord(
            "C-1",
            "SMB",
            "QB_ONLINE",
            new BigDecimal("85.00"),
            3,
            true,
            false,
            5,
            false,
            true,
            10
        );

        // Assert
        assertEquals("C-1", record.customerId());
        assertEquals("SMB", record.segment());
        assertEquals("QB_ONLINE", record.product());
        assertEquals(new BigDecimal("85.00"), record.mrr());
        assertEquals(3, record.activeUsers());
        assertTrue(record.aiEnabled());
        assertFalse(record.liveExpert());
        assertEquals(5, record.dataConnections());
        assertFalse(record.isTrial());
        assertTrue(record.churnSignal());
        assertEquals(10, record.daysInactive());
    }

    @Test
    @DisplayName("isHighValue - MRR above threshold")
    void testIsHighValue_AboveThreshold() {
        // Arrange
        SubscriptionRecord record = new SubscriptionRecord(
            "C-1", "SMB", "QB_ONLINE", new BigDecimal("100.00"), 3, false, false, 5, false, false, 0
        );

        // Act & Assert
        assertTrue(record.isHighValue());
    }

    @Test
    @DisplayName("isHighValue - MRR at threshold")
    void testIsHighValue_AtThreshold() {
        // Arrange
        SubscriptionRecord record = new SubscriptionRecord(
            "C-1", "SMB", "QB_ONLINE", new BigDecimal("50.00"), 3, false, false, 5, false, false, 0
        );

        // Act & Assert
        assertFalse(record.isHighValue()); // Should be > 50.00, not >=
    }

    @Test
    @DisplayName("isHighValue - MRR below threshold")
    void testIsHighValue_BelowThreshold() {
        // Arrange
        SubscriptionRecord record = new SubscriptionRecord(
            "C-1", "SMB", "QB_ONLINE", new BigDecimal("30.00"), 3, false, false, 5, false, false, 0
        );

        // Act & Assert
        assertFalse(record.isHighValue());
    }

    @Test
    @DisplayName("isHighValue - Zero MRR")
    void testIsHighValue_ZeroMRR() {
        // Arrange
        SubscriptionRecord record = new SubscriptionRecord(
            "C-1", "CONSUMER", "TURBOTAX", new BigDecimal("0.00"), 1, false, false, 0, false, false, 10
        );

        // Act & Assert
        assertFalse(record.isHighValue());
    }

    @Test
    @DisplayName("isHighValue - Very high MRR")
    void testIsHighValue_VeryHighMRR() {
        // Arrange
        SubscriptionRecord record = new SubscriptionRecord(
            "C-1", "ACCOUNTANT", "QB_ONLINE_ADVANCED", new BigDecimal("1000.00"), 15, true, true, 25, false, false, 0
        );

        // Act & Assert
        assertTrue(record.isHighValue());
    }

    @Test
    @DisplayName("Record immutability - All segments")
    void testRecordImmutability_AllSegments() {
        // Arrange & Act
        SubscriptionRecord smb = new SubscriptionRecord(
            "C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 5, false, false, 0
        );
        SubscriptionRecord consumer = new SubscriptionRecord(
            "C-2", "CONSUMER", "TURBOTAX", new BigDecimal("0.00"), 1, false, false, 0, false, false, 10
        );
        SubscriptionRecord accountant = new SubscriptionRecord(
            "C-3", "ACCOUNTANT", "QB_ONLINE_ADVANCED", new BigDecimal("250.00"), 15, true, true, 25, false, false, 0
        );

        // Assert
        assertEquals("SMB", smb.segment());
        assertEquals("CONSUMER", consumer.segment());
        assertEquals("ACCOUNTANT", accountant.segment());
    }

    @Test
    @DisplayName("Record immutability - All products")
    void testRecordImmutability_AllProducts() {
        // Arrange & Act
        SubscriptionRecord qbOnline = new SubscriptionRecord(
            "C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 5, false, false, 0
        );
        SubscriptionRecord qbPayroll = new SubscriptionRecord(
            "C-2", "SMB", "QB_PAYROLL", new BigDecimal("65.00"), 2, false, false, 3, false, false, 0
        );
        SubscriptionRecord turbotax = new SubscriptionRecord(
            "C-3", "CONSUMER", "TURBOTAX", new BigDecimal("0.00"), 1, false, false, 0, false, false, 10
        );
        SubscriptionRecord creditKarma = new SubscriptionRecord(
            "C-4", "CONSUMER", "CREDIT_KARMA", new BigDecimal("0.00"), 1, true, false, 1, false, false, 1
        );
        SubscriptionRecord mailchimp = new SubscriptionRecord(
            "C-5", "SMB", "MAILCHIMP", new BigDecimal("45.00"), 3, true, true, 5, false, false, 0
        );
        SubscriptionRecord qbAdvanced = new SubscriptionRecord(
            "C-6", "ACCOUNTANT", "QB_ONLINE_ADVANCED", new BigDecimal("250.00"), 15, true, true, 25, false, false, 0
        );

        // Assert
        assertEquals("QB_ONLINE", qbOnline.product());
        assertEquals("QB_PAYROLL", qbPayroll.product());
        assertEquals("TURBOTAX", turbotax.product());
        assertEquals("CREDIT_KARMA", creditKarma.product());
        assertEquals("MAILCHIMP", mailchimp.product());
        assertEquals("QB_ONLINE_ADVANCED", qbAdvanced.product());
    }

    @Test
    @DisplayName("Record - Boolean flags combinations")
    void testRecord_BooleanCombinations() {
        // Test all combinations of boolean flags
        SubscriptionRecord allTrue = new SubscriptionRecord(
            "C-1", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, true, true, 5, true, true, 0
        );
        SubscriptionRecord allFalse = new SubscriptionRecord(
            "C-2", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, false, false, 5, false, false, 0
        );
        SubscriptionRecord mixed = new SubscriptionRecord(
            "C-3", "SMB", "QB_ONLINE", new BigDecimal("85.00"), 3, true, false, 5, false, true, 0
        );

        // Assert
        assertTrue(allTrue.aiEnabled() && allTrue.liveExpert() && allTrue.isTrial() && allTrue.churnSignal());
        assertFalse(allFalse.aiEnabled() || allFalse.liveExpert() || allFalse.isTrial() || allFalse.churnSignal());
        assertTrue(mixed.aiEnabled() && !mixed.liveExpert() && !mixed.isTrial() && mixed.churnSignal());
    }

    @Test
    @DisplayName("Record - Edge case values")
    void testRecord_EdgeCaseValues() {
        // Arrange & Act: Test boundary values
        SubscriptionRecord zeroValues = new SubscriptionRecord(
            "C-1", "SMB", "QB_ONLINE", new BigDecimal("0.00"), 0, false, false, 0, false, false, 0
        );
        SubscriptionRecord maxValues = new SubscriptionRecord(
            "C-2", "SMB", "QB_ONLINE", new BigDecimal("9999.99"), 1000, true, true, 100, true, true, 365
        );

        // Assert
        assertEquals(0, zeroValues.activeUsers());
        assertEquals(0, zeroValues.dataConnections());
        assertEquals(0, zeroValues.daysInactive());
        assertEquals(new BigDecimal("0.00"), zeroValues.mrr());
        
        assertEquals(1000, maxValues.activeUsers());
        assertEquals(100, maxValues.dataConnections());
        assertEquals(365, maxValues.daysInactive());
        assertEquals(new BigDecimal("9999.99"), maxValues.mrr());
    }

    @Test
    @DisplayName("Record - Decimal precision in MRR")
    void testRecord_DecimalPrecision() {
        // Arrange & Act: Test various decimal precisions
        SubscriptionRecord twoDecimals = new SubscriptionRecord(
            "C-1", "SMB", "QB_ONLINE", new BigDecimal("85.50"), 3, false, false, 5, false, false, 0
        );
        SubscriptionRecord threeDecimals = new SubscriptionRecord(
            "C-2", "SMB", "QB_ONLINE", new BigDecimal("85.555"), 3, false, false, 5, false, false, 0
        );

        // Assert
        assertEquals(new BigDecimal("85.50"), twoDecimals.mrr());
        assertEquals(new BigDecimal("85.555"), threeDecimals.mrr());
    }

    @Test
    @DisplayName("isHighValue - Boundary testing")
    void testIsHighValue_BoundaryTesting() {
        // Test values just above and below threshold
        SubscriptionRecord justBelow = new SubscriptionRecord(
            "C-1", "SMB", "QB_ONLINE", new BigDecimal("50.00"), 3, false, false, 5, false, false, 0
        );
        SubscriptionRecord justAbove = new SubscriptionRecord(
            "C-2", "SMB", "QB_ONLINE", new BigDecimal("50.01"), 3, false, false, 5, false, false, 0
        );

        // Assert
        assertFalse(justBelow.isHighValue());
        assertTrue(justAbove.isHighValue());
    }
}

