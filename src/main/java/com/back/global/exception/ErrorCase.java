package com.back.global.exception;

public interface ErrorCase {

    Integer getHttpStatusCode();

    Integer getErrorCode();

    String getMessage();
}
