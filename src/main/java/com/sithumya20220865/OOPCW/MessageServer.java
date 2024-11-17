package com.sithumya20220865.OOPCW;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/execute")
public class MessageServer {

    @PostMapping("/{command}")
    public ResponseEntity<String> executeCommandPost(@PathVariable String command, @RequestBody String body) {
        try {
            Message message = new Message(command, body);

            //process message

            String res = "done";
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error handling request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
