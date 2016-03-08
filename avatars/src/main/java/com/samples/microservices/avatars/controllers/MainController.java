package com.samples.microservices.avatars.controllers;

import com.samples.microservices.avatars.api.Avatar;
import com.samples.microservices.avatars.api.exceptions.AvatarNotFound;
import com.samples.microservices.avatars.services.AvatarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MainController {
    @Autowired
    private AvatarService avatarService;

    @RequestMapping(value="/{user}/{emails}", method = RequestMethod.GET)
    public Avatar getAvatar(@PathVariable("user") String user, @PathVariable("emails") String[] emails) throws AvatarNotFound {
        return avatarService.getAvatar(user, emails);
    }
}
