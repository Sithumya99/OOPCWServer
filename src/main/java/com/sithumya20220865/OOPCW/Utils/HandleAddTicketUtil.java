package com.sithumya20220865.OOPCW.Utils;

import com.sithumya20220865.OOPCW.Models.*;
import com.sithumya20220865.OOPCW.Services.*;
import com.sithumya20220865.OOPCW.Logger.*;
import com.sithumya20220865.OOPCW.Exceptions.*;
import com.sithumya20220865.OOPCW.Configs.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HandleAddTicketUtil {

    private Message message;
    private RepositoryService repositoryService;
    private JWTService jwtService;
    private TicketPoolService ticketPoolService;

    public HandleAddTicketUtil(Message message, RepositoryService repositoryService,
                               JWTService jwtService, TicketPoolService ticketPoolService) {
        this.message = message;
        this.repositoryService = repositoryService;
        this.jwtService = jwtService;
        this.ticketPoolService = ticketPoolService;
    }

    public ResponseEntity<?> execute() {
        GlobalLogger.logInfo("Start: Add Ticket process => ", message);
        try {
            //check access
            String role = jwtService.getRole(message.getUserAuth().getCredentials().toString());
            if (!role.equalsIgnoreCase("ROLE_Vendor")) {
                GlobalLogger.logError("Unauthorized: ",
                        new UserUnauthorizedException(message.getUserAuth().getPrincipal().toString(), role));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                        .body(message.writeResMsg("Only Vendor can add tickets"));
            }

            //check configuration
            if (SessionConfiguration.getInstance() == null) {
                GlobalLogger.logWarning("Bad_Request: Session not configured.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                        .body(message.writeResMsg("Start a session to add tickets."));
            }

            //get ticket properties from request
            String eventName = message.getString("eventName");
            double price = message.getDouble("price");

            //get vendor id
            User user = repositoryService.getUserRepository().findByUsername(message.getUserAuth().getPrincipal().toString());
            Vendor vendor = repositoryService.getVendorRepository().findByUserId(user.getId());
            String vendorId = vendor.getId();
            vendor.setTotalTickets(vendor.getTotalTickets() + 1);

            //create new ticket
            Ticket newTicket = new Ticket(eventName, price, vendorId);
            GlobalLogger.logInfo("Create new ticket: ", newTicket);

            //save ticket
            repositoryService.getTicketRepository().save(newTicket);

            //set adding ticket to ticket pool
            ticketPoolService.addTicketsToTask(newTicket);

            //return success response with new token
            String newToken = jwtService.generateToken(message.getUserAuth().getPrincipal().toString(), "Vendor");
            GlobalLogger.logInfo("Add ticket completed successfully: ", message);
            return ResponseEntity.status(HttpStatus.OK)
                    .header("Authorization", "Bearer " + newToken)
                    .body(message.writeResMsg("Ticket added successfully"));

        } catch (Exception e) {
            GlobalLogger.logError("Failed to add ticket: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                    .body(message.writeResMsg("Failed to add ticket."));
        } finally {
            GlobalLogger.logInfo("Stop: Add ticket process => ", message);
        }
    }
}
