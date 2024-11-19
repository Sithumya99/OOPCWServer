package com.sithumya20220865.OOPCW;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HandleStartSessionUtil {

    private Message message;

    public HandleStartSessionUtil(Message message) {this.message = message;}

    public ResponseEntity<?> execute() {
        try {
            //authenticate user
            //TODO: using tokens for authentication

            //get configuration properties
            int totalTickets = message.getInt("totalTickets");
            int ticketReleaseRate = message.getInt("ticketReleaseRate");
            int customerRetrievalRate = message.getInt("customerRetrievalRate");
            int maxTicketCapacity = message.getInt("maxTicketCapacity");

            //initialize global configuration
            SessionConfiguration.initialize(totalTickets, ticketReleaseRate, customerRetrievalRate, maxTicketCapacity);

            //initialize global ticket pool
            GlobalUtil.setTicketpool(new Ticketpool(maxTicketCapacity));

            return ResponseEntity.status(HttpStatus.OK).body("Session started successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }
}
