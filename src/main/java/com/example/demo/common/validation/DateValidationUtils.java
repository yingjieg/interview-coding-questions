package com.example.demo.common.validation;

import com.example.demo.common.exception.BusinessRuleCode;
import com.example.demo.common.exception.BusinessRuleViolationException;
import com.example.demo.common.utils.DateUtils;

import java.time.LocalDate;

public final class DateValidationUtils {

    private DateValidationUtils() {
        // Utility class - prevent instantiation
    }

    public static void validateVisitDateIsAtLeastTomorrow(LocalDate visitDate) {
        if (visitDate == null) {
            throw new IllegalArgumentException("Visit date cannot be null");
        }

        if (visitDate.isBefore(DateUtils.tomorrow())) {
            throw new BusinessRuleViolationException(BusinessRuleCode.INVALID_VISIT_DATE);
        }
    }

    public static void validateDateIsNotInPastForUpdate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new BusinessRuleViolationException(BusinessRuleCode.CANNOT_UPDATE_PAST_BOOKING);
        }
    }

    public static void validateDateIsNotInPastForCancel(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new BusinessRuleViolationException(BusinessRuleCode.CANNOT_CANCEL_PAST_BOOKING);
        }
    }
}