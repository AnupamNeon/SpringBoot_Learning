package com._2jwtauth.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * JwtService is responsible for all JWT operations:
 *   - Generating a token after successful login/register
 *   - Extracting claims (username, authorities) from a token
 *   - Validating that a token is genuine and not expired
 *
 * A JWT has three parts: Header.Payload.Signature
 *   Header: algorithm used (HS256)
 *   Payload: claims (subject=username, authorities, iat, exp)
 *   Signature: HMAC of header+payload using the secret key
 *
 * The secret key must be kept private on the server.
 * Anyone with the secret can forge tokens.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    // Token lifetime in milliseconds (set in application.properties, e.g. 86400000 = 24h)
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Converts the plain-text secret from application.properties into a
     * cryptographic key object that the JJWT library can use for signing.
     *
     * HMAC-SHA: a symmetric algorithm — same key signs and verifies.
     * This means only this server can create OR verify tokens.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generates a signed JWT token.
     *
     * The "authorities" claim stores roles and permissions so the JwtFilter
     * can reconstruct the user's GrantedAuthority list on every request
     * WITHOUT hitting the database.
     *
     * This is the stateless JWT design: the token itself carries the state.
     */
    public String generateToken(String username, List<String> authorities) {
        return Jwts.builder()
                .subject(username)
                .claim("authorities", authorities)  // custom claim — carries roles + permissions
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the username from the "sub" (subject) claim.
     * Throws JwtException if the token is tampered with or uses a wrong key.
     */
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Extracts the authorities list from the custom "authorities" claim.
     * The JwtFilter uses this to populate the SecurityContext without a DB call.
     */
    public List<String> extractAuthorities(String token) {
        Object authoritiesObj = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("authorities");

        if (authoritiesObj instanceof List<?> list) {
            return list.stream()
                    .map(Object::toString)
                    .toList();
        }
        return Collections.emptyList();
    }

    /**
     * A token is valid if:
     *   1. The username in the token matches the expected username
     *   2. The token has not expired
     */
    public boolean isTokenValid(String token, String username) {
        String extracted = extractUsername(token);
        return extracted.equals(username) && !isTokenExpired(token);
    }

    // Returns true if the token's expiration date is before now
    private boolean isTokenExpired(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }
}