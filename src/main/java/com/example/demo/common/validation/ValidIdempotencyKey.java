package com.example.demo.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IdempotencyKeyValidator.class)
public @interface ValidIdempotencyKey {
    String message() default "Idempotency key must be 10-50 characters containing only alphanumeric characters, hyphens, and underscores";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}