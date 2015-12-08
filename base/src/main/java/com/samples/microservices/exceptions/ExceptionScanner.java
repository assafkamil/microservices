package com.samples.microservices.exceptions;


import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

public class ExceptionScanner extends ClassPathScanningCandidateComponentProvider {
    public ExceptionScanner() {
        super(true);
        addIncludeFilter(new AssignableTypeFilter(Exception.class));
    }

}
