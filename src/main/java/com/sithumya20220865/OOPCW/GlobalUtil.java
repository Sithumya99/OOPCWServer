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

    public static void initializeDatabase() {
        globalDatabase.initialize();
    }
}
