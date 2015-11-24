package com.samples.microservices.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class ControllerAdvise {

    @ExceptionHandler(value = Exception.class)
    public void errorHandler(HttpServletResponse resp, Exception e) throws Exception {
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            resp.setContentType("application/json");
            resp.setStatus(responseStatus.value().value());
            ObjectMapper mapper = new ObjectMapper();
            resp.getWriter().write(mapper.writeValueAsString(e));
            resp.getWriter().flush();
            resp.getWriter().close();
        } else {
            throw e;
        }
    }
}
