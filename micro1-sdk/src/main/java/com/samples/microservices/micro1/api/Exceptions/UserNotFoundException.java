package com.samples.microservices.micro1.api.Exceptions;

import com.samples.microservices.exceptions.ExceptionBase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends ExceptionBase {
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
