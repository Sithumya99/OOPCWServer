package com.sithumya20220865.OOPCW;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "admins")
public class Admin {

    @Id
    private String id; // Unique identifier for the Customer
    private String userId; // Reference to the User document

    public Admin(String userId) {this.userId = userId;}

    public ObjectNode writeAdmin(User user, String token) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        response.put("username", user.getUsername());
        response.put("role", user.getUserRole().toString());
        response.put("token", token);
        return response;
    }
}
