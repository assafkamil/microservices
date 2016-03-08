package com.samples.microservices.avatars.api;

import com.samples.microservices.avatars.api.exceptions.AvatarNotFound;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "avatars")
public interface Avatars {
    @RequestMapping(value="/{user}/{emails}", method = RequestMethod.GET)
    public Avatar getAvatar(@PathVariable("user") String user, @PathVariable("emails") String[] emails) throws AvatarNotFound;
}