package com.samples.microservices.micro1.controllers;

import com.samples.microservices.micro1.api.UserCreateRequest;
import com.samples.microservices.micro1.api.UserResponse;
import com.samples.microservices.micro1.api.UserResponses;
import com.samples.microservices.micro1.api.UsersResource;
import com.samples.microservices.micro1.api.Exceptions.UserNotFoundException;
import com.samples.microservices.micro1.model.User;
import com.samples.microservices.micro1.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
public class UserController implements UsersResource {
    @Autowired
    private UserService userService;

    private UserResponse convertToResponse(User user) {
        return new UserResponse(user.getUsername(), user.getUserId());
    }

    @Override
    @RequestMapping(value="/users", method = RequestMethod.POST, consumes = "application/json")
    public UserResponse create(@RequestBody UserCreateRequest request) {
        return convertToResponse(userService.create(request.getUsername(), request.getPassword()));
    }

    @Override
    @RequestMapping(value="/users/{id}", method = RequestMethod.GET)
    public UserResponse getById(@PathVariable("id") String id) throws UserNotFoundException{
        return convertToResponse(userService.getById(id));
    }

    @Override
    @RequestMapping(value="/users", method = RequestMethod.GET)
    public List<UserResponse> getAll(@RequestParam(value = "start", defaultValue = "0") int start,
                                     @RequestParam(value = "limit", defaultValue = "-1") int limit) {
        return userService.getAll(start, limit).stream()
                            .map(this::convertToResponse)
                            .collect(toList());
    }

    @Override
    @RequestMapping(value="/users2", method = RequestMethod.GET)
    public UserResponses getAll2(@RequestParam(value = "cursor", defaultValue = "") String cursor) {
        int start = 0;
        if(!cursor.isEmpty()) {
            start = Integer.parseInt(cursor);
        }
        List<UserResponse> userResponses =
                userService.getAll(start, 1).stream()
                .map(this::convertToResponse)
                .collect(toList());
        Integer cursorResult = start + userResponses.size();
        return new UserResponses(userResponses, cursorResult.toString());
    }
}

