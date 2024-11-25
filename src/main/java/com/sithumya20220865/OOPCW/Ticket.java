package com.sithumya20220865.OOPCW;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tickets")
public class Ticket {
    @Id
    private String id;
    private String eventName;
    private double price;
    private boolean isSold;

    public Ticket(String eventName, double price) {
        this.eventName = eventName;
        this.price = price;
        this.isSold = false;
    }

    public String getId() {
        return id;
    }

    public void setSold(boolean sold) {
        isSold = sold;
    }

    public boolean getSold() {return isSold;}
}
