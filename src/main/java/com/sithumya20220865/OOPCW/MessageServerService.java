package com.sithumya20220865.OOPCW;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class MessageServerService {

    @Async
    public CompletableFuture<ResponseEntity<?>> executeCommandService(Message message) {
        try {
            //process message
            if ("register".equalsIgnoreCase(message.getCommand())) {
                //register new user
                HandleRegisterUserUtil newUserRegister = new HandleRegisterUserUtil(message);
                return CompletableFuture.completedFuture(newUserRegister.execute());
            } else if ("login".equalsIgnoreCase(message.getCommand())) {
                //authenticate user
                HandleLoginUserUtil loginUser = new HandleLoginUserUtil(message);
                return CompletableFuture.completedFuture(loginUser.execute());
            } else if ("startsession".equalsIgnoreCase(message.getCommand())) {
                //start ticket session
                HandleStartSessionUtil newSession = new HandleStartSessionUtil(message);
                return CompletableFuture.completedFuture(newSession.execute());
            } else if ("stopsession".equalsIgnoreCase(message.getCommand())) {
                //stop ticket session
                HandleStopSessionUtil stopSession = new HandleStopSessionUtil(message);
                return CompletableFuture.completedFuture(stopSession.execute());
            } else {
                return CompletableFuture.completedFuture(ResponseEntity.badRequest().body("Unknown command"));
            }

        } catch (Exception e) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error handling request: " + e.getMessage()));
        }
    }
}
