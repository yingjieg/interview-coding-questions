package com.example.demo.common.exception;

import com.example.demo.payment.exception.PayPalConfigurationException;
import com.example.demo.payment.exception.PayPalPaymentException;
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

    @ExceptionHandler(RecordNotFoundException.class)
    public ProblemDetail handleRecordNotFound(RecordNotFoundException ex) {
        log.warn("Database record not found: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("timestamp", Instant.now());

        if (ex.getArgs() != null && ex.getArgs().length > 0) {
            problemDetail.setProperty("recordType", ex.getArgs()[0]);
            if (ex.getArgs().length > 1) {
                problemDetail.setProperty("recordId", ex.getArgs()[1]);
            }
        }

        return problemDetail;
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException ex) {
        log.warn("Business exception: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("timestamp", Instant.now());

        if (ex.getArgs() != null && ex.getArgs().length > 0) {
            problemDetail.setProperty("details", ex.getArgs()[0]);
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

    @ExceptionHandler(PayPalPaymentException.class)
    public ProblemDetail handlePayPalPaymentException(PayPalPaymentException ex) {
        log.error("PayPal payment error: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("timestamp", Instant.now());

        if (ex.getArgs() != null && ex.getArgs().length > 0) {
            problemDetail.setProperty("operation", ex.getArgs()[0]);
            if (ex.getArgs().length > 1) {
                problemDetail.setProperty("orderId", ex.getArgs()[1]);
            }
        }

        return problemDetail;
    }

    @ExceptionHandler(PayPalConfigurationException.class)
    public ProblemDetail handlePayPalConfigurationException(PayPalConfigurationException ex) {
        log.error("PayPal configuration error: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "PayPal payment system is not properly configured");
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("timestamp", Instant.now());

        if (ex.getArgs() != null && ex.getArgs().length > 0) {
            problemDetail.setProperty("configField", ex.getArgs()[0]);
            if (ex.getArgs().length > 1) {
                problemDetail.setProperty("issue", ex.getArgs()[1]);
            }
        }

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