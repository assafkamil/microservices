package com.samples.microservices.avatars;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.samples")
public class Application extends SpringBootServletInitializer {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
