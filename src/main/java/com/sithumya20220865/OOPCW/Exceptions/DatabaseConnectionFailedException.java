package com.sithumya20220865.OOPCW.Exceptions;

public class DatabaseConnectionFailedException extends RuntimeException {
    String message;

    public DatabaseConnectionFailedException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return "Database connection failed: " + message;
    }
}
