package com.sithumya20220865.OOPCW.Controller;

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
            @PathVariable String command, @RequestBody String body, HttpServletRequest request) {
        GlobalLogger.logInfo("Receive request "+ command, null);
        Authentication currentAuth = jwtAuthenticationService.authenticate(request);
        GlobalLogger.logInfo("Start: Get tickets process => ", currentAuth.getPrincipal());

        if (currentAuth == null) {
            GlobalLogger.logError("Unauthorized: ",
                    new UserUnauthorizedException(currentAuth.getPrincipal().toString(), "no role"));
            GlobalLogger.logInfo("Stop: Get tickets process ", null);
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("Authorization failed."));
        }

        if (SessionConfiguration.getInstance() == null) {
            GlobalLogger.logWarning("Session not configured.");
            GlobalLogger.logInfo("Stop: Get tickets process ", currentAuth.getPrincipal());
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .header("Authorization", "Bearer " + currentAuth.getCredentials())
                            .body("Ticket session is not active"));
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
    }

    private CompletableFuture<ResponseEntity<?>> addTask(String command, String body, HttpServletRequest request) {
        GlobalLogger.logInfo("Start: Add task process => ", null);
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
                    return CompletableFuture.completedFuture(
                            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token"));
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
                return CompletableFuture.completedFuture(
                        ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Queue is full,try again later"));
            } else {
                ResponseEntity<?> res = response.join();
                GlobalLogger.logInfo("Task completed: ", message);
                return CompletableFuture.completedFuture(res);
            }

        } catch (Exception e) {
            GlobalLogger.logError("Failed to add task: ", e);
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()));
        } finally {
            GlobalLogger.logInfo("Stop: Add task process => ", null);
        }
    }

}
