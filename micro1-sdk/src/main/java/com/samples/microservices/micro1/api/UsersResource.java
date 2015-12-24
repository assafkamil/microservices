package com.samples.microservices.micro1.api;


import com.samples.microservices.micro1.api.Exceptions.UserNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
public interface UsersResource {
    @RequestMapping(value="/users", method = RequestMethod.POST, consumes = "application/json")
    public UserResponse create(@RequestBody UserCreateRequest request);

    @RequestMapping(value="/users/{id}", method = RequestMethod.GET)
    public UserResponse getById(@PathVariable("id") String id) throws UserNotFoundException;

    @RequestMapping(value="/users/name/{username}", method = RequestMethod.GET)
    public UserResponse getByUsername(@PathVariable("username")String username) throws UserNotFoundException;

    @RequestMapping(value="/users", method = RequestMethod.GET)
    public List<UserResponse> getAll(@RequestParam(value = "start", defaultValue = "0") int start,
                                     @RequestParam(value = "limit", defaultValue = "-1") int limit);

    @RequestMapping(value="/users2", method = RequestMethod.GET)
    public UserResponses getAll2(@RequestParam(value = "cursor", defaultValue = "") String cursor);
}
