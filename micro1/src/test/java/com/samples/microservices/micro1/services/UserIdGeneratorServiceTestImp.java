package com.samples.microservices.micro1.services;

import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by assafkamil on 11/12/15.
 */
//@Service
public class UserIdGeneratorServiceTestImp implements UserIdGeneratorService{
    @Override
    public String generateId() {
        return "5";
    }
}
