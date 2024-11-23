package com.sithumya20220865.OOPCW;

import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class GlobalUtil {

    private static GlobalDatabase globalDatabase;
    private static Ticketpool ticketpool;  //global ticket pool

    @Autowired
    public GlobalUtil(GlobalDatabase globalDatabase) {
        GlobalUtil.globalDatabase = globalDatabase;
    }

    //start server
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

    public static Ticketpool getTicketpool() {
        return ticketpool;
    }

    public static void setTicketpool(Ticketpool ticketpool) {
        GlobalUtil.ticketpool = ticketpool;
    }
}
