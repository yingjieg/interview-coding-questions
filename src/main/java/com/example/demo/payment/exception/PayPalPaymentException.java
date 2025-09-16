package com.example.demo.payment.exception;

import com.example.demo.common.exception.BusinessException;

/**
 * Exception thrown when PayPal payment operations fail.
 * This includes payment creation, capture, and status checking failures.
 */
public class PayPalPaymentException extends BusinessException {

    public PayPalPaymentException(String message) {
        super("PAYPAL_PAYMENT_ERROR", message);
    }

    public PayPalPaymentException(String message, Throwable cause) {
        super("PAYPAL_PAYMENT_ERROR", message, cause);
    }

    public PayPalPaymentException(String operation, String orderId, Throwable cause) {
        super("PAYPAL_PAYMENT_ERROR",
              String.format("PayPal %s failed for order %s: %s", operation, orderId, cause.getMessage()),
              cause, operation, orderId);
    }
}