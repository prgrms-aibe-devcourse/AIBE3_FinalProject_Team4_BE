package com.back.global.rq;

import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.service.UserService;
import com.back.global.exception.AuthException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class Rq {
    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;
    private final UserService userService;

    public User getActor() {
        String headerAuthorization = httpServletRequest.getHeader("Authorization");
        String apiKey;

        // 헤더에 Authorization이 존재하면 사용하고
        if (headerAuthorization != null && !headerAuthorization.isBlank()) {
            if(!headerAuthorization.startsWith("Bearer ")) {
                throw new AuthException("401-2", "유효하지 않은 Authorization 헤더입니다.");
            }
            apiKey = headerAuthorization.substring("Bearer".length()).trim();
        } else {    // 존재하지 않으면 쿠키에서 apiKey를 찾음
            apiKey = httpServletRequest.getCookies() == null ? "" :
                    Arrays.stream(httpServletRequest.getCookies())
                            .filter(cookie -> cookie.getName().equals("apiKey"))
                            .map(Cookie::getValue)
                            .findFirst().orElse("");
        }

        if (apiKey.isBlank()) {
            throw new AuthException("401-1", "로그인 후 사용해주세요");
        }

        return userService.getUserByApiKey(apiKey);
    }

    public void setCookie(String name, String value) {
        if(value == null) value = "";

        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        if (value.isBlank()) {
            cookie.setMaxAge(0);
        }

        httpServletResponse.addCookie(cookie);
    }

    public void deleteCookie(String name) {
        setCookie(name, null);
    }
}