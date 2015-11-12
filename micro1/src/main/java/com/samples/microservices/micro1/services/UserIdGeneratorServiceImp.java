package com.samples.microservices.micro1.services;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by assafkamil on 11/12/15.
 */
@Service
@Profile("!test")
public class UserIdGeneratorServiceImp implements UserIdGeneratorService{
    @Override
    public String generateId() {
        return UUID.randomUUID().toString();
    }
}
