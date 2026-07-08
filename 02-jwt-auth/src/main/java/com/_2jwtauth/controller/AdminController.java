package com._2jwtauth.controller;

import com._2jwtauth.model.Role;
import com._2jwtauth.repository.RoleRepository;
import com._2jwtauth.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")   //  every method requires ROLE_ADMIN
public class AdminController {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public AdminController(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<String> dashboard() {
        return ResponseEntity.ok("Welcome to Admin Dashboard");
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleRepository.findAllRoles());
    }

    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<String> assignRole(@PathVariable Long userId,
                                             @RequestBody Map<String, Long> body) {
        Long roleId = body.get("roleId");
        roleRepository.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok("Role assigned successfully");
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    public ResponseEntity<String> removeRole(@PathVariable Long userId,
                                             @PathVariable Long roleId) {
        roleRepository.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok("Role removed successfully");
    }
}