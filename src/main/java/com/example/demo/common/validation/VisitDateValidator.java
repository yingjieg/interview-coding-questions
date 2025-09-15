package com.example.demo.common.validation;

import com.example.demo.order.dto.CreatePurchaseDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class VisitDateValidator implements ConstraintValidator<ValidVisitDate, CreatePurchaseDto> {

    @Override
    public void initialize(ValidVisitDate constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(CreatePurchaseDto dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getVisitDate() == null) {
            return true; // Let other validators handle null values
        }

        LocalDate visitDate = dto.getVisitDate();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        if (visitDate.isBefore(tomorrow)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Visit date must be at least tomorrow. Cannot book for today or past dates. " +
                "Visit date provided: " + visitDate + ", minimum allowed: " + tomorrow
            )
            .addPropertyNode("visitDate")
            .addConstraintViolation();
            return false;
        }

        return true;
    }
}