package com.sithumya20220865.OOPCW;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface VendorRepository extends MongoRepository<Vendor, String> {
    Vendor findByUserId(String userId);
}
