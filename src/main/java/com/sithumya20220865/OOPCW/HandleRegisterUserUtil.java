package com.sithumya20220865.OOPCW;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HandleRegisterUserUtil {

    private RepositoryService repositoryService;  //access to all repositories

    private JWTService jwtService;

    private Message message;

    public HandleRegisterUserUtil(Message message, RepositoryService repositoryService, JWTService jwtService) {
        this.message = message;
        this.repositoryService = repositoryService;
        this.jwtService = jwtService;
    }

    //method for registering new users
    public ResponseEntity<?> execute() {
        User newUser = new User();

        try {
            //get user values into object
            newUser.parseRequest(message);

            //check if username already exists
            if (repositoryService.getUserRepository().findByUsername(newUser.getUsername()) != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
            }

            //save new user
            repositoryService.getUserRepository().save(newUser);

            //create role relevant document
            switch (newUser.userRole) {
                case Customer -> {
                    Customer newCustomer = new Customer(newUser.getId());
                    newCustomer.setNoOfTicketsBought(0);
                    repositoryService.getCustomerRepository().save(newCustomer);
                    //generate JWT token
                    String token = jwtService.generateToken(newUser.getUsername(), "Customer");
                    return ResponseEntity.ok(newCustomer.writeCustomer(newUser, token));
                }
                case Vendor -> {
                    Vendor newVendor = new Vendor(newUser.getId());
                    newVendor.setTotalTickets(0);
                    repositoryService.getVendorRepository().save(newVendor);
                    //generate JWT token
                    String token = jwtService.generateToken(newUser.getUsername(), "Vendor");
                    return ResponseEntity.ok(newVendor.writeVendor(newUser, token, repositoryService));
                }
                case Admin -> {
                    Admin newAdmin = new Admin(newUser.getId());
                    repositoryService.getAdminRepository().save(newAdmin);
                    //generate JWT token
                    String token = jwtService.generateToken(newUser.getUsername(), "Admin");
                    return ResponseEntity.ok(newAdmin.writeAdmin(newUser, token));
                }
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user role.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }
}
