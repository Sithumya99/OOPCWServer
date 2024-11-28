package com.sithumya20220865.OOPCW.Utils;

import com.sithumya20220865.OOPCW.Models.*;
import com.sithumya20220865.OOPCW.Services.*;
import com.sithumya20220865.OOPCW.Logger.*;
import com.sithumya20220865.OOPCW.Exceptions.*;

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
        GlobalLogger.logInfo("Start: Register new user process => ", message);
        User newUser = new User();

        try {
            //get user values into object
            newUser.parseRequest(message);

            //check if username already exists
            if (repositoryService.getUserRepository().findByUsername(newUser.getUsername()) != null) {
                GlobalLogger.logWarning("Username already exists " + newUser.getUsername());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
            }

            //save new user
            repositoryService.getUserRepository().save(newUser);
            GlobalLogger.logInfo("New user registered successfully: ", newUser);

            //create role relevant document
            switch (newUser.getUserRole()) {
                case "Customer" -> {
                    Customer newCustomer = new Customer(newUser.getId());
                    newCustomer.setNoOfTicketsBought(0);
                    repositoryService.getCustomerRepository().save(newCustomer);
                    //generate JWT token
                    String token = jwtService.generateToken(newUser.getUsername(), "Customer");
                    GlobalLogger.logInfo("Customer registered successfully: ", newCustomer);
                    return ResponseEntity.ok(newCustomer.writeCustomer(newUser, token));
                }
                case "Vendor" -> {
                    Vendor newVendor = new Vendor(newUser.getId());
                    newVendor.setTotalTickets(0);
                    repositoryService.getVendorRepository().save(newVendor);
                    //generate JWT token
                    String token = jwtService.generateToken(newUser.getUsername(), "Vendor");
                    GlobalLogger.logInfo("Vendor registered successfully: ", newVendor);
                    return ResponseEntity.ok(newVendor.writeVendor(newUser, token, repositoryService));
                }
                case "Admin" -> {
                    Admin newAdmin = new Admin(newUser.getId());
                    repositoryService.getAdminRepository().save(newAdmin);
                    //generate JWT token
                    String token = jwtService.generateToken(newUser.getUsername(), "Admin");
                    GlobalLogger.logInfo("Admin registered successfully: ", newAdmin);
                    return ResponseEntity.ok(newAdmin.writeAdmin(newUser, token));
                }
            }
            throw new InvalidUserRoleException(newUser.getUserRole());

        } catch (Exception e) {
            GlobalLogger.logError("Failed to register user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        } finally {
            GlobalLogger.logInfo("Stop: Register new user process => ", message);
        }
    }
}
