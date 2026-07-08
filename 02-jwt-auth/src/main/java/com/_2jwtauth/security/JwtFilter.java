package com._2jwtauth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JwtFilter intercepts every HTTP request and validates the JWT token.
 *
 * ─── Where does this filter sit? ─────────────────────────────────────────────
 * The Spring Security filter chain processes requests before they reach controllers.
 * We insert JwtFilter BEFORE UsernamePasswordAuthenticationFilter (configured in
 * SecurityConfig) so our JWT check runs first on every request.
 *
 * ─── What does this filter do? ───────────────────────────────────────────────
 * 1. Reads the "Authorization: Bearer <token>" header
 * 2. Extracts and validates the JWT
 * 3. Extracts authorities from the token (no DB call — stateless design)
 * 4. Creates an Authentication object and puts it in the SecurityContext
 *
 * After this filter runs, Spring Security reads the SecurityContext to decide
 * whether the request is allowed to proceed (@PreAuthorize checks happen later).
 *
 * ─── Why OncePerRequestFilter? ───────────────────────────────────────────────
 * Some servlet containers might invoke filters multiple times per request
 * (e.g., on forward/include). OncePerRequestFilter guarantees our logic runs
 * exactly once per HTTP request.
 *
 * ─── Stateless vs Stateful ───────────────────────────────────────────────────
 * Traditional session-based auth: server stores session state, client sends session ID.
 * JWT auth: server is stateless — all state (who you are, what you can do) lives
 * in the token itself. The server just validates the signature.
 * This is why SessionCreationPolicy.STATELESS is set in SecurityConfig.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Step 1: Read the Authorization header
        // Expected format: "Bearer eyJhbGciOiJIUzI1NiJ9..."
        String authHeader = req.getHeader("Authorization");

        // If no token is present, skip this filter and continue the chain.
        // The request will still be processed — but without authentication.
        // Unauthenticated requests to protected endpoints will be rejected
        // by the security rules in SecurityConfig (anyRequest().authenticated()).
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(req, res);
            return;
        }

        // Step 2: Extract the token (strip "Bearer " prefix — 7 characters)
        String token = authHeader.substring(7);

        // Step 3: Extract the username from the token's "sub" claim
        // extractUsername() also validates the signature — throws JwtException if invalid
        String username = jwtService.extractUsername(token);

        // Step 4: Only authenticate if we have a username AND the request is not
        // already authenticated (prevents processing the token twice)
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Step 5: Validate that the token is not expired and belongs to this username
            if (jwtService.isTokenValid(token, username)) {

                // Step 6: Extract authorities from the token
                // This is the key benefit of embedding authorities in the JWT:
                // we reconstruct the user's permissions WITHOUT a database query.
                List<String> authorityStrings = jwtService.extractAuthorities(token);
                List<SimpleGrantedAuthority> authorities = authorityStrings.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                // Step 7: Create an authenticated token and set it in the SecurityContext.
                //
                // UsernamePasswordAuthenticationToken(principal, credentials, authorities)
                //   principal   = username (who is making the request)
                //   credentials = null (we don't need the password after authentication)
                //   authorities = the list we just built from the JWT
                //
                // Passing authorities in the constructor marks this token as authenticated.
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                // Attach request details (IP address, session ID) — useful for audit logging
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                // Set the authenticated token into the SecurityContext.
                // From this point on, Spring Security treats this request as authenticated.
                // @PreAuthorize annotations will check the authorities in this token.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step 8: Continue the filter chain — the request proceeds to the controller
        filterChain.doFilter(req, res);
    }
}