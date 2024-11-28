package com.sithumya20220865.OOPCW.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {

    @Id
    private String id;
    protected String username;
    protected String password;
    protected String userRole;

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

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public void parseRequest(Message message) throws Exception {
        try {
            this.username = message.getString("username");
            this.password = message.getString("password");
            this.userRole = message.getString("role");
        } catch (Exception e) {
            throw e;
        }
    }
}
