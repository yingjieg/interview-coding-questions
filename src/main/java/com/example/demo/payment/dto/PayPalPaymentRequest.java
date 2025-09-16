package com.example.demo.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PayPalPaymentRequest {

    private String referenceId;      // Our internal order/purchase ID
    private String customId;         // Additional custom identifier
    private String description;      // Payment description
    private BigDecimal amount;       // Payment amount in USD
    private String currency;         // Currency code (default USD)
    private String returnUrl;        // Success return URL
    private String cancelUrl;        // Cancel return URL

    // Default currency to USD if not specified
    public String getCurrency() {
        return currency != null ? currency : "USD";
    }
}