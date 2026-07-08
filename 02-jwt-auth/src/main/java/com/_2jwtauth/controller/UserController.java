package com._2jwtauth.controller;

import com._2jwtauth.model.User;
import com._2jwtauth.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // get user by id
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // update user
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    // delete user
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public String deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }
}