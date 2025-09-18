package com.example.demo.payment.entity;

/**
 * Payment status for tracking PayPal payment lifecycle
 */
public enum PaymentStatus {

    /**
     * No payment initiated yet - order created but payment not started
     */
    PENDING,

    /**
     * Payment created in PayPal but user hasn't approved yet
     */
    PAYMENT_CREATED,

    /**
     * User approved payment, ready to capture
     */
    PAYMENT_APPROVED,

    /**
     * Payment successfully captured and completed
     */
    PAYMENT_COMPLETED,

    /**
     * Payment failed during processing
     */
    PAYMENT_FAILED,

    /**
     * Payment was cancelled by user or system
     */
    PAYMENT_CANCELLED,

    /**
     * Payment was refunded (future use)
     */
    PAYMENT_REFUNDED
}