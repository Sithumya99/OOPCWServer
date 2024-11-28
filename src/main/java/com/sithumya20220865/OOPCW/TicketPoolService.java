package com.sithumya20220865.OOPCW;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
    private TicketWebSocketHandler ticketWebSocketHandler;  //handle changes in ticket pool for websockets

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
                        GlobalLogger.logInfo("Start: Add ticket to pool process => ", ticket);

                        //add ticket it ticket pool
                        GlobalUtil.getTicketpool().addTicket(ticket);
                        ticketWebSocketHandler.sendTicketUpdate("TICKET_POOL_CHANGED");
                        break;
                    }

                    if (!isActive) {
                        GlobalLogger.logWarning("Ticket pool inactive");
                    }

                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    GlobalLogger.logError("Failed to add ticket to pool: ", e);
                } finally {
                    GlobalLogger.logInfo("Stop: Add ticket to pool process => ", ticket);
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
        GlobalLogger.logInfo("Start: Buy ticket to task process => ", ticket);
        try {
            return threadPoolTaskExecutor.submit(() -> {
                synchronized (lock) {
                    if (!isActive) {
                        GlobalLogger.logWarning("Ticket pool inactive");
                        return false;
                    } else {
                        return removeTicket(ticket);
                    }
                }
            }).get();
        } catch (Exception e) {
            GlobalLogger.logError("Failed buy ticket: ", e);
            return false;
        } finally {
            GlobalLogger.logInfo("Stop: Buy ticket to task process =>", ticket);
        }
    }

    private boolean removeTicket(Ticket ticket) {
        GlobalLogger.logInfo("Start: Purchase ticket process => ", ticket);
        try {
            if (GlobalUtil.getTicketpool().removeTicket(ticket)) {
                //set ticket to sold
                ticket.setSold(true);
                repositoryService.getTicketRepository().save(ticket);

                GlobalLogger.logInfo("Ticket purchased successfully: ", ticket);
                ticketWebSocketHandler.sendTicketUpdate("TICKET_POOL_CHANGED");
                return true;
            }
            throw new RuntimeException("Failed to purchase ticket");
        } catch (Exception e) {
            GlobalLogger.logError("Failed to purchase ticket: ", e);
            return false;
        } finally {
            GlobalLogger.logInfo("Stop: Purchase ticket process => ", ticket);
        }
    }

    public void writeTicketPool(ArrayList<Ticket> ticketsList) {
        for (String ticketId: GlobalUtil.getTicketpool().getTickets()) {
            Ticket ticket = repositoryService.getTicketRepository().findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket does not exist."));
            ticketsList.add(ticket);
        }
    }

    public void addUnsoldTickets() {
        List<Ticket> ticketList = repositoryService.getTicketRepository().findByIsSold(false);
        for (Ticket ticket: ticketList) {
            addTicketsToTask(ticket);
        }
    }

    @PreDestroy
    public void shutdownExecutor() {
        threadPoolTaskExecutor.shutdown();
    }
}
