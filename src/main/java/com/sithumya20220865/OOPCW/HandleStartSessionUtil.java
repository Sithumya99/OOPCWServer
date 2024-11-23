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
        try {
            System.out.println("start session: start");
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

            System.out.println("token in msg: " + message.getUserAuth().getCredentials());
            System.out.println("token exp: " + jwtService.validateToken(message.getUserAuth().getCredentials().toString()));

            String newToken = jwtService.generateToken(message.getUserAuth().getPrincipal().toString(), "Admin");
            System.out.println("new token: " + newToken);

            return ResponseEntity.status(HttpStatus.OK)
                    .header("Authorization", "Bearer " + newToken)
                    .body("Session started successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }
}
