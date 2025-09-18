package com.example.demo.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StripePaymentRequest {

    @NotNull
    @Min(1)
    private Long amount; // Amount in cents

    @NotBlank
    private String currency = "usd";

    @NotBlank
    private String description;

    // Optional: Customer information
    private String customerEmail;
    private String customerName;

    // Optional: Metadata
    private String orderId;
    private String userId;

    // Optional: Payment method configuration
    private String returnUrl; // For redirect after payment
    private String cancelUrl; // For redirect on cancel

    // For payment confirmation
    private String paymentIntentId;
}