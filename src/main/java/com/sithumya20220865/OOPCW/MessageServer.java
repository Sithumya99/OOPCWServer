package com.sithumya20220865.OOPCW;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/execute")
public class MessageServer {

    @Autowired
    private MessageServerService messageServerService;

    @PostMapping("/{command}")
    public CompletableFuture<ResponseEntity<?>> executeCommandPost(@PathVariable String command, @RequestBody String body) {
        Message message = new Message(command, body);
        return messageServerService.executeCommandService(message);
    }

}
