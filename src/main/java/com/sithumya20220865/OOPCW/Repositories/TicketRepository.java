package com.sithumya20220865.OOPCW.Repositories;

import com.sithumya20220865.OOPCW.Models.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TicketRepository extends MongoRepository<Ticket, String> {
    List<Ticket> findByVendorId(String vendorId);
    List<Ticket> findByIsSold(boolean isSold);
}
