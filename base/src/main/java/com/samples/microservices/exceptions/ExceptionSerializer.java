package com.samples.microservices.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = ExceptionDeserializer.class)
public class ExceptionSerializer {
    private String className;

    @JsonIgnoreProperties({"cause", "stackTrace", "suppressed"})
    private Exception exception;

    public ExceptionSerializer() {}

    public ExceptionSerializer(Exception exception) {
        this.exception = exception;
        this.className = exception.getClass().getCanonicalName();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
