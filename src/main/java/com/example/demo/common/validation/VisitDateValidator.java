package com.example.demo.common.validation;

import com.example.demo.order.dto.CreatePurchaseDto;
import com.example.demo.common.utils.DateUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

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

        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        LocalDate visitDate = dto.getVisitDate();
        LocalDate tomorrow = DateUtils.tomorrow();

        // Check if visit date is at least tomorrow
        if (visitDate.isBefore(tomorrow)) {
            context.buildConstraintViolationWithTemplate(
                            "Visit date must be at least tomorrow. Cannot book for today or past dates. " +
                                    "Visit date provided: " + visitDate + ", minimum allowed: " + tomorrow
                    )
                    .addPropertyNode("visitDate")
                    .addConstraintViolation();
            isValid = false;
        }

        // Check if document type is provided when visit date is provided
        if (dto.getDocumentType() == null) {
            context.buildConstraintViolationWithTemplate(
                            "Document type is required when visit date is provided"
                    )
                    .addPropertyNode("documentType")
                    .addConstraintViolation();
            isValid = false;
        }

        // Check if document number is provided when visit date is provided
        if (StringUtils.isBlank(dto.getDocumentNumber())) {
            context.buildConstraintViolationWithTemplate(
                            "Document number is required when visit date is provided"
                    )
                    .addPropertyNode("documentNumber")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}