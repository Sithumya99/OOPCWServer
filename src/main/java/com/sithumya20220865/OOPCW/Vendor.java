package com.sithumya20220865.OOPCW;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    public ObjectNode writeVendor(User user, String token) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        response.put("username", user.getUsername());
        response.put("role", user.getUserRole().toString());
        response.put("totalTickets", getTotalTickets());
        response.put("token", token);
        return response;
    }
}
