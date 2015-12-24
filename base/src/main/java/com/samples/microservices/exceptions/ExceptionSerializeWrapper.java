package com.samples.microservices.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = ExceptionDeserializer.class)
public class ExceptionSerializeWrapper {
    private String className;

    private ExceptionBase exception;

    public ExceptionSerializeWrapper() {}

    public ExceptionSerializeWrapper(ExceptionBase exception) {
        this.exception = exception;
        this.className = exception.getClass().getCanonicalName();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public ExceptionBase getException() {
        return exception;
    }

    public void setException(ExceptionBase exception) {
        this.exception = exception;
    }
}
