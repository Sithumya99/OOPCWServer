package com.sithumya20220865.OOPCW;

import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GlobalUtil {

    private static GlobalDatabase globalDatabase;

    @Autowired
    public GlobalUtil(GlobalDatabase globalDatabase) {
        GlobalUtil.globalDatabase = globalDatabase;
    }

    public static void serverSetupJob() {
        try {
            // Initialize the database connection
            GlobalUtil.initializeDatabase();
            System.out.println("Server Started Successfully.");
        } catch (RuntimeException e) {
            System.err.println("Server failed to start: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void initializeDatabase() {
        globalDatabase.initialize();
    }
}