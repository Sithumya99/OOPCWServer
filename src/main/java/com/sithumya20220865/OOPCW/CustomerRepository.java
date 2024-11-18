package com.sithumya20220865.OOPCW;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    Customer findByUserId(String userId);
}
