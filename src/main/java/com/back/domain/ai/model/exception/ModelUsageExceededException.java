package com.back.domain.ai.model.exception;

public class ModelUsageExceededException extends RuntimeException {
    public ModelUsageExceededException(String message) {
        super(message);
    }

    public ModelUsageExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}

