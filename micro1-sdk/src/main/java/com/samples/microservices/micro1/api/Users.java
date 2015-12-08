package com.samples.microservices.micro1.api;

import com.samples.microservices.micro1.api.Exceptions.UserNotFoundException;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "users")
public interface Users extends UsersResource {
}
