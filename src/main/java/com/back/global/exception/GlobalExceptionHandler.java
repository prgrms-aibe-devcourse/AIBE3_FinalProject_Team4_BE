package com.back.global.exception;

import com.back.global.rsData.RsData;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import java.time.format.DateTimeParseException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

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
                .status(BAD_REQUEST)
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
    public ResponseEntity<RsData<?>> handleUnexpectedException(Exception ex, HttpServletRequest req) {
        log.error("[UnexpectedException] {}", ex.getMessage(), ex);

        if (isSseRequest(req)) {
            // 여기서 RsData 쓰면 바로 No converter ... text/event-stream 터짐
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }

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

    // IllegalArgumentException 처리 (해시태그 검증 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RsData<?>> handleIllegalArgumentException(IllegalArgumentException e) {
        RsData<?> response = RsData.failOf("400-1", e.getMessage());
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(response);
    }

    // 날짜 파싱 및 변환 오류 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RsData<?>> handleParsingAndConversionErrors(HttpMessageNotReadableException ex) {
        Throwable rootCause = ex.getRootCause();    // 내부 예외가 DateTimeParseException인지 확인
        if (rootCause instanceof DateTimeParseException) {
            // 400-DATE: 날짜 파싱 실패 (예: '1999010'처럼 자리수 부족/형식 오류)
            RsData<?> response = RsData.failOf("400-DATE", "생년월일 형식이 유효하지 않습니다. (YYYYMMDD 형식 확인)");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        // 400-JSON: 그 외 JSON 구문 오류나 타입 미스매치
        RsData<?> response = RsData.failOf("400-JSON", "요청 본문(JSON) 형식이 잘못되었습니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<?> handleAsyncTimeout(AsyncRequestTimeoutException ex, HttpServletRequest req) {
        if (isSseRequest(req)) {
            // SSE는 바디 쓰면 안 됨 (이미 커밋됐을 가능성 높음)
            log.warn("[SSE Timeout] path={}", req.getRequestURI());
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
        }

        return ResponseEntity
                .status(HttpStatus.REQUEST_TIMEOUT)
                .body(RsData.failOf("408-1", "요청 시간이 초과되었습니다."));
    }

    private boolean isSseRequest(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        return accept != null && accept.contains("text/event-stream");
    }
}