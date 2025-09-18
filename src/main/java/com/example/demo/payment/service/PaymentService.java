package com.example.demo.payment.service;

import com.example.demo.order.entity.OrderEntity;
import com.example.demo.payment.entity.PaymentEntity;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

    // Basic CRUD operations
    PaymentEntity getPaymentById(Long id);

    PaymentEntity getPaymentByOrderId(Long orderId);

    List<PaymentEntity> getUserPayments(Long userId);

    PaymentEntity savePayment(PaymentEntity payment);

    // Payment creation methods
    PaymentEntity createPayPalPayment(OrderEntity order, BigDecimal amount);

    PaymentEntity createStripePayment(OrderEntity order, BigDecimal amount);

    // Payment processing
    PaymentEntity processPayPalPayment(PaymentEntity payment, String returnUrl, String cancelUrl);

    // PayPal specific methods
    PaymentEntity approvePayPalPayment(String paypalOrderId, String payerId);

    PaymentEntity capturePayPalPayment(String paypalOrderId);

    // Generic payment operations
    PaymentEntity cancelPayment(Long paymentId, String reason);

    // Maintenance operations
    List<PaymentEntity> getExpiredPayments();

    void cleanupExpiredPayments();
}