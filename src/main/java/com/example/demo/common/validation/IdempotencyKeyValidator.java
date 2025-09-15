package com.example.demo.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IdempotencyKeyValidator implements ConstraintValidator<ValidIdempotencyKey, String> {

    private static final String VALID_PATTERN = "^[a-zA-Z0-9_-]{10,50}$";

    @Override
    public void initialize(ValidIdempotencyKey constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Idempotency key is required")
                .addConstraintViolation();
            return false;
        }

        if (!value.matches(VALID_PATTERN)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Idempotency key must be 10-50 characters containing only letters, numbers, hyphens, and underscores. " +
                "Provided key length: " + value.length()
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}