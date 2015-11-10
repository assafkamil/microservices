package com.samples.microservices.micro1.api;

/**
 * Created by assafkamil on 11/9/15.
 */
public class UserResponse {
    private String username;
    private String userId;

    public UserResponse() {}

    public UserResponse(String username, String userId) {
        this.username = username;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
