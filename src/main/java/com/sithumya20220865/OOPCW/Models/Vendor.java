package com.sithumya20220865.OOPCW.Models;

import com.sithumya20220865.OOPCW.Services.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "vendors")
public class Vendor{

    @Id
    private String id; // Unique identifier for the Customer
    private String userId; // Reference to the User document
    private int totalTickets;

    public Vendor(String userId) { this.userId = userId;}

    public int getTotalTickets() {
        return totalTickets;
    }

    public String getId() {
        return id;
    }

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    public ObjectNode writeVendor(User user, String token, RepositoryService repositoryService) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        //write vendor details
        response.put("username", user.getUsername());
        response.put("role", user.getUserRole().toString());
        response.put("totalTickets", getTotalTickets());
        response.put("token", token);

        //write ticket list
        ArrayNode ticketsArray = mapper.createArrayNode();
        List<Ticket> vendorTickets = repositoryService.getTicketRepository().findByVendorId(id);
        for (Ticket ticket: vendorTickets) {
            ObjectNode ticketNode = mapper.createObjectNode();
            ticket.writeTicket(ticketNode);
            ticketsArray.add(ticketNode);
        }
        response.set("tickets", ticketsArray);

        return response;
    }
}
