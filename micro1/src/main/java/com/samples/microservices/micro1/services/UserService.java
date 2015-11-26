package com.samples.microservices.micro1.services;
import com.netflix.loadbalancer.reactive.ExecutionListener;
import com.samples.microservices.micro1.model.User;
import com.samples.microservices.micro1.services.Exceptions.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by assafkamil on 11/9/15.
 */
@Service
public class UserService {
    @Autowired
    private UserIdGeneratorService userIdGeneratorService;

    HashMap<String, User> users = new HashMap<String, User>();

    private static final Logger logger = LoggerFactory
            .getLogger(UserService.class);

    public User create(String username, String password) {
        logger.debug("new user created " + username);

        User user = new User(username, userIdGeneratorService.generateId(), password);
        users.put(user.getUserId(), user);
        return user;
    }

    public User getById(String userId) throws UserNotFoundException {
        User user = users.get(userId);
        if(user == null) {
            UserNotFoundException exception = new UserNotFoundException(userId);
            throw exception;
        }
        return user;
    }

    public List<User> getAll(int start, int limit) {
        List<User> usersResult = new ArrayList<User>();
        List<User> userValues = new ArrayList<User>(users.values());
        if(limit < 0) {
            limit = userValues.size();
        }
        for(int i=start; i < start + limit; i++) {
            usersResult.add(userValues.get(i));
        }
        return usersResult;
    }
}
