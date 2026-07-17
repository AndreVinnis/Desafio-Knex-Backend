package com.uepb.DesafioKnex.security;

import com.uepb.DesafioKnex.model.User;
import com.uepb.DesafioKnex.model.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(
            @Value("${JWT_SECRET}") String secret,
            @Value("${JWT_EXPIRATION_MS}") long expirationMinutes
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMillis = expirationMinutes * 60 * 1000;
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("roles", List.of(user.getRole().toString()))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractUserId(String token) {
        return parseClaims(token).get("userId", String.class);
    }

    public long getExpirationSeconds() {
        return expirationMillis / 1000;
    }

    @SuppressWarnings("unchecked")
    public Set<Role> extractRoles(String token) {
        List<String> roleNames = parseClaims(token).get("roles", List.class);
        return roleNames.stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }
}