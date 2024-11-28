package com.sithumya20220865.OOPCW.Configs;

import com.sithumya20220865.OOPCW.Exceptions.*;

public class SessionConfiguration {

    private static SessionConfiguration instance;  //singleton instance
    private int totalTickets;
    private int ticketReleaseRate;
    private int customerRetrievalRate;
    private int maxTicketCapacity;

    //private to ensure singleton pattern
    private SessionConfiguration(int totalTickets, int ticketReleaseRate,
                                int customerRetrievalRate, int maxTicketCapacity) {
        this.totalTickets = totalTickets;
        this.ticketReleaseRate = ticketReleaseRate;
        this.customerRetrievalRate = customerRetrievalRate;
        this.maxTicketCapacity = maxTicketCapacity;
    }

    //initialize configuration
    public static synchronized SessionConfiguration initialize(int totalTickets, int ticketReleaseRate,
                                                                                int customerRetrievalRate, int maxTicketCapacity) throws SessionAlreadyInitializedException {
        if (instance == null) {
            instance = new SessionConfiguration(totalTickets, ticketReleaseRate, customerRetrievalRate, maxTicketCapacity);
        } else {
            throw new SessionAlreadyInitializedException();
        }
        return instance;
    }

    public static SessionConfiguration getInstance() {
        return instance;
    }

    public static synchronized void reset() {
        instance = null;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public int getCustomerRetrievalRate() {
        return customerRetrievalRate;
    }

    public int getTicketReleaseRate() {
        return ticketReleaseRate;
    }

    public int getMaxTicketCapacity() {
        return maxTicketCapacity;
    }
}
