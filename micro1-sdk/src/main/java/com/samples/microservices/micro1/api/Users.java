package com.samples.microservices.micro1.api;

import org.springframework.cloud.netflix.feign.FeignClient;

@FeignClient(value = "users")
public interface Users extends UsersResource {
}
