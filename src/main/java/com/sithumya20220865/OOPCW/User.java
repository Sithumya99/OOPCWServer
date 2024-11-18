package com.sithumya20220865.OOPCW;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

enum Role {
    Customer,
    Vendor,
    Admin
}

@Document(collection = "users")
public class User {

    @Id
    private String id;
    protected String username;
    protected String password;
    protected Role userRole;

    public User() {}

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getUserRole() {
        return userRole;
    }

    public void setUserRole(Role userRole) {
        this.userRole = userRole;
    }

    public void parseRequest(Message message) throws Exception {
        try {
            this.username = message.getString("username");
            this.password = message.getString("password");
            String role = message.getString("role");
            this.userRole = Role.valueOf(role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase());
        } catch (Exception e) {
            throw e;
        }
    }
}
