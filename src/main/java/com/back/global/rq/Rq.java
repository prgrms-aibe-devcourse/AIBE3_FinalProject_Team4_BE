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
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Rq {
    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;
    private final UserService userService;

    public User getActor() {
        String headerAuthorization = httpServletRequest.getHeader("Authorization");
        String refreshToken;
        String accessToken;

        // 헤더에 Authorization이 존재하면 사용하고
        if (headerAuthorization != null && !headerAuthorization.isBlank()) {
            if(!headerAuthorization.startsWith("Bearer ")) {
                throw new AuthException("401-2", "유효하지 않은 Authorization 헤더입니다.");
            }
            String[] headerAuthorizations = headerAuthorization.split(" ", 3);
            refreshToken = headerAuthorizations[1];
            accessToken = headerAuthorizations.length == 3 ? headerAuthorizations[2] : "";
        } else {    // 존재하지 않으면 쿠키에서 apiKey를 찾음
            refreshToken = getCookieValue("apiKey");
            accessToken = getCookieValue("accessToken");
        }

        // refreshToken 없으면 인증 실패
        if (refreshToken.isBlank()) throw new AuthException("401-1", "로그인 후 사용해주세요");

        User user = null;

        // accessToken이 존재하면 payload에서 사용자 정보를 가져옴
        if (!accessToken.isBlank()) {
            Map<String, Object> payload = userService.getPayload(accessToken);
            if(payload != null) {
                long userId = ((Number) payload.get("id")).longValue();
                String username = (String) payload.get("username");
                user = new User(userId, username);
            }
        }

        if(!accessToken.isBlank())

        user = userService.getUserRefreshToken(refreshToken);

        return user;
    }

    public String getCookieValue(String name) {
        return Arrays.stream(httpServletRequest.getCookies())
                .filter(cookie -> cookie.getName().equals(name))
                .map(Cookie::getValue)
                .findFirst().orElse("");
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