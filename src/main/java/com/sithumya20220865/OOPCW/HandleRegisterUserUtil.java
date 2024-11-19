package com.sithumya20220865.OOPCW;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HandleRegisterUserUtil {

    @Autowired
    private RepositoryService repositoryService;

    private Message message;

    public HandleRegisterUserUtil(Message message) { this.message = message;}

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
                    return ResponseEntity.ok(newCustomer.writeCustomer(newUser));
                }
                case Vendor -> {
                    Vendor newVendor = new Vendor(newUser.getId());
                    newVendor.setTotalTickets(0);
                    repositoryService.getVendorRepository().save(newVendor);
                    return ResponseEntity.ok(newVendor.writeVendor(newUser));
                }
                case Admin -> {
                    Admin newAdmin = new Admin(newUser.getId());
                    repositoryService.getAdminRepository().save(newAdmin);
                    return ResponseEntity.ok(newAdmin.writeAdmin(newUser));
                }
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user role.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }
}
