package com.back.global.globalExceptionHandler;

import com.back.global.exception.AuthException;
import com.back.global.rsData.RsData;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public RsData<Void> handle(AuthException e, HttpServletResponse response) {
        RsData<Void> rsData = e.getRsData();
        response.setStatus(rsData.getStatusCode());
        return rsData;
    }
}