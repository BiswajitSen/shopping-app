package com.shopapp.integration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MongoDB Connection Test")
class MongoConnectionTest {

    @Test
    @DisplayName("Should connect to MongoDB")
    void shouldConnectToMongoDB() {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            // Test connection by listing databases
            var databases = mongoClient.listDatabases().first();
            assertNotNull(databases, "Should be able to connect to MongoDB and list databases");
        } catch (Exception e) {
            fail("Failed to connect to MongoDB: " + e.getMessage());
        }
    }
}