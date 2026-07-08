// ===== config/SecurityConfig.java =====

package com._2jwtauth.config;

import com._2jwtauth.security.JwtAuthenticationEntryPoint;
import com._2jwtauth.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig is the central configuration for Spring Security.
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 * COMPLETE REQUEST FLOW — from login to accessing a protected endpoint
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * ── STEP A: Login (/auth/login) ──────────────────────────────────────────────
 *
 *  Client ──POST /auth/login {username, password}──► AuthController.login()
 *         └─► AuthService.login()
 *               └─► AuthenticationManager.authenticate(UsernamePasswordAuthenticationToken)
 *                     └─► CustomUserDetailsService.loadUserByUsername(username)
 *                           └─► UserRepository.findByUsername()       [DB call]
 *                           └─► RoleRepository.findRolesByUserId()    [DB call]
 *                           └─► returns UserDetails with authorities
 *                     └─► BCryptPasswordEncoder.matches(rawPw, hashedPw)
 *                     └─► returns authenticated Authentication object
 *               └─► JwtService.generateToken(username, authorities)
 *               └─► returns {token, username}
 *  Client ◄─── 200 OK {token: "eyJ..."}
 *
 * ── STEP B: Accessing a Protected Endpoint (/employees) ──────────────────────
 *
 *  Client ──GET /employees {Authorization: Bearer eyJ...}──► JwtFilter
 *         └─► JwtService.extractUsername(token)        [no DB call]
 *         └─► JwtService.isTokenValid(token, username) [no DB call]
 *         └─► JwtService.extractAuthorities(token)     [no DB call]
 *         └─► SecurityContextHolder.setAuthentication(authToken)
 *               └─► EmployeeController.getAllEmployees()
 *                     └─► @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
 *                           checks SecurityContext authorities
 *                     └─► EmployeeService.getAllEmployees()
 *                     └─► EmployeeRepository.getAllEmployees()        [DB call]
 *  Client ◄─── 200 OK [list of employees]
 *
 * ── STEP C: Unauthorized Access Attempt ──────────────────────────────────────
 *
 *  Client ──GET /employees (no token)──► JwtFilter (skips, no header)
 *         └─► Security checks: no Authentication in SecurityContext
 *         └─► JwtAuthenticationEntryPoint.commence()
 *  Client ◄─── 401 Unauthorized
 *
 * ── STEP D: Forbidden Access (wrong role) ────────────────────────────────────
 *
 *  Client ──GET /admin/dashboard (ROLE_USER token)──► JwtFilter (valid token)
 *         └─► SecurityContext has Authentication with ["ROLE_USER", ...]
 *         └─► @PreAuthorize("hasRole('ADMIN')") → FAILS
 *         └─► AccessDeniedException
 *         └─► GlobalExceptionHandler.handleAccessDenied()
 *  Client ◄─── 403 Forbidden
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * @EnableWebSecurity  — disables Spring Boot's auto-configured security defaults
 *                       and lets us define our own SecurityFilterChain
 * @EnableMethodSecurity — activates @PreAuthorize and @PostAuthorize on methods
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          JwtFilter jwtFilter) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (Cross-Site Request Forgery) protection.
                // CSRF attacks rely on browser cookies to send credentials automatically.
                // With JWT (sent as a header, not a cookie) CSRF is not applicable.
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // /auth/register and /auth/login must be public — no token required yet
                        .requestMatchers("/auth/**").permitAll()
                        // Every other endpoint requires a valid JWT
                        .anyRequest().authenticated()
                )

                // Use our custom entry point for 401 responses
                // (instead of Spring's default redirect to /login)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // STATELESS: Spring will not create or use HTTP sessions.
                // Each request must carry its own JWT. There is no "remember me" server state.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Insert our JwtFilter before the default username/password filter.
                // This means JWT validation runs first on every request.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCryptPasswordEncoder hashes passwords with a random salt.
     * Even if two users have the same password, their hashes are different.
     * The strength (work factor) defaults to 10 — takes ~100ms to hash.
     * This makes brute-force attacks computationally expensive.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager is the entry point for programmatic authentication.
     * AuthService.login() calls authenticationManager.authenticate() to
     * trigger the full Spring Security authentication pipeline.
     *
     * We get it from AuthenticationConfiguration which auto-configures it
     * based on the UserDetailsService and PasswordEncoder beans we've defined.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}