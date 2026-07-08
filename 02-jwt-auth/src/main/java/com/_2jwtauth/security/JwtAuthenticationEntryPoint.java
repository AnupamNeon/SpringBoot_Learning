package com._2jwtauth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JwtAuthenticationEntryPoint handles requests that reach a protected endpoint
 * WITHOUT a valid authentication token.
 *
 * ─── When is this called? ────────────────────────────────────────────────────
 * This is NOT called when @PreAuthorize fails (that's AccessDeniedException → 403).
 * This IS called when:
 *   - No Authorization header is present
 *   - The JWT is invalid or expired
 *   - The request is simply unauthenticated
 *
 * Spring Security calls commence() and we tell it to respond with 401.
 *
 * ─── Why not just let the filter return 401 directly? ────────────────────────
 * JwtFilter calls filterChain.doFilter() even when no token is present.
 * Spring Security needs a configured entry point to know WHAT to do
 * when an unauthenticated request reaches a protected resource.
 * Without this, Spring would redirect to a login form (default behavior).
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest req,
                         HttpServletResponse res,
                         AuthenticationException authException) throws IOException {
        // Send HTTP 401 with a plain message.
        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + authException.getMessage());
    }
}