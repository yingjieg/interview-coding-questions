package com.example.demo.booking.service;

public record TicketSubmissionResult(
        boolean success,
        String message,
        SubmissionType type
) {

    public enum SubmissionType {
        NOT_REQUIRED,
        IMMEDIATE_SUCCESS,
        IMMEDIATE_FAILURE,
        SCHEDULED
    }

    public static TicketSubmissionResult notRequired() {
        return new TicketSubmissionResult(true,
                "Tickets will be submitted 24 hours before visit",
                SubmissionType.NOT_REQUIRED);
    }

    public static TicketSubmissionResult immediateSuccess() {
        return new TicketSubmissionResult(true,
                "Purchase, booking confirmed, and tickets submitted successfully (ready for tomorrow's visit)",
                SubmissionType.IMMEDIATE_SUCCESS);
    }

    public static TicketSubmissionResult immediateFailure() {
        return new TicketSubmissionResult(false,
                "Purchase and booking confirmed, but ticket submission failed (will retry automatically)",
                SubmissionType.IMMEDIATE_FAILURE);
    }

    public static TicketSubmissionResult scheduled() {
        return new TicketSubmissionResult(true,
                "Purchase and booking confirmed successfully (tickets will be submitted 24 hours before visit)",
                SubmissionType.SCHEDULED);
    }
}