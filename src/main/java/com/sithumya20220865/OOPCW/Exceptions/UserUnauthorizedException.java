package com.sithumya20220865.OOPCW.Exceptions;

public class UserUnauthorizedException extends RuntimeException{

    private String username;
    private String role;

    public UserUnauthorizedException(String username, String role) {
        this.username = username;
        this.role = role;
    }

    @Override
    public String getMessage() {
        return "User unauthorized: user => " + username + ": role => " + role;
    }
}
