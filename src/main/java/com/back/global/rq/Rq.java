package com.back.global.rq;

import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Rq {
    private final HttpServletRequest httpServletRequest;
    private final UserService userService;

    public User getActor() {
        String headerAuthorization = httpServletRequest.getHeader("Authorization");

        if(headerAuthorization == null || headerAuthorization.isEmpty()) {
            throw new RuntimeException("Authorization 헤더가 없습니다.");
        }

        if(!headerAuthorization.startsWith("Bearer ")) {
            throw new RuntimeException("유효하지 않은 Authorization 헤더입니다.");
        }

        String apiKey = headerAuthorization.substring("Bearer".length()).trim();

        return userService.getUserByApiKey(apiKey);
    }
}