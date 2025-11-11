package com.back.global.exception;

import com.back.global.rsData.RsData;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ✅ 서비스 예외 처리
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<RsData<?>> handleServiceException(ServiceException e) {
        return ResponseEntity
                .status(e.getErrorCase().getHttpStatus())
                .body(e.toRsData());
    }

    /**
     * ✅ 유효성 검사 실패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<?>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(ObjectError::getDefaultMessage)
                .orElse("유효성 검사 실패: 잘못된 요청입니다.");

        RsData<?> response = RsData.failOf("400-1", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * ✅ 인증 실패 처리
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<RsData<?>> handleAuthenticationException(AuthenticationException e) {
        RsData<?> response = RsData.failOf("401-1", "로그인 후 이용해주세요.");
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    /**
     * ✅ 알 수 없는 서버 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RsData<?>> handleUnexpectedException(Exception ex) {
        log.error("[UnexpectedException] {}", ex.getMessage(), ex);
        RsData<?> response = RsData.failOf("500-1", "서버 내부 오류가 발생했습니다.");
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<RsData<Void>> handle(AuthException e) {
        RsData<Void> rsData = e.getRsData();
        return ResponseEntity.status(rsData.statusCode()).body(rsData);
    }
}
