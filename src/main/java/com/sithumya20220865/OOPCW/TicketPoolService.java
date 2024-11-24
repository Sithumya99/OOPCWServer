package com.sithumya20220865.OOPCW;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TicketPoolService {

    @Autowired
    @Qualifier("taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private TicketWebSocketHandler ticketWebSocketHandler;  //handle changes in ticketpool for websockets

    private volatile boolean isActive = true;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final Object lock = new Object();

    public void stopSession() {
        isActive = false;
    }

    public void startSession() {
        isActive = true;
    }

    /**
     * Handles adding tickets using a thread pool executor.
     *
     * @param ticket new ticket to add to ticket pool.
     */
    public void addTicketsToTask(Ticket ticket) {
        scheduler.scheduleAtFixedRate(() -> {
            threadPoolTaskExecutor.submit(() -> {
                try {
                    while (isActive) {
                        //add ticket it ticket pool
                        GlobalUtil.getTicketpool().addTicket(ticket);
                        ticketWebSocketHandler.sendTicketUpdate("TICKET_POOL_CHANGED");
                        break;
                    }

                    if (!isActive) {
                        System.out.println("Ticket pool is inactive");
                    }

                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                    System.out.println("Failed to add ticket: " + e.getMessage());
                }
            });
        }, 0, SessionConfiguration.getInstance().getTicketReleaseRate(), TimeUnit.MILLISECONDS);
    }

    /**
     * Handles ticket purchase using a thread pool executor.
     *
     * @param ticket the customer wants to buy.
     * @return true if the ticket was purchased successfully, false otherwise.
     */
    public boolean buyTicketToTask(Ticket ticket) {
        try {
            return threadPoolTaskExecutor.submit(() -> {
                synchronized (lock) {
                    if (!isActive) {
                        System.out.println("Ticket pool is inactive");
                        return false;
                    } else {
                        return removeTicket(ticket);
                    }
                }
            }).get();
        } catch (Exception e) {
            System.out.println("Failed process: buyTicketToTask");
            e.printStackTrace();
            return false;
        }
    }

    private boolean removeTicket(Ticket ticket) {
        try {
            if (ticket == null) {
                System.out.println("Ticket: " + ticket.getId() + " not found");
                return false;
            }
            if (GlobalUtil.getTicketpool().removeTicket(ticket)) {
                //set ticket to sold
                ticket.setSold(true);
                repositoryService.getTicketRepository().save(ticket);

                System.out.println("Ticket: " + ticket.getId() + " purchased successfully");
                ticketWebSocketHandler.sendTicketUpdate("TICKET_POOL_CHANGED");
                return true;
            } else {
                System.out.println("Failed to purchase ticket: " + ticket.getId());
                return false;
            }
        } catch (Exception e) {
            System.out.println("Failed to buy ticket: " + ticket.getId());
            e.printStackTrace();
            return false;
        }
    }

    @PreDestroy
    public void shutdownExecutor() {
        threadPoolTaskExecutor.shutdown();
    }
}
