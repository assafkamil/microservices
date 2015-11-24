package com.samples.microservices.micro1.api.Exceptions;

public class UserNotFoundException extends Exception {
    private String userId;

    public UserNotFoundException(String userId) {
        super();
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
