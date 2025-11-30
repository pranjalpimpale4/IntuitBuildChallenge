package com.pranjal.assign2.service;

import com.pranjal.assign2.model.SubscriptionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for DataIngestionService.
 * Tests stream operations, parallel processing, and file I/O.
 */
@DisplayName("DataIngestionService Test Suite")
public class DataIngestionServiceTest {

    private DataIngestionService service;

    @BeforeEach
    void setUp() {
        service = new DataIngestionService();
    }

    @Test
    @DisplayName("loadSubscriptions - Valid CSV file")
    void testLoadSubscriptions_ValidFile() throws IOException {
        // Arrange: Create a temporary CSV file
        Path tempFile = Files.createTempFile("test_data", ".csv");
        String csvContent = "customer_id,segment,product,mrr,active_users,ai_enabled,live_expert,data_connections,is_trial,churn_signal,days_inactive\n" +
                "C-1,SMB,QB_ONLINE,85.00,3,false,false,5,false,false,0\n" +
                "C-2,CONSUMER,TURBOTAX,0.00,1,false,false,0,false,false,10";
        Files.write(tempFile, csvContent.getBytes());

        // Act
        List<SubscriptionRecord> records = service.loadSubscriptions(tempFile.toString());

        // Assert
        assertNotNull(records);
        assertEquals(2, records.size());
        
        SubscriptionRecord record1 = records.get(0);
        assertEquals("C-1", record1.customerId());
        assertEquals("SMB", record1.segment());
        assertEquals("QB_ONLINE", record1.product());
        assertEquals(new BigDecimal("85.00"), record1.mrr());
        assertEquals(3, record1.activeUsers());
        assertFalse(record1.aiEnabled());
        assertFalse(record1.liveExpert());
        assertEquals(5, record1.dataConnections());
        assertFalse(record1.isTrial());
        assertFalse(record1.churnSignal());
        assertEquals(0, record1.daysInactive());

        // Cleanup
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("loadSubscriptions - Skip header row")
    void testLoadSubscriptions_SkipHeader() throws IOException {
        // Arrange: CSV with header
        Path tempFile = Files.createTempFile("test_data", ".csv");
        String csvContent = "customer_id,segment,product,mrr,active_users,ai_enabled,live_expert,data_connections,is_trial,churn_signal,days_inactive\n" +
                "C-1,SMB,QB_ONLINE,85.00,3,false,false,5,false,false,0";
        Files.write(tempFile, csvContent.getBytes());

        // Act
        List<SubscriptionRecord> records = service.loadSubscriptions(tempFile.toString());

        // Assert: Header should be skipped
        assertEquals(1, records.size());
        assertNotEquals("customer_id", records.get(0).customerId());

        // Cleanup
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("loadSubscriptions - Filter empty lines")
    void testLoadSubscriptions_FilterEmptyLines() throws IOException {
        // Arrange: CSV with empty lines
        Path tempFile = Files.createTempFile("test_data", ".csv");
        String csvContent = "customer_id,segment,product,mrr,active_users,ai_enabled,live_expert,data_connections,is_trial,churn_signal,days_inactive\n" +
                "C-1,SMB,QB_ONLINE,85.00,3,false,false,5,false,false,0\n" +
                "\n" +
                "C-2,CONSUMER,TURBOTAX,0.00,1,false,false,0,false,false,10\n" +
                "   \n";
        Files.write(tempFile, csvContent.getBytes());

        // Act
        List<SubscriptionRecord> records = service.loadSubscriptions(tempFile.toString());

        // Assert: Empty lines should be filtered out
        assertEquals(2, records.size());

        // Cleanup
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("loadSubscriptions - Large dataset (parallel processing)")
    void testLoadSubscriptions_LargeDataset() throws IOException {
        // Arrange: Create CSV with 1000 records
        Path tempFile = Files.createTempFile("test_data", ".csv");
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("customer_id,segment,product,mrr,active_users,ai_enabled,live_expert,data_connections,is_trial,churn_signal,days_inactive\n");
        
        for (int i = 1; i <= 1000; i++) {
            csvContent.append(String.format("C-%d,SMB,QB_ONLINE,85.00,3,false,false,5,false,false,0\n", i));
        }
        Files.write(tempFile, csvContent.toString().getBytes());

        // Act
        long startTime = System.currentTimeMillis();
        List<SubscriptionRecord> records = service.loadSubscriptions(tempFile.toString());
        long endTime = System.currentTimeMillis();

        // Assert
        assertNotNull(records);
        assertEquals(1000, records.size());
        // Parallel processing should complete (verification that it doesn't hang)
        assertTrue((endTime - startTime) < 10000); // Should complete in reasonable time

        // Cleanup
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("loadSubscriptions - Boolean parsing (true values)")
    void testLoadSubscriptions_BooleanTrue() throws IOException {
        // Arrange: CSV with true boolean values
        Path tempFile = Files.createTempFile("test_data", ".csv");
        String csvContent = "customer_id,segment,product,mrr,active_users,ai_enabled,live_expert,data_connections,is_trial,churn_signal,days_inactive\n" +
                "C-1,SMB,QB_ONLINE,85.00,3,true,true,5,true,true,0";
        Files.write(tempFile, csvContent.getBytes());

        // Act
        List<SubscriptionRecord> records = service.loadSubscriptions(tempFile.toString());

        // Assert
        assertEquals(1, records.size());
        SubscriptionRecord record = records.get(0);
        assertTrue(record.aiEnabled());
        assertTrue(record.liveExpert());
        assertTrue(record.isTrial());
        assertTrue(record.churnSignal());

        // Cleanup
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("loadSubscriptions - Boolean parsing (false values)")
    void testLoadSubscriptions_BooleanFalse() throws IOException {
        // Arrange: CSV with false boolean values
        Path tempFile = Files.createTempFile("test_data", ".csv");
        String csvContent = "customer_id,segment,product,mrr,active_users,ai_enabled,live_expert,data_connections,is_trial,churn_signal,days_inactive\n" +
                "C-1,SMB,QB_ONLINE,85.00,3,false,false,5,false,false,0";
        Files.write(tempFile, csvContent.getBytes());

        // Act
        List<SubscriptionRecord> records = service.loadSubscriptions(tempFile.toString());

        // Assert
        assertEquals(1, records.size());
        SubscriptionRecord record = records.get(0);
        assertFalse(record.aiEnabled());
        assertFalse(record.liveExpert());
        assertFalse(record.isTrial());
        assertFalse(record.churnSignal());

        // Cleanup
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("loadSubscriptions - Decimal MRR values")
    void testLoadSubscriptions_DecimalMRR() throws IOException {
        // Arrange: CSV with various decimal MRR values
        Path tempFile = Files.createTempFile("test_data", ".csv");
        String csvContent = "customer_id,segment,product,mrr,active_users,ai_enabled,live_expert,data_connections,is_trial,churn_signal,days_inactive\n" +
                "C-1,SMB,QB_ONLINE,85.50,3,false,false,5,false,false,0\n" +
                "C-2,SMB,QB_PAYROLL,65.75,2,false,false,3,false,false,0\n" +
                "C-3,CONSUMER,TURBOTAX,0.00,1,false,false,0,false,false,10";
        Files.write(tempFile, csvContent.getBytes());

        // Act
        List<SubscriptionRecord> records = service.loadSubscriptions(tempFile.toString());

        // Assert
        assertEquals(3, records.size());
        assertEquals(new BigDecimal("85.50"), records.get(0).mrr());
        assertEquals(new BigDecimal("65.75"), records.get(1).mrr());
        assertEquals(new BigDecimal("0.00"), records.get(2).mrr());

        // Cleanup
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("loadSubscriptions - All segments")
    void testLoadSubscriptions_AllSegments() throws IOException {
        // Arrange: CSV with all segment types
        Path tempFile = Files.createTempFile("test_data", ".csv");
        String csvContent = "customer_id,segment,product,mrr,active_users,ai_enabled,live_expert,data_connections,is_trial,churn_signal,days_inactive\n" +
                "C-1,SMB,QB_ONLINE,85.00,3,false,false,5,false,false,0\n" +
                "C-2,CONSUMER,TURBOTAX,0.00,1,false,false,0,false,false,10\n" +
                "C-3,ACCOUNTANT,QB_ONLINE_ADVANCED,250.00,15,true,true,25,false,false,0";
        Files.write(tempFile, csvContent.getBytes());

        // Act
        List<SubscriptionRecord> records = service.loadSubscriptions(tempFile.toString());

        // Assert
        assertEquals(3, records.size());
        assertEquals("SMB", records.get(0).segment());
        assertEquals("CONSUMER", records.get(1).segment());
        assertEquals("ACCOUNTANT", records.get(2).segment());

        // Cleanup
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("loadSubscriptions - File not found")
    void testLoadSubscriptions_FileNotFound() {
        // Act
        List<SubscriptionRecord> records = service.loadSubscriptions("non_existent_file.csv");

        // Assert: Should return empty list on error
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    @DisplayName("loadSubscriptions - Invalid file path")
    void testLoadSubscriptions_InvalidPath() {
        // Act
        List<SubscriptionRecord> records = service.loadSubscriptions("");

        // Assert: Should return empty list on error
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    @DisplayName("loadSubscriptions - Empty file (only header)")
    void testLoadSubscriptions_EmptyFile() throws IOException {
        // Arrange: CSV with only header
        Path tempFile = Files.createTempFile("test_data", ".csv");
        String csvContent = "customer_id,segment,product,mrr,active_users,ai_enabled,live_expert,data_connections,is_trial,churn_signal,days_inactive\n";
        Files.write(tempFile, csvContent.getBytes());

        // Act
        List<SubscriptionRecord> records = service.loadSubscriptions(tempFile.toString());

        // Assert: Should return empty list
        assertNotNull(records);
        assertTrue(records.isEmpty());

        // Cleanup
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("loadSubscriptions - Stream parallel processing verification")
    void testLoadSubscriptions_ParallelProcessing() throws IOException {
        // Arrange: Create CSV with sufficient records to benefit from parallel processing
        Path tempFile = Files.createTempFile("test_data", ".csv");
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("customer_id,segment,product,mrr,active_users,ai_enabled,live_expert,data_connections,is_trial,churn_signal,days_inactive\n");
        
        for (int i = 1; i <= 500; i++) {
            csvContent.append(String.format("C-%d,SMB,QB_ONLINE,85.00,3,false,false,5,false,false,0\n", i));
        }
        Files.write(tempFile, csvContent.toString().getBytes());

        // Act
        List<SubscriptionRecord> records = service.loadSubscriptions(tempFile.toString());

        // Assert: All records should be loaded correctly
        assertNotNull(records);
        assertEquals(500, records.size());
        // Verify all records are properly parsed
        assertTrue(records.stream().allMatch(r -> r.customerId().startsWith("C-")));
        assertTrue(records.stream().allMatch(r -> r.segment().equals("SMB")));
        assertTrue(records.stream().allMatch(r -> r.product().equals("QB_ONLINE")));

        // Cleanup
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("loadSubscriptions - Various integer values")
    void testLoadSubscriptions_VariousIntegers() throws IOException {
        // Arrange: CSV with various integer values
        Path tempFile = Files.createTempFile("test_data", ".csv");
        String csvContent = "customer_id,segment,product,mrr,active_users,ai_enabled,live_expert,data_connections,is_trial,churn_signal,days_inactive\n" +
                "C-1,SMB,QB_ONLINE,85.00,1,false,false,0,false,false,0\n" +
                "C-2,SMB,QB_ONLINE,85.00,15,false,false,25,false,false,300\n" +
                "C-3,SMB,QB_ONLINE,85.00,100,false,false,50,false,false,1";
        Files.write(tempFile, csvContent.getBytes());

        // Act
        List<SubscriptionRecord> records = service.loadSubscriptions(tempFile.toString());

        // Assert
        assertEquals(3, records.size());
        assertEquals(1, records.get(0).activeUsers());
        assertEquals(15, records.get(1).activeUsers());
        assertEquals(100, records.get(2).activeUsers());
        assertEquals(0, records.get(0).dataConnections());
        assertEquals(25, records.get(1).dataConnections());
        assertEquals(50, records.get(2).dataConnections());
        assertEquals(0, records.get(0).daysInactive());
        assertEquals(300, records.get(1).daysInactive());
        assertEquals(1, records.get(2).daysInactive());

        // Cleanup
        Files.deleteIfExists(tempFile);
    }
}

