package com.example.demo.payment.entity;

/**
 * Supported payment methods for orders
 */
public enum PaymentType {

    /**
     * PayPal payment processing
     */
    PAYPAL("PayPal"),

    /**
     * Stripe payment processing
     */
    STRIPE("Stripe"),

    /**
     * Credit card payment (future)
     */
    CREDIT_CARD("Credit Card"),

    /**
     * Bank transfer (future)
     */
    BANK_TRANSFER("Bank Transfer"),

    /**
     * Digital wallet (future)
     */
    DIGITAL_WALLET("Digital Wallet");

    private final String displayName;

    PaymentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}