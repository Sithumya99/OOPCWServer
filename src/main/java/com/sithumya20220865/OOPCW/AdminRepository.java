package com.sithumya20220865.OOPCW;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AdminRepository extends MongoRepository<Admin, String> {
    Admin findByUserId(String userId);
}
