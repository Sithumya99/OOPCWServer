package com.sithumya20220865.OOPCW;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HandleLoginUserUtil {

    @Autowired
    private RepositoryService repositoryService;

    private Message message;

    public HandleLoginUserUtil(Message message) {this.message = message;}

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
                        return ResponseEntity.status(HttpStatus.OK).body(customer.writeCustomer(user));
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Couldn't retrieve customer information");
                    }
                }
                case Vendor -> {
                    Vendor vendor = repositoryService.getVendorRepository().findByUserId(user.getId());
                    if (vendor != null) {
                        return ResponseEntity.status(HttpStatus.OK).body(vendor.writeVendor(user));
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Couldn't retrieve vendor information");
                    }
                }
                case Admin -> {
                    Admin admin = repositoryService.getAdminRepository().findByUserId(user.getId());
                    if (admin != null) {
                        return ResponseEntity.status(HttpStatus.OK).body(admin.writeAdmin(user));
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Couldn't retrieve admin information");
                    }
                }
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid role.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }
}
