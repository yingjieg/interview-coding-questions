package com.example.demo.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("timestamp", Instant.now());

        if (ex.getArgs() != null && ex.getArgs().length > 0) {
            problemDetail.setProperty("entityType", ex.getArgs()[0]);
            if (ex.getArgs().length > 1) {
                problemDetail.setProperty("entityId", ex.getArgs()[1]);
            }
        }

        return problemDetail;
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ProblemDetail handleBusinessRuleViolation(BusinessRuleViolationException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("timestamp", Instant.now());

        if (ex.getArgs() != null && ex.getArgs().length > 0) {
            problemDetail.setProperty("rule", ex.getArgs()[0]);
        }

        return problemDetail;
    }

    @ExceptionHandler(IdempotencyException.class)
    public ProblemDetail handleIdempotencyException(IdempotencyException ex) {
        log.warn("Idempotency error: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ProblemDetail handleExternalServiceException(ExternalServiceException ex) {
        log.error("External service error: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_GATEWAY, "External service temporarily unavailable");
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("timestamp", Instant.now());

        if (ex.getArgs() != null && ex.getArgs().length > 0) {
            problemDetail.setProperty("serviceName", ex.getArgs()[0]);
        }

        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
            .collect(Collectors.joining(", "));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Validation failed: " + errors);
        problemDetail.setProperty("errorCode", "VALIDATION_ERROR");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("validationErrors",
            ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage(),
                    (existing, replacement) -> existing
                )));

        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problemDetail.setProperty("errorCode", "INTERNAL_ERROR");
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
}