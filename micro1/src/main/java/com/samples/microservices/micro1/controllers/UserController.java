package com.samples.microservices.micro1.controllers;

import com.samples.microservices.micro1.api.UserCreateRequest;
import com.samples.microservices.micro1.api.UserResponse;
import com.samples.microservices.micro1.api.UserResponses;
import com.samples.microservices.micro1.api.Exceptions.UserNotFoundException;
import com.samples.microservices.micro1.model.User;
import com.samples.microservices.micro1.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RefreshScope
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private GaugeService gaugeService;

    private UserResponse convertToResponse(User user) {
        return new UserResponse(user.getUsername(), user.getUserId());
    }

    @RequestMapping(value="/users", method = RequestMethod.POST, consumes = "application/json")
    public UserResponse create(@RequestBody UserCreateRequest request) {
        return convertToResponse(userService.create(request.getUsername(), request.getPassword()));
    }

    @RequestMapping(value="/users/{id}", method = RequestMethod.GET)
    public UserResponse getById(@PathVariable("id") String id) throws UserNotFoundException{
        gaugeService.submit("askedForId", 1);
        return convertToResponse(userService.getById(id));
    }

    @RequestMapping(value="/users/name/{username}", method = RequestMethod.GET)
    public UserResponse getByUsername(@PathVariable("username")String username) throws UserNotFoundException {
        return convertToResponse(userService.getByUsername(username));
    }

    @RequestMapping(value="/users", method = RequestMethod.GET)
    public List<UserResponse> getAll(@RequestParam(value = "start", defaultValue = "0") int start,
                                     @RequestParam(value = "limit", defaultValue = "-1") int limit) {
        return userService.getAll(start, limit).stream()
                            .map(this::convertToResponse)
                            .collect(toList());
    }

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

