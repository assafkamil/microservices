package com.samples.microservices.micro2.api;

import com.samples.microservices.micro1.api.UserCreateRequest;
import com.samples.microservices.micro1.api.UserResponse;
import com.samples.microservices.micro1.api.UserResponses;
import com.samples.microservices.micro1.api.Users;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by assafkamil on 11/12/15.
 */
@Service
public class UsersTestImp implements Users {
    @Override
    public UserResponse create(UserCreateRequest userCreateRequest) {
        return new UserResponse("abc", "123");
    }

    @Override
    public UserResponse getById(String s) {
        return null;
    }

    @Override
    public List<UserResponse> getAll(int i, int i1) {
        return null;
    }

    @Override
    public UserResponses getAll2(String s) {
        return null;
    }
}
