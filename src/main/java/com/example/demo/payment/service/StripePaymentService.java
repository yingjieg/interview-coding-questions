package com.example.demo.payment.service;

import com.example.demo.common.exception.RecordNotFoundException;
import com.example.demo.order.entity.OrderEntity;
import com.example.demo.payment.entity.PaymentEntity;
import com.example.demo.payment.entity.PaymentStatus;
import com.example.demo.payment.entity.PaymentType;
import com.example.demo.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class StripePaymentService implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;

    @Override
    @Transactional(readOnly = true)
    public PaymentEntity getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Payment", id));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentEntity getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RecordNotFoundException("Payment for order", orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentEntity> getUserPayments(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public PaymentEntity savePayment(PaymentEntity payment) {
        log.info("Saving payment with ID: {}", payment.getId());
        return paymentRepository.save(payment);
    }

    @Override
    public PaymentEntity createPayPalPayment(OrderEntity order, BigDecimal amount) {
        throw new UnsupportedOperationException("PayPal payments are not supported by StripePaymentService. Use PayPalPaymentService instead.");
    }

    @Override
    public PaymentEntity createStripePayment(OrderEntity order, BigDecimal amount) {
        log.info("Creating Stripe payment for order {} with amount {}", order.getId(), amount);

        PaymentEntity payment = new PaymentEntity();
        payment.setOrder(order);
        payment.setPaymentType(PaymentType.STRIPE);
        payment.setAmount(amount);
        payment.setCurrency("USD");
        payment.setPaymentStatus(PaymentStatus.PENDING);

        PaymentEntity savedPayment = paymentRepository.save(payment);
        log.info("Created payment with ID: {}", savedPayment.getId());

        return savedPayment;
    }

    @Override
    public PaymentEntity processPayPalPayment(PaymentEntity payment, String returnUrl, String cancelUrl) {
        throw new UnsupportedOperationException("PayPal payment processing is not supported by StripePaymentService. Use PayPalPaymentService instead.");
    }

    @Override
    public PaymentEntity approvePayPalPayment(String paypalOrderId, String payerId) {
        throw new UnsupportedOperationException("PayPal payment approval is not supported by StripePaymentService. Use PayPalPaymentService instead.");
    }

    @Override
    public PaymentEntity capturePayPalPayment(String paypalOrderId) {
        throw new UnsupportedOperationException("PayPal payment capture is not supported by StripePaymentService. Use PayPalPaymentService instead.");
    }

    @Override
    public PaymentEntity cancelPayment(Long paymentId, String reason) {
        log.info("Cancelling payment with ID: {} for reason: {}", paymentId, reason);

        PaymentEntity payment = getPaymentById(paymentId);

        if (payment.isCompleted()) {
            throw new IllegalStateException("Cannot cancel completed payment");
        }

        payment.markPaymentCancelled(reason);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentEntity> getExpiredPayments() {
        return paymentRepository.findExpiredPaymentsByStatus(
                PaymentStatus.PAYMENT_CREATED,
                LocalDateTime.now()
        );
    }

    @Override
    public void cleanupExpiredPayments() {
        List<PaymentEntity> expiredPayments = getExpiredPayments();

        for (PaymentEntity payment : expiredPayments) {
            log.info("Marking expired payment as cancelled: {}", payment.getId());
            payment.markPaymentCancelled("Payment expired");
            paymentRepository.save(payment);
        }

        log.info("Cleaned up {} expired payments", expiredPayments.size());
    }
}