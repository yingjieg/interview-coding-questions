package com.example.demo.payment.dto;

import com.example.demo.payment.entity.PaymentStatus;
import com.example.demo.payment.entity.PaymentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponseDto {
    private Long id;
    private Long orderId;
    private PaymentType paymentType;
    private PaymentStatus paymentStatus;
    private BigDecimal amount;
    private String currency;
    private String paypalOrderId;
    private String paypalApprovalUrl;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean expired;
}