package com.sithumya20220865.OOPCW;

import com.mongodb.MongoClientSettings;
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

    //extract and assign connection variables
    public GlobalDatabase(@Value("${spring.data.mongodb.uri}") String connectionString,
                          @Value("${spring.data.mongodb.database}") String databaseName) {
        this.connectionString = connectionString;
        this.databaseName = databaseName;
    }

    //connect to database
    public synchronized void initialize() {
        if (this.database != null) {
            GlobalLogger.logWarning("Database already initialized.");
            return;
        }
        try {
            //connect to mongodb atlas
            MongoClient mongoClient = MongoClients.create(
                    MongoClientSettings.builder()
                            .applyConnectionString( new com.mongodb.ConnectionString(connectionString))
                            .build()
            );
            this.database = mongoClient.getDatabase(databaseName);

            GlobalLogger.logInfo("Database connected successfully", null);
        } catch (Exception e) {
            DatabaseConnectionFailedException dbExp = new DatabaseConnectionFailedException(e.getMessage());
            GlobalLogger.logError("Error occurred", dbExp);
            throw dbExp;
        }
    }

    public MongoDatabase getDatabase() {
        if (database == null) {
            throw new IllegalStateException("Cannot retrieve Global database: Database not initialized");
        }
        return database;
    }
}
