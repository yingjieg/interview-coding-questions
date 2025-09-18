package com.example.demo.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StripePaymentResponse {

    private String paymentIntentId;
    private String clientSecret;
    private String status; // requires_payment_method, requires_confirmation, requires_action, processing, requires_capture, canceled, succeeded
    private Long amount;
    private String currency;
    private String description;

    // For redirect flows
    private String redirectUrl;

    // Payment method details
    private String paymentMethodId;
    private String paymentMethodType;

    // Confirmation details
    private String receiptUrl;
    private String chargeId;

    // Error information
    private String errorMessage;
    private String errorCode;

    // Metadata
    private String orderId;
    private String userId;

    private String message;
}