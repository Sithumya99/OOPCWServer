package com.sithumya20220865.OOPCW.Models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.concurrent.CompletableFuture;

public class Message {
    private String command; //store the request command
    private String data; //store the request body
    private CompletableFuture<ResponseEntity<?>> response;  //final response after completing request
    private Authentication userAuth;


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

    public boolean getBoolean(String property) throws Exception {
        JsonNode jsonNode = getJsonData();
        return jsonNode.get(property).asBoolean(false);
    }

    public double getDouble(String property) throws Exception {
        JsonNode jsonNode = getJsonData();
        return jsonNode.has(property) ? jsonNode.get(property).asDouble() : -1;
    }

    public CompletableFuture<ResponseEntity<?>> getResponse() {
        return response;
    }

    public Authentication getUserAuth() {
        return userAuth;
    }

}
