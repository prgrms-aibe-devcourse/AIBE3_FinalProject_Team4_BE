package com.back.project.global.handler;

import com.back.project.global.exception.ServiceException;
import com.back.project.global.rsData.RsData;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<RsData<Void>> handle(NoSuchElementException e) {
        return new ResponseEntity<>(
                new RsData<>(
                        "404-1",
                        "존재하지 않는 데이터에 접근했습니다."
                ),
                NOT_FOUND
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handle(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getAllErrors()
                .stream()
                .filter(error -> error instanceof FieldError)
                .map(error -> (FieldError) error)
                .map(error -> error.getField() + "-" + error.getCode() + "-" + error.getDefaultMessage())
                .sorted(Comparator.comparing(String::toString))
                .collect(Collectors.joining("\n"));

        return new ResponseEntity<>(
                new RsData<>(
                        "400-1",
                        message
                ),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RsData<Void>> handle(HttpMessageNotReadableException e) {

        return new ResponseEntity<>(
                new RsData<>(
                        "400-1",
                        "요청 본문 형식이 올바르지 않습니다."
                ),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(BAD_REQUEST)
    public ResponseEntity<RsData<Void>> handle(ServiceException e) {
        RsData<Void> rsData = e.getRsData();

        return new ResponseEntity<>(
                rsData,
                ResponseEntity
                        .status(rsData.statusCode())
                        .build()
                        .getStatusCode()
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<RsData<Void>> handle(MissingRequestHeaderException e) {

        return new ResponseEntity<>(
                new RsData<>(
                        "400-1",
                        "회원정보를 찾을 수 없습니다."
                ),
                BAD_REQUEST
        );
    }

    // IllegalArgumentException 처리 (해시태그 검증 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RsData<Void>> handle(IllegalArgumentException e) {
        return new ResponseEntity<>(
                new RsData<>(
                        "400-1",
                        e.getMessage()
                ),
                BAD_REQUEST
        );
    }
}
