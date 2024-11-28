package com.sithumya20220865.OOPCW.Repositories;

import com.sithumya20220865.OOPCW.Models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);
}
