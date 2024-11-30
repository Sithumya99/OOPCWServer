package com.sithumya20220865.OOPCW.Utils;

import com.sithumya20220865.OOPCW.Models.*;
import com.sithumya20220865.OOPCW.Services.*;
import com.sithumya20220865.OOPCW.Logger.*;
import com.sithumya20220865.OOPCW.Exceptions.*;
import com.sithumya20220865.OOPCW.Configs.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HandleBuyTicketUtil {

    private Message message;
    private RepositoryService repositoryService;
    private JWTService jwtService;
    private TicketPoolService ticketPoolService;

    public HandleBuyTicketUtil(Message message, RepositoryService repositoryService,
                               JWTService jwtService, TicketPoolService ticketPoolService) {
        this.message = message;
        this.repositoryService = repositoryService;
        this.jwtService = jwtService;
        this.ticketPoolService = ticketPoolService;
    }

    public ResponseEntity<?> execute() {
        GlobalLogger.logInfo("Start: Buy ticket process => ", message);
        try {
            //check access
            String role = jwtService.getRole(message.getUserAuth().getCredentials().toString());
            if (!role.equalsIgnoreCase("ROLE_Customer")) {
                GlobalLogger.logError("Unauthorized: ",
                        new UserUnauthorizedException(message.getUserAuth().getPrincipal().toString(), role));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                        .body(message.writeResMsg("Only Customers can buy tickets"));
            }

            //check configuration
            if (SessionConfiguration.getInstance() == null) {
                GlobalLogger.logWarning("Bad_Request: Session not configured.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                        .body(message.writeResMsg("Start a session to buy tickets."));
            }

            //get ticketId from request
            String id = message.getString("ticketId");
            Ticket ticket = repositoryService.getTicketRepository().findById(id)
                    .orElseThrow(() -> new RuntimeException("Ticket does not exist."));
            GlobalLogger.logInfo("Retrieve ticket: ", ticket);

            //check if ticket sold
            if (ticket.getSold()) {
                GlobalLogger.logWarning("Ticket already sold.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                        .body(message.writeResMsg("Ticket already sold."));
            }

            //remove ticket from ticket pool
            if (ticketPoolService.buyTicketToTask(ticket)) {
                String newToken = jwtService.generateToken(message.getUserAuth().getPrincipal().toString(), "Customer");
                GlobalLogger.logInfo("Buy ticket completed successfully: ", message);
                return ResponseEntity.status(HttpStatus.OK)
                        .header("Authorization", "Bearer " + newToken)
                        .body(message.writeResMsg("Ticket bought successfully"));
            } else {
                throw new FailedBuyTicketException("Failed to buy ticket.");
            }

        } catch (Exception e) {
            GlobalLogger.logError("Failed: Buy ticket process => ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                    .body(message.writeResMsg("Server error: " + e.getMessage()));
        } finally {
            GlobalLogger.logInfo("Stop: Buy ticket process => ", message);
        }
    }
}
