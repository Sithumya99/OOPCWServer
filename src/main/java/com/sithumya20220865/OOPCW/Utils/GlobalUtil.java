package com.sithumya20220865.OOPCW.Utils;

import com.sithumya20220865.OOPCW.Models.*;
import com.sithumya20220865.OOPCW.Logger.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
            GlobalLogger.logInfo("Server started successfully", null);
        } catch (Exception e) {
            GlobalLogger.logError("Server failed to start", e);
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
