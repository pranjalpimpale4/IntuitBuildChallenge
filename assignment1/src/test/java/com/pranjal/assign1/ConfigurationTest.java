package com.pranjal.assign1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Configuration class.
 * Tests factory methods, validation, and calculations.
 */
class ConfigurationTest {

    // ==========================================
    // FACTORY METHOD TESTS
    // ==========================================

    @Test
    void testWithDefaults() {
        Configuration config = Configuration.withDefaults();

        assertNotNull(config);
        assertEquals(4, config.getNumberOfProducers());
        assertEquals(20, config.getItemsPerProducer());
        assertEquals(2, config.getNumberOfConsumers());
        assertEquals(10, config.getQueueCapacity());
    }

    @Test
    void testCustomConfiguration() {
        Configuration config = Configuration.custom(5, 100, 3, 50);

        assertNotNull(config);
        assertEquals(5, config.getNumberOfProducers());
        assertEquals(100, config.getItemsPerProducer());
        assertEquals(3, config.getNumberOfConsumers());
        assertEquals(50, config.getQueueCapacity());
    }

    @Test
    void testCustomWithMinimalValues() {
        Configuration config = Configuration.custom(1, 1, 1, 1);

        assertEquals(1, config.getNumberOfProducers());
        assertEquals(1, config.getItemsPerProducer());
        assertEquals(1, config.getNumberOfConsumers());
        assertEquals(1, config.getQueueCapacity());
    }

    @Test
    void testCustomWithLargeValues() {
        Configuration config = Configuration.custom(100, 1000, 50, 500);

        assertEquals(100, config.getNumberOfProducers());
        assertEquals(1000, config.getItemsPerProducer());
        assertEquals(50, config.getNumberOfConsumers());
        assertEquals(500, config.getQueueCapacity());
    }

    // ==========================================
    // GETTER TESTS
    // ==========================================

    @Test
    void testAllGetters() {
        Configuration config = Configuration.custom(7, 15, 4, 20);

        assertEquals(7, config.getNumberOfProducers());
        assertEquals(15, config.getItemsPerProducer());
        assertEquals(4, config.getNumberOfConsumers());
        assertEquals(20, config.getQueueCapacity());
    }

    // ==========================================
    // CALCULATION TESTS
    // ==========================================

    @Test
    void testTotalExpectedItemsCalculation() {
        Configuration config = Configuration.custom(5, 10, 2, 20);

        // 5 producers × 10 items each = 50 total
        assertEquals(50, config.getTotalExpectedItems());
    }

    @Test
    void testTotalExpectedItemsWithDefaults() {
        Configuration config = Configuration.withDefaults();

        // 4 producers × 20 items each = 80 total
        assertEquals(80, config.getTotalExpectedItems());
    }

    @Test
    void testTotalExpectedItemsLargeNumbers() {
        Configuration config = Configuration.custom(100, 1000, 10, 50);

        // 100 producers × 1000 items each = 100,000 total
        assertEquals(100000, config.getTotalExpectedItems());
    }

    // ==========================================
    // VALIDATION TESTS
    // ==========================================

    @Test
    void testValidateWithValidConfiguration() {
        Configuration config = Configuration.custom(5, 10, 3, 15);

        // Should not throw exception
        assertDoesNotThrow(() -> config.validate());
    }

    @Test
    void testValidateWithZeroProducers() {
        Configuration config = Configuration.custom(0, 10, 3, 15);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            config.validate();
        });

        assertTrue(exception.getMessage().contains("positive"));
    }

    @Test
    void testValidateWithNegativeProducers() {
        Configuration config = Configuration.custom(-1, 10, 3, 15);

        assertThrows(IllegalArgumentException.class, () -> config.validate());
    }

    @Test
    void testValidateWithZeroItemsPerProducer() {
        Configuration config = Configuration.custom(5, 0, 3, 15);

        assertThrows(IllegalArgumentException.class, () -> config.validate());
    }

    @Test
    void testValidateWithNegativeItemsPerProducer() {
        Configuration config = Configuration.custom(5, -5, 3, 15);

        assertThrows(IllegalArgumentException.class, () -> config.validate());
    }

    @Test
    void testValidateWithZeroConsumers() {
        Configuration config = Configuration.custom(5, 10, 0, 15);

        assertThrows(IllegalArgumentException.class, () -> config.validate());
    }

    @Test
    void testValidateWithNegativeConsumers() {
        Configuration config = Configuration.custom(5, 10, -2, 15);

        assertThrows(IllegalArgumentException.class, () -> config.validate());
    }

    @Test
    void testValidateWithZeroCapacity() {
        Configuration config = Configuration.custom(5, 10, 3, 0);

        assertThrows(IllegalArgumentException.class, () -> config.validate());
    }

    @Test
    void testValidateWithNegativeCapacity() {
        Configuration config = Configuration.custom(5, 10, 3, -10);

        assertThrows(IllegalArgumentException.class, () -> config.validate());
    }

    @Test
    void testValidateWithMultipleZeros() {
        Configuration config = Configuration.custom(0, 0, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> config.validate());
    }

    // ==========================================
    // TO_STRING TESTS
    // ==========================================

    @Test
    void testToStringFormat() {
        Configuration config = Configuration.custom(5, 10, 3, 15);

        String result = config.toString();

        // Verify format contains all values
        assertTrue(result.contains("Configuration{"));
        assertTrue(result.contains("producers=5"));
        assertTrue(result.contains("itemsPerProducer=10"));
        assertTrue(result.contains("consumers=3"));
        assertTrue(result.contains("capacity=15"));
        assertTrue(result.contains("totalItems=50"));
    }

    @Test
    void testToStringWithDefaults() {
        Configuration config = Configuration.withDefaults();

        String result = config.toString();

        assertTrue(result.contains("producers=4"));
        assertTrue(result.contains("itemsPerProducer=20"));
        assertTrue(result.contains("consumers=2"));
        assertTrue(result.contains("capacity=10"));
        assertTrue(result.contains("totalItems=80"));
    }

    @Test
    void testToStringNotNull() {
        Configuration config = Configuration.withDefaults();

        assertNotNull(config.toString());
        assertFalse(config.toString().isEmpty());
    }

    // ==========================================
    // IMMUTABILITY TESTS
    // ==========================================

    @Test
    void testConfigurationIsImmutable() {
        Configuration config = Configuration.custom(5, 10, 3, 15);

        // Get values
        int producers1 = config.getNumberOfProducers();
        int items1 = config.getItemsPerProducer();

        // Call getters again
        int producers2 = config.getNumberOfProducers();
        int items2 = config.getItemsPerProducer();

        // Values should be the same (no setters exist to change them)
        assertEquals(producers1, producers2);
        assertEquals(items1, items2);
    }

    // ==========================================
    // EDGE CASE TESTS
    // ==========================================

    @Test
    void testVeryLargeConfiguration() {
        Configuration config = Configuration.custom(
            Integer.MAX_VALUE / 1000,
            1000,
            100,
            1000
        );

        assertDoesNotThrow(() -> config.validate());
        assertTrue(config.getTotalExpectedItems() > 0);
    }

    @Test
    void testAsymmetricProducerConsumerRatio() {
        // Many producers, few consumers
        Configuration config1 = Configuration.custom(100, 10, 1, 50);
        assertDoesNotThrow(() -> config1.validate());

        // Few producers, many consumers
        Configuration config2 = Configuration.custom(1, 100, 50, 10);
        assertDoesNotThrow(() -> config2.validate());
    }
}

