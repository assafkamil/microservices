package com.samples.microservices.micro1.services;
import com.samples.microservices.micro1.model.User;
import com.samples.microservices.micro1.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.samples.microservices.micro1.api.Exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {
    @Autowired
    private UserIdGeneratorService userIdGeneratorService;

    HashMap<String, User> users = new HashMap<String, User>();

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory
            .getLogger(UserService.class);

    @Transactional(readOnly = false)
    public User create(String username, String password) {
        logger.debug("new user created " + username);

        User user = new User(username, userIdGeneratorService.generateId(), password);
        userRepository.save(user);
        return user;
    }

    public User getById(String userId) throws UserNotFoundException {
        User user = userRepository.findOne(userId);
        if(user == null) {
            UserNotFoundException exception = new UserNotFoundException(userId);
            throw exception;
        }
        return user;
    }

    public User getByUsername(String username) throws UserNotFoundException {
        List<User> users = userRepository.findByUsername(username);
        if(users.size() == 0) {
            UserNotFoundException exception = new UserNotFoundException(username);
            throw exception;
        }
        return users.get(0);
    }

    public List<User> getAll(int start, int limit) {
        List<User> usersResult = new ArrayList<User>();
        List<User> userValues = new ArrayList<User>(users.values());
        if(limit < 0) {
            limit = userValues.size();
        }

        Page<User> users = userRepository.findAll(new PageRequest(start, limit));
        for(User user : users) {
            usersResult.add(user);
        }
        return usersResult;
    }
}
