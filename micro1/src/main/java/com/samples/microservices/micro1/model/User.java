package com.samples.microservices.micro1.model;

/**
 * Created by assafkamil on 11/9/15.
 */
public class User {
    private String userId;
    private String username;
    private String password;

    public User() {}

    public User(String username) {
        this.username = username;
    }

    public User(String username, String userId) {
        this.userId = userId;
        this.username = username;
    }

    public User(String username, String userId, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
