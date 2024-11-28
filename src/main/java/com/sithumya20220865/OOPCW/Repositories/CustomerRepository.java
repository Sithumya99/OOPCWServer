package com.sithumya20220865.OOPCW.Repositories;

import com.sithumya20220865.OOPCW.Models.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    Customer findByUserId(String userId);
}
