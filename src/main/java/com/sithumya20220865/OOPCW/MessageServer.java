package com.sithumya20220865.OOPCW;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/execute")
public class MessageServer {

    //for queueing tasks
    @Autowired
    private MessageQueueService messageQueueService;

    //for authentication
    @Autowired
    private JwtAuthenticationService jwtAuthenticationService;

    @Autowired
    private TicketPoolService ticketPoolService;  //for reading tickets

    //handle all incoming post requests
    @PostMapping("/{command}")
    public CompletableFuture<ResponseEntity<?>> executeCommandPost(
            @PathVariable String command, @RequestBody String body, HttpServletRequest request) {
        System.out.println("start msg server");
        return addTask(command, body, request);
    }

    //handle all incoming get requests
    @GetMapping("/{command}")
    public CompletableFuture<ResponseEntity<?>> executeCommandGet(
            @PathVariable String command, @RequestBody String body, HttpServletRequest request) {
        if (SessionConfiguration.getInstance() == null) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Ticket session is not active"));
        } else {
            ArrayList<Ticket> tickets = new ArrayList<>();
            ticketPoolService.writeTicketPool(tickets);
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.OK)
                    .body(tickets));
        }
    }

    private CompletableFuture<ResponseEntity<?>> addTask(String command, String body, HttpServletRequest request) {
        try {
            Authentication currentAuth = null;
            //create response object
            CompletableFuture<ResponseEntity<?>> response = new CompletableFuture<>();

            //authenticate request
            if (!"register".equalsIgnoreCase(command) && !"login".equalsIgnoreCase(command)) {
                currentAuth = jwtAuthenticationService.authenticate(request);
                System.out.println("authentocation in msg: " + currentAuth);
                if (currentAuth == null) {
                    return CompletableFuture.completedFuture(
                            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token"));
                }
            }

            if (currentAuth != null) {
                //set authentication context
                System.out.println("Authentication context before: " + SecurityContextHolder.getContext().getAuthentication());
                SecurityContextHolder.getContext().setAuthentication(currentAuth);
                System.out.println("Authentication context after: " + SecurityContextHolder.getContext().getAuthentication());
            }

            //create message object
            Message message = new Message(command, body, response, currentAuth);

            //add to queue
            boolean enqueued = messageQueueService.enqueue(message);

            //return response
            if (!enqueued) {
                return CompletableFuture.completedFuture(
                        ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Queue is full,try again later"));
            } else {
                System.out.println("End security context: " + SecurityContextHolder.getContext().getAuthentication());
                ResponseEntity<?> res = response.join();
                return CompletableFuture.completedFuture(res);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()));
        }
    }

}
