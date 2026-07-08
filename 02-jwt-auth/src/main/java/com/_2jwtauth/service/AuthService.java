package com._2jwtauth.service;

import com._2jwtauth.model.Permission;
import com._2jwtauth.model.Role;
import com._2jwtauth.model.User;
import com._2jwtauth.repository.RoleRepository;
import com._2jwtauth.repository.UserRepository;
import com._2jwtauth.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AuthService handles two operations:
 *   1. register() — creates a new user account and returns a JWT
 *   2. login()    — verifies credentials and returns a JWT
 *
 * ─── Why separate AuthService from UserService? ───────────────────────────
 * UserService handles CRUD operations on user data (read, update, delete).
 * AuthService handles the authentication ceremony (who are you? prove it).
 * Keeping them separate follows the Single Responsibility Principle and
 * makes both classes easier to understand and test.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new user and returns a JWT so they are immediately logged in.
     *
     * Steps:
     *   1. Hash the plain-text password (NEVER store plain text)
     *   2. Mark the account as enabled
     *   3. Persist the user (DB generates and returns the ID)
     *   4. Assign the default ROLE_USER (role id=2 in DB)
     *   5. Load the assigned roles+permissions and flatten into authority strings
     *   6. Generate a signed JWT containing those authorities
     */
    public Map<String, String> register(User user) {
        // Step 1+2: encode password and activate account
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);

        // Step 3: persist — addUser() sets user.getId() from the generated key
        userRepository.addUser(user);

        // Step 4: assign default role (ROLE_USER, id=2)
        roleRepository.assignRoleToUser(user.getId(), 2L);

        // Step 5: load roles+permissions from DB so the token carries correct authorities
        List<Role> roles = roleRepository.findRolesByUserId(user.getId());
        List<String> authorities = flattenAuthorities(roles);

        // Step 6: generate JWT
        String token = jwtService.generateToken(user.getUsername(), authorities);

        return Map.of("token", token, "username", user.getUsername());
    }

    /**
     * Authenticates a user and returns a JWT.
     *
     * ─── Why use AuthenticationManager instead of manual password check? ──
     *
     * authenticationManager.authenticate() does ALL of the following:
     *   1. Calls CustomUserDetailsService.loadUserByUsername() to fetch the user
     *   2. Verifies the password against the stored BCrypt hash
     *   3. Checks that the account is enabled, not locked, not expired
     *   4. Throws BadCredentialsException if any check fails
     *
     * Doing this manually (userRepository.findByUsername + passwordEncoder.matches)
     * skips checks 3 and 4, which matters for account security.
     *
     * On success, the returned Authentication object already contains the
     * GrantedAuthority list that CustomUserDetailsService built — so we
     * extract authorities from there rather than querying the DB again.
     */
    public Map<String, String> login(String username, String password) {
        // This will throw AuthenticationException (→ BadCredentialsException)
        // if credentials are wrong. Spring maps that to 401 automatically
        // when it comes from the security filter, but here we catch it at the
        // service layer and re-throw as IllegalArgumentException so our
        // GlobalExceptionHandler returns the correct 401 response.
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
                    // ↑ This is an "unauthenticated" token — just a container for credentials.
                    // AuthenticationManager will validate it and return an authenticated token.
            );

            // Extract authority strings from the authenticated principal
            // (these came from CustomUserDetailsService — no extra DB call needed)
            List<String> authorities = authentication.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            String token = jwtService.generateToken(username, authorities);

            return Map.of("token", token, "username", username);

        } catch (Exception ex) {
            // BadCredentialsException, DisabledException, LockedException, etc.
            // all become 401 via our GlobalExceptionHandler
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

    /**
     * Converts the Role/Permission object graph into a flat list of strings.
     *
     * Spring Security works with GrantedAuthority which is just a string wrapper.
     * The JWT "authorities" claim stores strings, not objects.
     *
     * Result example: ["ROLE_ADMIN", "EMPLOYEE_READ", "EMPLOYEE_WRITE", "USER_READ"]
     *
     * ─── Why include both roles and permissions? ──────────────────────────
     * @PreAuthorize("hasRole('ADMIN')")        checks for "ROLE_ADMIN"  ← role
     * @PreAuthorize("hasAuthority('EMPLOYEE_READ')")  checks for "EMPLOYEE_READ" ← permission
     *
     * Both types must be in the authority list for both annotation styles to work.
     */
    private List<String> flattenAuthorities(List<Role> roles) {
        List<String> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add(role.getName());                     // e.g. "ROLE_ADMIN"
            for (Permission perm : role.getPermissions()) {
                authorities.add(perm.getName());                 // e.g. "EMPLOYEE_READ"
            }
        }
        return authorities;
    }
}