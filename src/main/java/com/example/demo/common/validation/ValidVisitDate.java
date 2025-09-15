package com.example.demo.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VisitDateValidator.class)
public @interface ValidVisitDate {
    String message() default "Visit date must be at least tomorrow";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}