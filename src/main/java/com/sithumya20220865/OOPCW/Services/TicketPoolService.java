package com.sithumya20220865.OOPCW.Services;

import com.sithumya20220865.OOPCW.Configs.*;
import com.sithumya20220865.OOPCW.Utils.*;
import com.sithumya20220865.OOPCW.Logger.*;
import com.sithumya20220865.OOPCW.Models.*;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class TicketPoolService {

    @Autowired
    @Qualifier("taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private TicketWebSocketHandler ticketWebSocketHandler;  //handle changes in ticket pool for websockets

    private volatile boolean isActive = false;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final ReentrantLock poolLock = new ReentrantLock();
    private long lastAddedTime = 0L;
    private Map<String, Long> lastBoughtTime = new ConcurrentHashMap<>();

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
        threadPoolTaskExecutor.submit(() -> {
            try {
                while (isActive) {
                    GlobalLogger.logInfo("Start: Add ticket to pool process => ", ticket);
                    //acquire the lock
                    poolLock.lock();

                    //check time elapsed since last ticket addition
                    long curTime = System.currentTimeMillis();
                    long timeElapsed = curTime - lastAddedTime;
                    GlobalLogger.logInfo("curtime: "+ curTime + "| lastadded: " + lastAddedTime, ticket);

                    if (timeElapsed < SessionConfiguration.getInstance().getTicketReleaseRate()) {
                        poolLock.unlock();
                        long waitTime = SessionConfiguration.getInstance().getTicketReleaseRate() - timeElapsed;
                        GlobalLogger.logInfo("Waiting for ticket release rate to elapse: ", ticket);
                        Thread.sleep(waitTime);
                        poolLock.lock();
                    }

                    //add ticket it ticket pool
                    GlobalUtil.getTicketpool().addTicket(ticket);
                    GlobalLogger.logInfo("Successfully added ticket: ", ticket);
                    ticketWebSocketHandler.sendTicketUpdate("TICKET_POOL_CHANGED");
                    lastAddedTime = System.currentTimeMillis();
                    break;
                }

                if (!isActive) {
                    GlobalLogger.logWarning("Ticket pool inactive");
                }

            } catch (Exception e) {
                Thread.currentThread().interrupt();
                GlobalLogger.logError("Failed to add ticket to pool: ", e);
            } finally {
                poolLock.unlock();
                GlobalLogger.logInfo("Stop: Add ticket to pool process => ", ticket);
            }
        });
    }

    /**
     * Handles ticket purchase using a thread pool executor.
     *
     * @param ticket the customer wants to buy.
     * @return true if the ticket was purchased successfully, false otherwise.
     */
    public boolean buyTicketToTask(Ticket ticket, String username) {
        GlobalLogger.logInfo("Start: Buy ticket to task process => ", ticket);
        try {
            if (!isActive) {
                GlobalLogger.logWarning("Ticket pool inactive");
                return false;
            }
            return threadPoolTaskExecutor.submit(() -> {
                poolLock.lock();
                try {
                    long curTime = System.currentTimeMillis();
                    long lastBougtTime = lastBoughtTime.getOrDefault(username, 0L);
                    long elapsedTime = curTime - lastBougtTime;

                    if (elapsedTime < SessionConfiguration.getInstance().getCustomerRetrievalRate()) {
                        return false;
                    }

                    lastBoughtTime.put(username, System.currentTimeMillis());
                    return removeTicket(ticket);
                } finally {
                    poolLock.unlock();
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
