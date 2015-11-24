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
    public UserResponse create(@RequestBody UserCreateRequest request) {
        return convertToResponse(userService.create(request.getUsername(), request.getPassword()));
    }

    @Override
    public UserResponse getById(@PathVariable("id") String id) throws UserNotFoundException{
        return convertToResponse(userService.getById(id));
    }

    @Override
    public List<UserResponse> getAll(@RequestParam(value = "start", defaultValue = "0") int start,
                                     @RequestParam(value = "limit", defaultValue = "-1") int limit) {
        return userService.getAll(start, limit).stream()
                            .map(this::convertToResponse)
                            .collect(toList());
    }

    @Override
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

/*
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public @ResponseBody String handler(UserNotFoundException e) {
        return e.getUserId();
    }*/
}

