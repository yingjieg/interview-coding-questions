package com.example.demo.common.exception;

/**
 * Centralized enum for all business rule violation error codes.
 * This ensures consistency across the application and prevents typos in error codes.
 */
public enum BusinessRuleCode {

    // User Management Rules
    EMAIL_ALREADY_EXISTS("Email already exists"),

    // Order Management Rules
    INVALID_TICKET_COUNT("Must select exactly 4 tickets"),
    MAX_UNFINISHED_ORDERS_EXCEEDED("User cannot have more than 4 unfinished orders"),
    MISSING_IDEMPOTENCY_KEY("Idempotency-Key header is required for order operations"),

    // Booking Management Rules
    BOOKING_ALREADY_EXISTS("Booking already exists for this order"),
    INVALID_VISIT_DATE("Visit date must be at least tomorrow. Cannot book for today or past dates"),
    ONE_BOOKING_PER_USER_PER_DAY("User already has a booking for this date"),
    CANNOT_UPDATE_PAST_BOOKING("Cannot update booking for past dates"),
    CANNOT_CANCEL_PAST_BOOKING("Cannot cancel booking for past dates"),

    // Ticket Submission Rules
    INVALID_BOOKING_STATUS("Cannot submit tickets for booking that is not confirmed"),
    TICKETS_ALREADY_SUBMITTED("Tickets have already been submitted for this booking"),
    CANNOT_SUBMIT_PAST_TICKETS("Cannot submit tickets for past visit date");

    private final String defaultMessage;

    BusinessRuleCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return this.name();
    }

    public String getDefaultMessage() {
        return this.defaultMessage;
    }
}