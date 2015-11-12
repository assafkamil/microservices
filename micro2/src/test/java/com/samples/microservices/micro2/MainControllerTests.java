package com.samples.microservices.micro2;

import com.samples.microservices.micro2.controllers.MainController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Created by assafkamil on 11/12/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ApplicationTests.class)
@ActiveProfiles("test")
public class MainControllerTests {
    @Autowired
    private MainController mainController;

    @Test
    public void testMicro1() {
        String res = mainController.micro1();
        assertNotNull(res);
    }
}
