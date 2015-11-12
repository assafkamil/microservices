package com.samples.microservices.micro1.services;

/**
 * Created by assafkamil on 11/12/15.
 */

import com.samples.microservices.micro1.Application;
import com.samples.microservices.micro1.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@ActiveProfiles("test")
public class UserServiceTests {

    @Autowired
    private UserService userService;

    @Test
    public void testCreate() {
        User user = userService.create("user1", "abc");
        assertEquals("user1", user.getUsername());
        assertEquals("abc", user.getPassword());
        //assertNotNull(user.getUserId());
        assertEquals("5", user.getUserId());
    }

    @Test
    public void testGetById() {
        User user = userService.create("user1", "abc");
        User user1 = userService.getById(user.getUserId());
        assertEquals(user, user1);
    }
}
