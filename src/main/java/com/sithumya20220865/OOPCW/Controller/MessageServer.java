package com.sithumya20220865.OOPCW.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sithumya20220865.OOPCW.Services.*;
import com.sithumya20220865.OOPCW.Logger.*;
import com.sithumya20220865.OOPCW.Exceptions.*;
import com.sithumya20220865.OOPCW.Configs.*;
import com.sithumya20220865.OOPCW.Models.*;

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

    @Autowired
    private JWTService jwtService; //for JWT tokens

    @Autowired
    private RepositoryService repositoryService; //accessing mongodb repos

    //handle all incoming post requests
    @PostMapping("/{command}")
    public CompletableFuture<ResponseEntity<?>> executeCommandPost(
            @PathVariable String command, @RequestBody String body, HttpServletRequest request) {
        GlobalLogger.logInfo("Receive request "+ command, null);
        return addTask(command, body, request);
    }

    //handle all incoming get requests
    @GetMapping("/{command}")
    public CompletableFuture<ResponseEntity<?>> executeCommandGet(
            @PathVariable String command, HttpServletRequest request) {
        //create response node
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode res = objectMapper.createObjectNode();

        if ("gettickets".equalsIgnoreCase(command)) {
            GlobalLogger.logInfo("Receive request "+ command, null);
            Authentication currentAuth = jwtAuthenticationService.authenticate(request);
            GlobalLogger.logInfo("Start: Get tickets process => ", currentAuth.getPrincipal());

            if (currentAuth == null) {
                GlobalLogger.logError("Unauthorized: ",
                        new UserUnauthorizedException(currentAuth.getPrincipal().toString(), "no role"));
                GlobalLogger.logInfo("Stop: Get tickets process ", null);
                res.put("message", "Authorization failed.");
                return CompletableFuture.completedFuture(
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(res));
            }

            if (SessionConfiguration.getInstance() == null) {
                GlobalLogger.logWarning("Session not configured.");
                GlobalLogger.logInfo("Stop: Get tickets process ", currentAuth.getPrincipal());
                res.put("message", "Ticket session is not active");
                return CompletableFuture.completedFuture(
                        ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .header("Authorization", "Bearer " + currentAuth.getCredentials())
                                .body(res));
            } else {
                ArrayList<Ticket> tickets = new ArrayList<>();
                ticketPoolService.writeTicketPool(tickets);
                GlobalLogger.logInfo("Get tickets completed successfully ", currentAuth.getPrincipal());
                GlobalLogger.logInfo("Stop: Get tickets process ", currentAuth.getPrincipal());
                return CompletableFuture.completedFuture(
                        ResponseEntity.status(HttpStatus.OK)
                                .header("Authorization", "Bearer " + currentAuth.getCredentials())
                                .body(tickets));
            }
        } else {
            res.put("message", "Invalid request");
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(res));
        }
    }

    private CompletableFuture<ResponseEntity<?>> addTask(String command, String body, HttpServletRequest request) {
        GlobalLogger.logInfo("Start: Add task process => ", null);
        //create response node
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode res = objectMapper.createObjectNode();

        try {
            Authentication currentAuth = null;
            //create response object
            CompletableFuture<ResponseEntity<?>> response = new CompletableFuture<>();

            //authenticate request
            if (!"register".equalsIgnoreCase(command) && !"login".equalsIgnoreCase(command)) {
                currentAuth = jwtAuthenticationService.authenticate(request);
                if (currentAuth == null) {
                    GlobalLogger.logError("Unauthorized: ",
                            new UserUnauthorizedException(currentAuth.getPrincipal().toString(), "no role"));
                    res.put("message", "Invalid or missing token");
                    return CompletableFuture.completedFuture(
                            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res));
                }
            }

            if (currentAuth != null) {
                //set authentication context
                SecurityContextHolder.getContext().setAuthentication(currentAuth);
            }

            //create message object
            Message message = new Message(command, body, response, currentAuth);
            GlobalLogger.logInfo("New message task created: ", message);

            //add to queue
            boolean enqueued = messageQueueService.enqueue(message);

            //return response
            if (!enqueued) {
                GlobalLogger.logWarning("Queue is full, rejecting request");
                res.put("message", "Queue is full,try again later");
                return CompletableFuture.completedFuture(
                        ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(res));
            } else {
                ResponseEntity<?> resEnt = response.join();
                GlobalLogger.logInfo("Task completed: ", message);
                return CompletableFuture.completedFuture(resEnt);
            }

        } catch (Exception e) {
            GlobalLogger.logError("Failed to add task: ", e);
            res.put("message", "Server error: " + e.getMessage());
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res));
        } finally {
            GlobalLogger.logInfo("Stop: Add task process => ", null);
        }
    }

}
