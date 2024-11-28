package com.sithumya20220865.OOPCW.Exceptions;

public class TicketPoolAlreadyInitializedException extends RuntimeException {

    public TicketPoolAlreadyInitializedException() {}

    @Override
    public String getMessage() {
        return "Ticket pool already initialized";
    }
}
