package com.sithumya20220865.OOPCW;

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

        if (!jwtService.getRole(message.getUserAuth().getCredentials().toString()).equalsIgnoreCase("ROLE_Admin")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only Admin can start a session");
        }

        //terminate session
        if (SessionConfiguration.getInstance() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No session to terminate");
        } else {
            //terminate session
            SessionConfiguration.reset();
            ticketPoolService.stopSession();
            GlobalUtil.getTicketpool().getTickets().clear();
            GlobalUtil.setTicketpool(null);

            //notify clients ticketpool stopped
            ticketWebSocketHandler.sendTicketUpdate("TICKET_POOL_STOP");

            return ResponseEntity.status(HttpStatus.OK).body("Session terminated successfully");
        }
    }
}
