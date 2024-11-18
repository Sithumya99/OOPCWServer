package com.sithumya20220865.OOPCW;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/execute")
public class MessageServer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping("/{command}")
    public ResponseEntity<?> executeCommandPost(@PathVariable String command, @RequestBody String body) {
        try {
            Message message = new Message(command, body);

            //process message
            //register new user
            if ("register".equalsIgnoreCase(message.getCommand())) {
                return handleRegister(message);
            } else if ("login".equalsIgnoreCase(message.getCommand())) {
                return handleLogin(message);
            } else {
                return ResponseEntity.badRequest().body("Unknown command");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error handling request: " + e.getMessage());
        }
    }

    private ResponseEntity<?> handleRegister(Message message) {
        User newUser = new User();

        try {
            //get user values into object
            newUser.parseRequest(message);

            //check if user already exists
            if (userRepository.findByUsername(newUser.getUsername()) != null) {
                return ResponseEntity.badRequest().body("Username already exists");
            }

            //save new user
            userRepository.save(newUser);

            //create role relevant document
            switch (newUser.userRole) {
                case Customer -> {
                    Customer newCustomer = new Customer(newUser.getId());
                    newCustomer.setNoOfTicketsBought(0);
                    customerRepository.save(newCustomer);
                    return ResponseEntity.ok(newCustomer.writeCustomer(newUser));
                }
                case Vendor -> {
                    Vendor newVendor = new Vendor();
                    newVendor.setTotalTickets(0);
                }
                case Admin -> {
                    System.out.println("role: Admin");
                }
            }
            return ResponseEntity.ok("User registered successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }

    private ResponseEntity<?> handleLogin(Message message) {
        try {
            String username = message.getString("username");
            String password = message.getString("password");

            // Authenticate the user
            User user = userRepository.findByUsername(username);
            if (user == null || !user.getPassword().equals(password)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }

            //retrieve document relevant to role
            switch (user.userRole) {
                case Customer -> {
                    Customer customer = customerRepository.findByUserId(user.getId());
                    if (customer != null) {
                        return ResponseEntity.ok(customer.writeCustomer(user));
                    } else {
                        return ResponseEntity.badRequest().body("user does not exist.");
                    }
                }
                case Vendor -> {
                    return ResponseEntity.ok("role: vendor");
                }
                case Admin -> {
                    return ResponseEntity.ok("role: admin");
                }
            }
            return ResponseEntity.badRequest().body("Invalid role.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }
}
