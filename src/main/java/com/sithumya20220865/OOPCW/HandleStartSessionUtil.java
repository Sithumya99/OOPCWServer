package com.sithumya20220865.OOPCW;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HandleStartSessionUtil {

    private Message message;
    private JWTService jwtService;

    public HandleStartSessionUtil(Message message, JWTService jwtService) {
        this.message = message;
        this.jwtService = jwtService;
    }

    public ResponseEntity<?> execute() {
        System.out.println("start session process: start");
        try {

            if (!jwtService.getRole(message.getUserAuth().getCredentials().toString()).equalsIgnoreCase("ROLE_Admin")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only Admin can start a session");
            }

            //get configuration properties
            int totalTickets = message.getInt("totalTickets");
            int ticketReleaseRate = message.getInt("ticketReleaseRate");
            int customerRetrievalRate = message.getInt("customerRetrievalRate");
            int maxTicketCapacity = message.getInt("maxTicketCapacity");

            System.out.println("start config init");
            //initialize global configuration
            SessionConfiguration.initialize(totalTickets, ticketReleaseRate, customerRetrievalRate, maxTicketCapacity);
            System.out.println("finish config init");

            System.out.println("start ticketpool init");
            //initialize global ticket pool
            GlobalUtil.setTicketpool(new Ticketpool(maxTicketCapacity));
            System.out.println("ticket pool: " + GlobalUtil.getTicketpool());

            String newToken = jwtService.generateToken(message.getUserAuth().getPrincipal().toString(), "Admin");
            System.out.println("new token: " + newToken);

            System.out.println("start session process: end");
            return ResponseEntity.status(HttpStatus.OK)
                    .header("Authorization", "Bearer " + newToken)
                    .body("Session started successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }
}
