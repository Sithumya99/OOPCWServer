package com.sithumya20220865.OOPCW;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GlobalDatabase {

    private final String connectionString;
    private final String databaseName;
    private MongoDatabase database;

    public GlobalDatabase(@Value("${spring.data.mongodb.uri}") String connectionString,
                          @Value("${spring.data.mongodb.database}") String databaseName) {
        this.connectionString = connectionString;
        this.databaseName = databaseName;
    }

    public synchronized void initialize() {
        if (this.database != null) {
            System.out.println("Database already initialized");
            return;
        }
        try {
            MongoClient mongoClient = MongoClients.create(
                    MongoClientSettings.builder()
                            .applyConnectionString( new com.mongodb.ConnectionString(connectionString))
                            .build()
            );
            this.database = mongoClient.getDatabase(databaseName);
            System.out.println("Database connection success.");
        } catch (Exception e) {
            System.out.println("Database connection failed.");
            throw new RuntimeException("Database Initialization failed", e);
        }
    }

    public MongoDatabase getDatabase() {
        if (database == null) {
            throw new IllegalStateException("Database not initialized");
        }
        return database;
    }
}
