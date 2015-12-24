package com.samples.microservices.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"cause", "stackTrace", "suppressed"})
public class ExceptionBase extends Exception {
}
