package com.samples.microservices.micro1.api;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("users")
public interface Users extends UsersResource{
}
