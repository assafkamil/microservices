package com.samples.microservices.controllers;

import com.samples.microservices.exceptions.ExceptionSerializer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class ControllerAdvise {

    @ExceptionHandler(value = Exception.class)
    public @ResponseBody ExceptionSerializer errorHandler(HttpServletResponse resp, Exception e) throws Exception {
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            resp.setStatus(responseStatus.value().value());
        } else {
            resp.setStatus(500);
        }
        return new ExceptionSerializer(e);
    }
}
