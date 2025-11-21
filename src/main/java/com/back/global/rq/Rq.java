package com.back.global.rq;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Rq {
    private final HttpServletResponse httpServletResponse;

    public void setCookie(String name, String value) {
        setCookie(name, value, -1); // session cookie
    }

    public void setCookie(String name, String value, int maxAge) {
        if (value == null) value = "";

        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");

        cookie.setMaxAge(maxAge);

        httpServletResponse.addCookie(cookie);
    }

    public void deleteCookie(String name) {
        setCookie(name, null, 0);
    }
}