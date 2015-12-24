package com.samples.microservices.controllers;

import com.samples.microservices.exceptions.ExceptionBase;
import com.samples.microservices.exceptions.ExceptionSerializeWrapper;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class ControllerAdvise {

    @ExceptionHandler(value = ExceptionBase.class)
    public @ResponseBody
    ExceptionSerializeWrapper errorHandler(HttpServletResponse resp, ExceptionBase e) {
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            resp.setStatus(responseStatus.value().value());
        } else {
            resp.setStatus(500);
        }
        return new ExceptionSerializeWrapper(e);
    }
}
