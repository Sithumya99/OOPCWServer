package com.sithumya20220865.OOPCW.Exceptions;

public class FailedBuyTicketException extends RuntimeException{

    private String message;

    public FailedBuyTicketException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
