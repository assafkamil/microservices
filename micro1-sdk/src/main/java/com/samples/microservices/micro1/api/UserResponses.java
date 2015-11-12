package com.samples.microservices.micro1.api;

import java.util.List;

/**
 * Created by assafkamil on 11/9/15.
 */
public class UserResponses {
    private List<UserResponse> userResponses;
    private String cursor;

    public UserResponses(){}

    public UserResponses(List<UserResponse> userResponses, String cursor) {
        this.userResponses = userResponses;
        this.cursor = cursor;
    }

    public List<UserResponse> getUserResponses() {
        return userResponses;
    }

    public void setUserResponses(List<UserResponse> userResponses) {
        this.userResponses = userResponses;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
}
