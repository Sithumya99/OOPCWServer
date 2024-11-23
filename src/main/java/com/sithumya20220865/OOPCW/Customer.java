package com.sithumya20220865.OOPCW;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "customers")
public class Customer{
    @Id
    private String id; // Unique identifier for the Customer
    private String userId; // Reference to the User document
    private int noOfTicketsBought;

    public Customer(String userId) {
        this.userId = userId;
    }

    public int getNoOfTicketsBought() {
        return noOfTicketsBought;
    }

    public void setNoOfTicketsBought(int noOfTicketsBought) {
        this.noOfTicketsBought = noOfTicketsBought;
    }

    public ObjectNode writeCustomer(User user, String token) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        response.put("username", user.getUsername());
        response.put("role", user.getUserRole().toString());
        response.put("noOfTicketsBought", getNoOfTicketsBought());
        response.put("token", token);
        return response;
    }
}
