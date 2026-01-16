package com.example;

import UserService.*;
import org.apache.thrift.TException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class UserServiceImpl implements UserService.Iface {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public User createUser(String name, int age) throws TException {
        long id = idGenerator.getAndIncrement();
        User user = new User(id, name, age);
        users.put(id, user);
        System.out.println("Created user: " + user);
        return user;
    }

    @Override
    public User getUser(long userId) throws UserNotFoundException, TException {
        User user = users.get(userId);
        if (user == null) {
            throw new UserNotFoundException("User with ID " + userId + " not found");
        }
        System.out.println("Retrieved user: " + user);
        return user;
    }

    @Override
    public User updateUser(User user) throws UserNotFoundException, TException {
        if (!users.containsKey(user.getId())) {
            throw new UserNotFoundException("User with ID " + user.getId() + " not found");
        }
        users.put(user.getId(), user);
        System.out.println("Updated user: " + user);
        return user;
    }

    @Override
    public void deleteUser(long userId) throws UserNotFoundException, TException {
        if (!users.containsKey(userId)) {
            throw new UserNotFoundException("User with ID " + userId + " not found");
        }
        User removed = users.remove(userId);
        System.out.println("Deleted user: " + removed);
    }

    @Override
    public List<User> getAllUsers() throws TException {
        List<User> allUsers = users.values().stream().collect(Collectors.toList());
        System.out.println("Retrieved " + allUsers.size() + " users");
        return allUsers;
    }

    @Override
    public String validateUserData(String name, int age, boolean isActive) throws TException {
        System.out.println("Validating user data: name=" + name + ", age=" + age + ", isActive=" + isActive);
        
        if (name == null || name.trim().isEmpty()) {
            throw new org.apache.thrift.TApplicationException(
                org.apache.thrift.TApplicationException.INVALID_MESSAGE_TYPE,
                "Name cannot be null or empty"
            );
        }
        
        if (age < 0 || age > 150) {
            throw new org.apache.thrift.TApplicationException(
                org.apache.thrift.TApplicationException.WRONG_METHOD_NAME,
                "Age must be between 0 and 150"
            );
        }
        
        return "User data is valid: " + name + " (age: " + age + ", active: " + isActive + ")";
    }
}