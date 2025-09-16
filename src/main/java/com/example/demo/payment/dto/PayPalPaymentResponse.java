package com.example.demo.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayPalPaymentResponse {

    private String orderId;       // PayPal order ID
    private String status;        // PayPal order status (CREATED, APPROVED, COMPLETED, etc.)
    private String approvalUrl;   // URL for user to approve payment
    private String captureId;     // PayPal capture ID (after payment completion)
    private String payerId;       // PayPal payer ID (after approval)
}