package com.back.global.rq;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Rq {
    private final HttpServletResponse httpServletResponse;

    public void setCookie(String name, String value) {
        if (value == null) value = "";

        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
//                .domain(".textok.site")
                .path("/")
                .maxAge(value.isBlank() ? 0 : -1)
                .build();

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void setCookie(String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .maxAge(maxAge)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .build();

        httpServletResponse.addHeader("Set-Cookie", cookie.toString());
    }

    public void deleteCookie(String name) {
        setCookie(name, null);
    }
}