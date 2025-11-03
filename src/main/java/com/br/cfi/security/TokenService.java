package com.br.cfi.security;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.br.cfi.entity.User;

import jakarta.annotation.PostConstruct;

@Service
public class TokenService {

    private static final String ISSUER = "api-nj";

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(user.getUsername())
                    .withExpiresAt(Date.from(LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"))))
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new RuntimeException("Error generating token", e);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            return "";
        }
    }

    @PostConstruct
    public void checkSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("api.security.token.secret não definido!");
        }
    }

    public void invalidateToken(String token) {
        throw new UnsupportedOperationException("Unimplemented method 'invalidateToken'");
    }
}

//a