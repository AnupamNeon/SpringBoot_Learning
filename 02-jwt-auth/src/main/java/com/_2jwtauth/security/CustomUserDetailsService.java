package com._2jwtauth.security;

import com._2jwtauth.model.Permission;
import com._2jwtauth.model.Role;
import com._2jwtauth.model.User;
import com._2jwtauth.repository.RoleRepository;
import com._2jwtauth.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * CustomUserDetailsService is the bridge between YOUR user data (stored in the DB)
 * and Spring Security's authentication system.
 *
 * ─── The Contract ────────────────────────────────────────────────────────────
 * Spring Security doesn't know about YOUR User class or YOUR database schema.
 * It only knows about the UserDetails interface.
 *
 * By implementing UserDetailsService, we tell Spring Security:
 *   "When you need to load a user by username, call MY loadUserByUsername() method."
 *
 * ─── When is loadUserByUsername() called? ────────────────────────────────────
 * It is called by AuthenticationManager.authenticate() inside AuthService.login().
 * It is NOT called on every request — JwtFilter handles subsequent requests
 * by reading the token directly without touching the database.
 *
 * ─── Users, Roles, Authorities, and Permissions ──────────────────────────────
 *
 * User        → has many Roles        (via user_roles table)
 * Role        → has many Permissions  (via role_permissions table)
 * Permission  → a fine-grained action like "EMPLOYEE_READ"
 *
 * Spring Security flattens ALL of these into a single concept: GrantedAuthority.
 * A GrantedAuthority is just a string.
 *
 * So for a user with ROLE_ADMIN (which has EMPLOYEE_READ, EMPLOYEE_WRITE):
 *   authorities = ["ROLE_ADMIN", "EMPLOYEE_READ", "EMPLOYEE_WRITE"]
 *
 * @PreAuthorize("hasRole('ADMIN')")           checks for "ROLE_ADMIN"
 * @PreAuthorize("hasAuthority('EMPLOYEE_READ')") checks for "EMPLOYEE_READ"
 *
 * hasRole() automatically prepends "ROLE_" — hasAuthority() does not.
 * This is why roles in the DB must be stored as "ROLE_ADMIN", not "ADMIN".
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CustomUserDetailsService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Step 1: Load the user record from the database
        User user = userRepository.findByUsername(username);
        if (user == null) {
            // UsernameNotFoundException is a Spring Security exception.
            // AuthenticationManager catches it and converts it to BadCredentialsException
            // (to avoid revealing whether the username or password was wrong).
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // Step 2: Load roles and their permissions for this user
        List<Role> roles = roleRepository.findRolesByUserId(user.getId());

        // Step 3: Convert roles + permissions into GrantedAuthority objects
        // Spring Security only understands GrantedAuthority — not Role or Permission.
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            // Role becomes an authority: "ROLE_ADMIN", "ROLE_USER", etc.
            authorities.add(new SimpleGrantedAuthority(role.getName()));

            for (Permission perm : role.getPermissions()) {
                // Permission becomes an authority: "EMPLOYEE_READ", "USER_DELETE", etc.
                authorities.add(new SimpleGrantedAuthority(perm.getName()));
            }
        }

        // Step 4: Return Spring Security's UserDetails implementation.
        // We use the built-in User class from Spring Security (not our own User model).
        //
        // Parameters: username, password, enabled, accountNonExpired,
        //             credentialsNonExpired, accountNonLocked, authorities
        //
        // For learning purposes all account status flags are true.
        // In production you would store these in the DB and read them here.
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),   // enabled
                true,               // accountNonExpired
                true,               // credentialsNonExpired
                true,               // accountNonLocked
                authorities
        );
    }
}