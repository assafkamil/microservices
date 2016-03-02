package com.samples.microservices.avatars.controllers;

import com.samples.microservices.avatars.services.AvatarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    @Autowired
    private AvatarService avatarService;

    @RequestMapping(value="/{user}/{emails}", method = RequestMethod.GET)
    public String getAvatar(@PathVariable("user") String user, @PathVariable("emails") String[] emails) {
        return avatarService.getAvatar(user, emails);
    }
}
