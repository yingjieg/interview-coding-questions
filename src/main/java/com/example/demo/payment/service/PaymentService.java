package com.example.demo.payment.service;

import com.example.demo.order.entity.OrderEntity;
import com.example.demo.payment.entity.PaymentEntity;
import com.example.demo.payment.entity.PaymentType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Generic Payment Service interface following payment industry best practices.
 * This interface provides a unified API for all payment operations regardless of payment provider.
 */
public interface PaymentService {

    // Basic CRUD operations
    PaymentEntity getPaymentById(Long id);

    PaymentEntity getPaymentByOrderId(Long orderId);

    List<PaymentEntity> getUserPayments(Long userId);

    PaymentEntity savePayment(PaymentEntity payment);

    // Generic payment lifecycle operations
    PaymentEntity createPayment(OrderEntity order, BigDecimal amount, PaymentType paymentType);

    PaymentEntity processPayment(PaymentEntity payment, PaymentProcessingContext context);

    PaymentEntity cancelPayment(Long paymentId, String reason);
    // TODO: Add refundPayment when actually implemented by providers

    // Payment status operations
    PaymentEntity getPaymentStatus(Long paymentId);

    boolean isPaymentCompleted(Long paymentId);

    boolean canPaymentBeRetried(Long paymentId);

    // Maintenance operations
    List<PaymentEntity> getExpiredPayments();

    void cleanupExpiredPayments();
}