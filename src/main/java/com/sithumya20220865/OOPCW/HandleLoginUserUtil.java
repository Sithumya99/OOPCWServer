package com.sithumya20220865.OOPCW;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HandleLoginUserUtil {

    private RepositoryService repositoryService;  //access to all repositories

    private JWTService jwtService;  //for token generation

    private Message message;

    public HandleLoginUserUtil(Message message, RepositoryService repositoryService, JWTService jwtService) {
        this.message = message;
        this.repositoryService = repositoryService;
        this.jwtService = jwtService;
    }

    //method for authenticating users
    public ResponseEntity<?> execute() {
        try {
            String username = message.getString("username");
            String password = message.getString("password");

            // Authenticate the user
            User user = repositoryService.getUserRepository().findByUsername(username);
            if (user == null || !user.getPassword().equals(password)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }

            //retrieve document relevant to role
            switch (user.userRole) {
                case Customer -> {
                    Customer customer = repositoryService.getCustomerRepository().findByUserId(user.getId());
                    if (customer != null) {
                        //generate JWT token
                        String token = jwtService.generateToken(username, "Customer");
                        return ResponseEntity.status(HttpStatus.OK).body(customer.writeCustomer(user, token));
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Couldn't retrieve customer information");
                    }
                }
                case Vendor -> {
                    Vendor vendor = repositoryService.getVendorRepository().findByUserId(user.getId());
                    if (vendor != null) {
                        //generate JWT token
                        String token = jwtService.generateToken(username, "Vendor");
                        return ResponseEntity.status(HttpStatus.OK).body(vendor.writeVendor(user, token, repositoryService));
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Couldn't retrieve vendor information");
                    }
                }
                case Admin -> {
                    Admin admin = repositoryService.getAdminRepository().findByUserId(user.getId());
                    if (admin != null) {
                        //generate JWT token
                        String token = jwtService.generateToken(username, "Admin");
                        return ResponseEntity.status(HttpStatus.OK).body(admin.writeAdmin(user, token));
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Couldn't retrieve admin information");
                    }
                }
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid role.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }
}
