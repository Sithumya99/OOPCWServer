package com.sithumya20220865.OOPCW.Models;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class Ticketpool {

    private final BlockingDeque<String> tickets;
    private final int maxCapacity;

    private static Ticketpool instance;

    public Ticketpool(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        this.tickets = new LinkedBlockingDeque<>(maxCapacity);
    }

    public boolean addTicket(Ticket ticket) {
        try {
            tickets.put(ticket.getId());
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    public boolean removeTicket(Ticket ticket) {
        return tickets.remove(ticket.getId());
    }

    public BlockingDeque<String> getTickets() {
        return tickets;
    }
}
