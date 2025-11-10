package com.back.standard.jwtUt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Map;

public class JwtUt {

    public static String toString(String secret, int expireSeconds, Map<String, Object> body) {
        ClaimsBuilder claimsBuilder = Jwts.claims();

        for (Map.Entry<String, Object> entry : body.entrySet()) {
            claimsBuilder.add(entry.getKey(), entry.getValue());
        }

        Claims claims = claimsBuilder.build();

        Date issuedAt = new Date();     // 발급 시간
        Date expiration = new Date(issuedAt.getTime() + 1000L * expireSeconds);     // 만료 시간
        Key secretKey = Keys.hmacShaKeyFor(secret.getBytes());      // 서명 키

        return Jwts.builder()
                .claims(claims)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public static boolean isValid(String secret, String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

        try {
            Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(token);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static Map<String, Object> payload(String secret, String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

        try {
            return (Map<String, Object>) Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}