package com.sithumya20220865.OOPCW;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Message {
    private String command; //store the request command
    private String data; //store the request body

    public Message(String command, String data) {
        this.command = command;
        this.data = data;
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
}
