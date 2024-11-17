package com.sithumya20220865.OOPCW;

enum Role {
    Customer,
    Vendor,
    Admin
}
public class User {

    protected String username;
    protected String password;
    protected Role userRole;
}
