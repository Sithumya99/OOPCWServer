package com.sithumya20220865.OOPCW;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalLogger {
    private static final Logger logger = LoggerFactory.getLogger("GlobalLogger");

    public static void logInfo(String message, Object item) {
        logger.info(message, item);
    }

    public static void logWarning(String message) {
        logger.warn(message);
    }

    public static void logError(String message, Exception e) {
        logger.error(message, e);
    }
}
