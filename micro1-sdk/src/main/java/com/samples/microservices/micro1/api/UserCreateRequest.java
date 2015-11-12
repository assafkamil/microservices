package com.samples.microservices.micro1.api;

/**
 * Created by assafkamil on 11/9/15.
 */
public class UserCreateRequest {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
