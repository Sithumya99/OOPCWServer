package com.sithumya20220865.OOPCW;

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

}
