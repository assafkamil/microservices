package com.samples.microservices.micro1.configurations;

import com.samples.microservices.micro1.services.UserIdGeneratorService;
import com.samples.microservices.micro1.services.UserIdGeneratorServiceTestImp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by assafkamil on 11/12/15.
 */
@Configuration
public class TestConfiguration {
    @Bean
    public UserIdGeneratorService userIdGeneratorService() {
        return new UserIdGeneratorServiceTestImp();
    }
}
