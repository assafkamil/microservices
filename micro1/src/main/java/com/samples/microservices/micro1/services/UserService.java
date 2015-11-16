package com.samples.microservices.micro1.services;
import com.samples.microservices.micro1.model.User;
import com.samples.microservices.micro1.services.Exceptions.UserNotFoundException;
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

    public User create(String username, String password) {
        User user = new User(username, userIdGeneratorService.generateId(), password);
        users.put(user.getUserId(), user);
        return user;
    }

    public User getById(String userId) throws UserNotFoundException {
        User user = users.get(userId);
        if(user == null) {
            throw new UserNotFoundException(userId);
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
