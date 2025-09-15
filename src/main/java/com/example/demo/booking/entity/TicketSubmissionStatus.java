package com.example.demo.booking.entity;

public enum TicketSubmissionStatus {
    NOT_SUBMITTED,  // Initial state - tickets not submitted yet
    SUBMITTED,      // Tickets successfully submitted to external service
    FAILED,         // Submission failed, will retry
    EXPIRED         // Visit date passed, no longer valid
}