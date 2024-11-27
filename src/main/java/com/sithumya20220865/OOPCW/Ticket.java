package com.sithumya20220865.OOPCW;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tickets")
public class Ticket {
    @Id
    private String id;
    private String vendorId;
    private String eventName;
    private double price;
    private boolean isSold;

    public Ticket(String eventName, double price, String vendorId) {
        this.eventName = eventName;
        this.price = price;
        this.isSold = false;
        this.vendorId = vendorId;
    }

    public String getId() {
        return id;
    }

    public String getEventName() {
        return eventName;
    }

    public double getPrice() {
        return price;
    }

    public void setSold(boolean sold) {
        isSold = sold;
    }

    public boolean getSold() {return isSold;}

    public void writeTicket(ObjectNode ticketNode) {
        ticketNode.put("event", eventName);
        ticketNode.put("price", price);
        ticketNode.put("isSold", isSold);
    }
}
