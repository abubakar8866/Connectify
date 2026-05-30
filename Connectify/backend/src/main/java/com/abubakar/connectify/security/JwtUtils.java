package com.abubakar.connectify.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.abubakar.connectify.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt_initial_secret_key}")
    private String jwtSecret;

    @Value("${jwt_expiration}")
    private long jwtExpiration;

    // ===================== EXTRACT =====================

    public String extractEmail(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    private <T> T extractClaims(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        Jwt<?, Claims> jwt = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);

        return jwt.getPayload();
    }

    // ===================== VALIDATION =====================

    public boolean isTokenValid(String token, User user) {
        try {
            String email = extractEmail(token);
            boolean valid = email.equals(user.getEmail()) && !isTokenExpired(token);

            logger.info("Token validation result for user {} : {}", user.getEmail(), valid);

            return valid;

        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateToken(String token) {
        try {
            boolean expired = isTokenExpired(token);

            logger.info("Token validated | Expired: {}", expired);

            return !expired;

        } catch (Exception e) {
            logger.error("Invalid JWT Token: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    // ===================== GENERATE =====================

    public String generateToken(User user) {
        return createToken(new HashMap<>(), user);
    }

    private String createToken(Map<String, Object> claims, User user) {

        logger.info("Generating token for user: {}", user.getEmail());

        return Jwts.builder()
                .claims(claims)
                .claim("role", user.getRole().name())
                .claim("userId", user.getId())
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // ===================== KEY =====================

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}

