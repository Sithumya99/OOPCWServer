package com.sithumya20220865.OOPCW;

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
        System.out.println("Buy ticket process: start");
        try {
            //check access
            if (!jwtService.getRole(message.getUserAuth().getCredentials().toString()).equalsIgnoreCase("ROLE_Customer")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                        .body("Only Customers can buy tickets");
            }

            //check configuration
            if (SessionConfiguration.getInstance() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                        .body("Start a session to add tickets.");
            }

            //get ticket Id from request
            String id = message.getString("ticketId");
            Ticket ticket = repositoryService.getTicketRepository().findById(id)
                    .orElseThrow(() -> new RuntimeException("Ticket does not exist."));

            //check if ticket sold
            if (ticket.getSold()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                        .body("Ticket already sold.");
            }

            //remove ticket from ticket pool
            if (ticketPoolService.buyTicketToTask(ticket)) {
                String newToken = jwtService.generateToken(message.getUserAuth().getPrincipal().toString(), "Customer");
                return ResponseEntity.status(HttpStatus.OK)
                        .header("Authorization", "Bearer " + newToken)
                        .body("Ticket bought successfully");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                        .body("Failed to buy ticket.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                    .body("Failed to buy ticket.");
        }
    }
}
