package com.sithumya20220865.OOPCW;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tickets")
public class Ticket {
    @Id
    private Long id;
    private String eventName;
    private double price;
    private boolean isSold;
}
