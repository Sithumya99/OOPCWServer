package com.sithumya20220865.OOPCW.Utils;

import com.sithumya20220865.OOPCW.Models.*;
import com.sithumya20220865.OOPCW.Services.*;
import com.sithumya20220865.OOPCW.Logger.*;
import com.sithumya20220865.OOPCW.Exceptions.*;
import com.sithumya20220865.OOPCW.Configs.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HandleStartSessionUtil {

    private Message message;
    private JWTService jwtService;

    private TicketWebSocketHandler ticketWebSocketHandler;
    private TicketPoolService ticketPoolService;

    public HandleStartSessionUtil(Message message, JWTService jwtService,
                                  TicketWebSocketHandler ticketWebSocketHandler, TicketPoolService ticketPoolService) {
        this.message = message;
        this.jwtService = jwtService;
        this.ticketWebSocketHandler = ticketWebSocketHandler;
        this.ticketPoolService = ticketPoolService;
    }

    public ResponseEntity<?> execute() {
        GlobalLogger.logInfo("Start: Start session process => ", message);
        try {
            //authorize use
            String role = jwtService.getRole(message.getUserAuth().getCredentials().toString());
            if (!role.equalsIgnoreCase("ROLE_Admin")) {
                GlobalLogger.logError("Unauthorized: ",
                        new UserUnauthorizedException(message.getUserAuth().getPrincipal().toString(), role));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                        .body(message.writeResMsg("Only Admin can start a session"));
            }

            //get configuration properties
            int totalTickets = message.getInt("totalTickets");
            int ticketReleaseRate = message.getInt("ticketReleaseRate");
            int customerRetrievalRate = message.getInt("customerRetrievalRate");
            int maxTicketCapacity = message.getInt("maxTicketCapacity");

            //initialize global configuration
            SessionConfiguration.initialize(totalTickets, ticketReleaseRate, customerRetrievalRate, maxTicketCapacity);
            GlobalLogger.logInfo("Configuration initialized: ", message);

            //initialize global ticket pool
            GlobalUtil.setTicketpool(new Ticketpool(maxTicketCapacity));
            ticketPoolService.startSession();
            GlobalLogger.logInfo("Ticket pool initialized: ", message);

            //add unsold tickets
            ticketPoolService.addUnsoldTickets();

            //notify clients ticket pool initiated
            ticketWebSocketHandler.sendTicketUpdate("TICKET_POOL_START");

            String newToken = jwtService.generateToken(message.getUserAuth().getPrincipal().toString(), "Admin");

            GlobalLogger.logInfo("Start session completed successfully", message);
            return ResponseEntity.status(HttpStatus.OK)
                    .header("Authorization", "Bearer " + newToken)
                    .body(message.writeResMsg("Session started successfully"));

        } catch (Exception e) {
            GlobalLogger.logError("Failed start session: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                    .body(message.writeResMsg("Server error: " + e.getMessage()));
        } finally {
            GlobalLogger.logInfo("Stop: Start session process => ", message);
        }
    }
}
