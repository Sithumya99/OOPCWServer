package com.sithumya20220865.OOPCW.Utils;

import com.sithumya20220865.OOPCW.Models.*;
import com.sithumya20220865.OOPCW.Services.*;
import com.sithumya20220865.OOPCW.Logger.*;
import com.sithumya20220865.OOPCW.Exceptions.*;
import com.sithumya20220865.OOPCW.Configs.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HandleStopSessionUtil {

    private Message message;
    private JWTService jwtService;
    private TicketWebSocketHandler ticketWebSocketHandler;
    private TicketPoolService ticketPoolService;

    public HandleStopSessionUtil(Message message, JWTService jwtService,
                                 TicketWebSocketHandler ticketWebSocketHandler, TicketPoolService ticketPoolService) {
        this.message = message;
        this.jwtService = jwtService;
        this.ticketWebSocketHandler = ticketWebSocketHandler;
        this.ticketPoolService = ticketPoolService;
    }

    public ResponseEntity<?> execute() {
        GlobalLogger.logInfo("Start: Stop session process => ", message);

        //authorize user
        String role = jwtService.getRole(message.getUserAuth().getCredentials().toString());
        if (!role.equalsIgnoreCase("ROLE_Admin")) {
            GlobalLogger.logError("Unauthorized: ",
                    new UserUnauthorizedException(message.getUserAuth().getPrincipal().toString(), role));
            GlobalLogger.logInfo("Stop: Stop session process => ", message);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                    .body("Only Admin can start a session");
        }

        //terminate session
        if (SessionConfiguration.getInstance() == null) {
            GlobalLogger.logWarning("No session to terminate.");
            GlobalLogger.logInfo("Stop: Stop session process => ", message);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                    .body("No session to terminate");
        } else {
            //terminate session
            SessionConfiguration.reset();
            ticketPoolService.stopSession();
            GlobalUtil.getTicketpool().getTickets().clear();
            GlobalUtil.setTicketpool(null);

            //notify clients ticketpool stopped
            ticketWebSocketHandler.sendTicketUpdate("TICKET_POOL_STOP");

            GlobalLogger.logInfo("Session terminated successfully", message);
            GlobalLogger.logInfo("Stop: Stop session process => ", message);
            String newToken = jwtService.generateToken(message.getUserAuth().getPrincipal().toString(), "Admin");
            return ResponseEntity.status(HttpStatus.OK)
                    .header("Authorization", "Bearer " + newToken)
                    .body("Session terminated successfully");
        }
    }
}
