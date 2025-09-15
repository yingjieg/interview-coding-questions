package com.example.demo.common.exception;

public class IdempotencyException extends BusinessException {

    public IdempotencyException(String message) {
        super("IDEMPOTENCY_ERROR", message);
    }

    public IdempotencyException(String message, Throwable cause) {
        super("IDEMPOTENCY_ERROR", message, cause);
    }
}