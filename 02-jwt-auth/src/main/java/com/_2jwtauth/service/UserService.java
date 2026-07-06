package com._2jwtauth.service;

import com._2jwtauth.model.User;
import com._2jwtauth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long id) {
        return userRepository.getUserById(id);
    }

    public User addUser(User user) {
        return userRepository.addUser(user);
    }

    public User updateUser(Long id, User user) {
        return userRepository.updateUser(id, user);
    }

    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public String deleteUser(Long id) {
        return userRepository.deleteUser(id);
    }
}