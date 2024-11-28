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
        GlobalLogger.logInfo("Start: Login user process => ", message);
        try {
            String username = message.getString("username");
            String password = message.getString("password");

            // Authenticate the user
            User user = repositoryService.getUserRepository().findByUsername(username);
            if (user == null || !user.getPassword().equals(password)) {
                GlobalLogger.logError("Unauthorized: Invalid credentials => ",
                        new UserUnauthorizedException(username, "no role"));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
            GlobalLogger.logInfo("User authorized successfully: ", message);

            //retrieve document relevant to role
            switch (user.userRole) {
                case "Customer" -> {
                    Customer customer = repositoryService.getCustomerRepository().findByUserId(user.getId());
                    if (customer != null) {
                        GlobalLogger.logInfo("Customer document found: ", customer);
                        //generate JWT token
                        String token = jwtService.generateToken(username, "Customer");
                        return ResponseEntity.status(HttpStatus.OK).body(customer.writeCustomer(user, token));
                    }
                    break;
                }
                case "Vendor" -> {
                    Vendor vendor = repositoryService.getVendorRepository().findByUserId(user.getId());
                    if (vendor != null) {
                        GlobalLogger.logInfo("Vendor document found: ", vendor);
                        //generate JWT token
                        String token = jwtService.generateToken(username, "Vendor");
                        return ResponseEntity.status(HttpStatus.OK).body(vendor.writeVendor(user, token, repositoryService));
                    }
                    break;
                }
                case "Admin" -> {
                    Admin admin = repositoryService.getAdminRepository().findByUserId(user.getId());
                    if (admin != null) {
                        GlobalLogger.logInfo("Admin document found: ", admin);
                        //generate JWT token
                        String token = jwtService.generateToken(username, "Admin");
                        return ResponseEntity.status(HttpStatus.OK).body(admin.writeAdmin(user, token));
                    }
                    break;
                }
            }
            GlobalLogger.logWarning("User document not found.");
            throw new InvalidUserRoleException(user.getUserRole());

        } catch (Exception e) {
            GlobalLogger.logError("Failed to login user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        } finally {
            GlobalLogger.logInfo("Stop: Login user process => ", message);
        }
    }
}
