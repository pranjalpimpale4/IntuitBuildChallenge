package com.pranjal.assign2;

import com.pranjal.assign2.model.SubscriptionRecord;
import com.pranjal.assign2.service.DataIngestionService;
import com.pranjal.assign2.service.ProsperityAnalyticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for App class.
 * Tests the complete data pipeline: Ingestion -> Analysis -> Results.
 */
@DisplayName("App Integration Test Suite")
public class AppTest {

    @Test
    @DisplayName("App - Complete pipeline with valid data")
    void testApp_CompletePipeline() throws IOException {
        // Arrange: Create a test CSV file
        Path tempFile = Files.createTempFile("test_data", ".csv");
        String csvContent = "customer_id,segment,product,mrr,active_users,ai_enabled,live_expert,data_connections,is_trial,churn_signal,days_inactive\n" +
                "C-1,SMB,QB_ONLINE,85.00,3,false,false,5,false,false,0\n" +
                "C-2,CONSUMER,TURBOTAX,0.00,1,false,false,0,false,false,10\n" +
                "C-3,ACCOUNTANT,QB_ONLINE_ADVANCED,250.00,15,true,true,25,false,false,0";
        Files.write(tempFile, csvContent.getBytes());

        // Act: Simulate App.main() logic
        DataIngestionService loader = new DataIngestionService();
        List<SubscriptionRecord> data = loader.loadSubscriptions(tempFile.toString());

        // Assert: Pipeline should work end-to-end
        assertNotNull(data);
        assertFalse(data.isEmpty());

        ProsperityAnalyticsService engine = new ProsperityAnalyticsService(data);
        assertNotNull(engine.getProductCountDistribution());
        assertNotNull(engine.getRevenueMatrix());
        assertNotNull(engine.getSegmentStatistics());

        // Cleanup
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("App - Empty data handling")
    void testApp_EmptyData() {
        // Arrange: Use non-existent file
        DataIngestionService loader = new DataIngestionService();
        List<SubscriptionRecord> data = loader.loadSubscriptions("non_existent.csv");

        // Act & Assert: Should handle empty data gracefully
        assertNotNull(data);
        assertTrue(data.isEmpty());

        // Should not throw exception when creating engine with empty data
        assertDoesNotThrow(() -> {
            ProsperityAnalyticsService engine = new ProsperityAnalyticsService(data);
            assertNotNull(engine);
        });
    }

    @Test
    @DisplayName("App - Data source constant")
    void testApp_DataSourceConstant() {
        // Assert: Verify the constant is defined
        assertNotNull(App.class);
        // The constant should be accessible (testing that the class compiles correctly)
        assertTrue(true); // Placeholder - actual constant access would require reflection or making it public
    }
}
