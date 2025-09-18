package com.example.demo.payment.service;

import com.example.demo.order.entity.OrderEntity;
import com.example.demo.payment.entity.PaymentEntity;
import com.example.demo.payment.entity.PaymentType;

import java.math.BigDecimal;

/**
 * Interface for payment provider-specific operations.
 * Each payment provider (PayPal, Stripe, etc.) implements this interface
 * to handle their specific payment processing logic.
 */
public interface PaymentProviderService {

    /**
     * Returns the payment type this service handles
     */
    PaymentType getSupportedPaymentType();

    /**
     * Create a payment for this provider
     */
    PaymentEntity createPayment(OrderEntity order, BigDecimal amount);

    /**
     * Process a payment using this provider
     */
    PaymentEntity processPayment(PaymentEntity payment, PaymentProcessingContext context);

    /**
     * Cancel a payment using this provider
     */
    PaymentEntity cancelPayment(PaymentEntity payment, String reason);

    // TODO: Add refundPayment method when implemented by providers

    /**
     * Check if this service supports the given payment type
     */
    default boolean supports(PaymentType paymentType) {
        return getSupportedPaymentType().equals(paymentType);
    }
}