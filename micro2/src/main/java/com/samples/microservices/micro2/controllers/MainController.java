package com.samples.microservices.micro2.controllers;

import com.samples.microservices.micro1.api.Exceptions.UserNotFoundException;
import com.samples.microservices.micro1.api.UserCreateRequest;
import com.samples.microservices.micro1.api.UserResponse;
import com.samples.microservices.micro1.api.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by assafkamil on 11/9/15.
 */
@RestController
public class MainController {

    @Autowired
    private Users users;

    @RequestMapping("/")
    public String micro1() {
        UserCreateRequest userCreateRequest = new UserCreateRequest();
        userCreateRequest.setUsername("moshe");
        userCreateRequest.setPassword("aaaaa");
        UserResponse userResponse = users.create(userCreateRequest);
        return userResponse.getUserId();
    }

    @RequestMapping("/user")
    public String micro1Find() {
        UserResponse userResponse = null;
        try {
            userResponse = users.getById("shmulik");
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }
        return userResponse.getUserId();
    }
}
