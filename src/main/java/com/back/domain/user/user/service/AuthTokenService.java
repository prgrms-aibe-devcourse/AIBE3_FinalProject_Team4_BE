package com.back.domain.user.user.service;

import com.back.domain.user.user.entity.User;
import com.back.standard.jwtUt.JwtUt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthTokenService {
    @Value("${jwt.secret}")
    private String jwtSecretKey;
    @Value("${jwt.access-exp}")
    private int accessTokenExpireSeconds;

    String generateAuthToken(User user) {
        long id = user.getId();
        String username = user.getUsername();

        Map<String, Object> claims = Map.of("id", id, "username", username);

        return JwtUt.toString(
                jwtSecretKey,
                accessTokenExpireSeconds,
                claims
        );
    }

    Map<String, Object> payload(String accessToken) {
        Map<String, Object> payload = JwtUt.payload(jwtSecretKey, accessToken);

        if (payload == null) return null;

        long id = ((Number) payload.get("id")).longValue();
        String username = (String) payload.get("username");

        return Map.of("id", id, "username", username);
    }

}
