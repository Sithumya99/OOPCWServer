package com.sithumya20220865.OOPCW;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.concurrent.CompletableFuture;

public class Message {
    private String command; //store the request command
    private String data; //store the request body
    private CompletableFuture<ResponseEntity<?>> response;  //final response after completing request
    private Authentication userAuth;

    private HttpStatus status;

    public Message(String command, String data, CompletableFuture<ResponseEntity<?>> response, Authentication auth) {
        this.command = command;
        this.data = data;
        this.response = response;
        this.userAuth = auth;
    }

    public String getCommand() {
        return command;
    }

    //read the request body into JsonNode
    private JsonNode getJsonData() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(this.data);
    }

    //extract int properties from request body
    public int getInt(String property) throws Exception {
        JsonNode jsonNode = getJsonData();
        return jsonNode.has(property) ? jsonNode.get(property).asInt() : -1;
    }

    //extract string properties from request body
    public String getString(String property) throws Exception {
        JsonNode jsonNode = getJsonData();
        return jsonNode.has(property) ? jsonNode.get(property).asText() : null;
    }

    public CompletableFuture<ResponseEntity<?>> getResponse() {
        return response;
    }

    public void setResponse(String response) {
        System.out.println("msg resp: " + response);
        System.out.println("res ent: " + ResponseEntity.status(status).body(response));
        this.response = CompletableFuture.completedFuture(ResponseEntity.status(status).body(response));
    }

    public void setResponse(ObjectNode response) {
        this.response = CompletableFuture.completedFuture(ResponseEntity.status(status).body(response));
    }

    public Authentication getUserAuth() {
        return userAuth;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}
