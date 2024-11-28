package com.sithumya20220865.OOPCW.Repositories;


import com.sithumya20220865.OOPCW.Models.Vendor;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VendorRepository extends MongoRepository<Vendor, String> {
    Vendor findByUserId(String userId);
}
