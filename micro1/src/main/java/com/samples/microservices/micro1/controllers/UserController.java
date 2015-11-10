package com.samples.microservices.micro1.controllers;

import com.samples.microservices.micro1.api.UserCreateRequest;
import com.samples.microservices.micro1.api.UserResponse;
import com.samples.microservices.micro1.api.UserResponses;
import com.samples.microservices.micro1.model.User;
import com.samples.microservices.micro1.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by assafkamil on 11/9/15.
 */
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    private UserResponse convertToResponse(User user) {
        return new UserResponse(user.getUsername(), user.getUserId());
    }

    @RequestMapping(value="/users", method = RequestMethod.POST)
    public UserResponse create(@RequestBody UserCreateRequest request) {
        return convertToResponse(userService.create(request.getUsername(), request.getPassword()));
    }

    @RequestMapping(value="/users/{id}", method = RequestMethod.GET)
    public UserResponse getById(@PathVariable("id")String id) {
        return convertToResponse(userService.getById(id));
    }

    @RequestMapping(value="/users", method = RequestMethod.GET)
    public List<UserResponse> getAll(@RequestParam(value="start", defaultValue = "0") int start,
                                     @RequestParam(value="limit", defaultValue = "-1") int limit) {
        List<UserResponse> userResponses = new ArrayList<UserResponse>();
        for(User user:userService.getAll(start, limit)) {
            userResponses.add(convertToResponse(user));
        }
        return userResponses;
    }

    @RequestMapping(value="/users2", method = RequestMethod.GET)
    public UserResponses getAll2(@RequestParam(value="cursor", defaultValue = "") String cursor) {
        List<UserResponse> userResponses = new ArrayList<UserResponse>();
        int start = 0;
        if(!cursor.isEmpty()) {
            start = Integer.parseInt(cursor);
        }
        for(User user:userService.getAll(start, 1)) {
            userResponses.add(convertToResponse(user));
        }
        Integer cursorResult = start + userResponses.size();
        return new UserResponses(userResponses, cursorResult.toString());
    }
}

