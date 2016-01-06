package com.samples.microservices.micro2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@ComponentScan(basePackages = "com.samples", excludeFilters =
        @ComponentScan.Filter(type=FilterType.REGEX, pattern={"com.samples.bootstrap.*"}))
@EnableFeignClients(basePackages = "com.samples")
@EnableDiscoveryClient
@Profile("!test")
public class Application {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
