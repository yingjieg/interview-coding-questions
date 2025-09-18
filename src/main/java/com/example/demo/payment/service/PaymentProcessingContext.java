package com.example.demo.payment.service;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Context object for payment processing operations.
 * This allows different payment providers to receive the specific data they need
 * without polluting the generic PaymentService interface.
 */
@Data
@Builder
public class PaymentProcessingContext {

    // Generic fields available to all payment providers
    private String returnUrl;
    private String cancelUrl;
    private String description;
    private Long userId;
    private Long orderId;

    // Provider-specific metadata
    @Builder.Default
    private Map<String, String> metadata = Map.of();

    // PayPal-specific fields (when needed)
    private String paypalPayerId;

    // Stripe-specific fields (when needed)
    private String stripePaymentMethodId;
    private Boolean confirmImmediately;

    // Generic provider-specific data
    private Map<String, Object> providerSpecificData;

    /**
     * Get provider-specific data with type casting
     */
    @SuppressWarnings("unchecked")
    public <T> T getProviderData(String key, Class<T> type) {
        if (providerSpecificData == null) {
            return null;
        }
        Object value = providerSpecificData.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * Helper method to create context for PayPal processing
     */
    public static PaymentProcessingContext forPayPal(String returnUrl, String cancelUrl) {
        return PaymentProcessingContext.builder()
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .build();
    }

    /**
     * Helper method to create context for Stripe processing
     */
    public static PaymentProcessingContext forStripe(Long userId, Long orderId, String description) {
        return PaymentProcessingContext.builder()
                .userId(userId)
                .orderId(orderId)
                .description(description)
                .confirmImmediately(false)
                .build();
    }
}