package com.sithumya20220865.OOPCW;

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
        System.out.println("Add ticket: start");
        try {
            //check access
            if (!jwtService.getRole(message.getUserAuth().getCredentials().toString()).equalsIgnoreCase("ROLE_Vendor")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                        .body("Only Vendor can add tickets");
            }

            //check configuration
            if (SessionConfiguration.getInstance() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                        .body("Start a session to add tickets.");
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
            System.out.println("Created new ticket: " + newTicket);

            System.out.println("Create new ticket: " + newTicket);
            //save ticket
            repositoryService.getTicketRepository().save(newTicket);

            //set adding ticket to ticket pool
            ticketPoolService.addTicketsToTask(newTicket);

            //return success response with new token
            String newToken = jwtService.generateToken(message.getUserAuth().getPrincipal().toString(), "Vendor");
            return ResponseEntity.status(HttpStatus.OK)
                    .header("Authorization", "Bearer " + newToken)
                    .body("Ticket added successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Authorization", "Bearer " + message.getUserAuth().getCredentials())
                    .body("Failed to add ticket.");
        }
    }
}
