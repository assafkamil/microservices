package com.samples.microservices.micro1.repository;


import com.samples.microservices.micro1.model.User;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface UserRepository extends PagingAndSortingRepository<User, String> {
    List<User> findByUsername(String username);
}
