package com.samples.microservices.avatars.api.exceptions;

public class AvatarNotFound extends Exception {
    private String avatar;

    public AvatarNotFound(String user) {
        super();
        this.avatar = user;
    }
}
