package com.example.demo.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PayPalCaptureResponse {

    private String captureId;        // PayPal capture ID
    private String orderId;          // PayPal order ID
    private String status;           // Capture status
    private BigDecimal amount;       // Captured amount
    private String currency;         // Currency code
    private String createTime;       // Creation timestamp
    private String updateTime;       // Update timestamp
}