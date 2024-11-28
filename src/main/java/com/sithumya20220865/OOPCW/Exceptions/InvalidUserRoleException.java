package com.sithumya20220865.OOPCW.Exceptions;

public class InvalidUserRoleException extends RuntimeException{

    private String role;

    public InvalidUserRoleException(String role) {
        this.role = role;
    }

    @Override
    public String getMessage() {
        return "Invalid user role: " + role;
    }
}
