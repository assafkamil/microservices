package com.samples.microservices.micro1.api;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by assafkamil on 11/9/15.
 */
@FeignClient("users")
public interface Users {
    @RequestMapping(value="/users", method = RequestMethod.POST, consumes = "application/json")
    public UserResponse create(@RequestBody UserCreateRequest request);

    @RequestMapping(value="/users/{id}", method = RequestMethod.GET, consumes = "application/json")
    public UserResponse getById(@PathVariable("id") String id);

    @RequestMapping(value="/users", method = RequestMethod.GET, consumes = "application/json")
    public List<UserResponse> getAll(@RequestParam(value = "start", defaultValue = "0") int start,
                                     @RequestParam(value = "limit", defaultValue = "-1") int limit);

    @RequestMapping(value="/users2", method = RequestMethod.GET, consumes = "application/json")
    public UserResponses getAll2(@RequestParam(value = "cursor", defaultValue = "") String cursor);
}
