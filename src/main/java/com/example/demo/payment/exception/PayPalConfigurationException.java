package com.example.demo.payment.exception;

import com.example.demo.common.exception.BusinessException;

/**
 * Exception thrown when PayPal configuration is invalid or missing.
 * This includes missing credentials, invalid URLs, or configuration errors.
 */
public class PayPalConfigurationException extends BusinessException {

    public PayPalConfigurationException(String message) {
        super("PAYPAL_CONFIG_ERROR", message);
    }

    public PayPalConfigurationException(String message, Throwable cause) {
        super("PAYPAL_CONFIG_ERROR", message, cause);
    }

    public PayPalConfigurationException(String configField, String issue) {
        super("PAYPAL_CONFIG_ERROR",
              String.format("PayPal configuration error - %s: %s", configField, issue),
              configField, issue);
    }
}