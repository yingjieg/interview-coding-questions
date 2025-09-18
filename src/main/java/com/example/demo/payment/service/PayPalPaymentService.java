package com.example.demo.payment.service;

import com.example.demo.common.exception.RecordNotFoundException;
import com.example.demo.order.entity.OrderEntity;
import com.example.demo.payment.dto.PayPalCaptureResponse;
import com.example.demo.payment.dto.PayPalPaymentRequest;
import com.example.demo.payment.dto.PayPalPaymentResponse;
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
public class PayPalPaymentService implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PayPalService payPalService;

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
    public PaymentEntity createPayPalPayment(OrderEntity order, BigDecimal amount) {
        log.info("Creating PayPal payment for order {} with amount {}", order.getId(), amount);

        PaymentEntity payment = new PaymentEntity();
        payment.setOrder(order);
        payment.setPaymentType(PaymentType.PAYPAL);
        payment.setAmount(amount);
        payment.setCurrency("USD");
        payment.setPaymentStatus(PaymentStatus.PENDING);

        PaymentEntity savedPayment = paymentRepository.save(payment);
        log.info("Created payment with ID: {}", savedPayment.getId());

        return savedPayment;
    }

    @Override
    public PaymentEntity createStripePayment(OrderEntity order, BigDecimal amount) {
        throw new UnsupportedOperationException("Stripe payments are not supported by PayPalPaymentService. Use StripePaymentService instead.");
    }

    @Override
    public PaymentEntity savePayment(PaymentEntity payment) {
        log.info("Saving payment with ID: {}", payment.getId());
        return paymentRepository.save(payment);
    }

    @Override
    public PaymentEntity processPayPalPayment(PaymentEntity payment, String returnUrl, String cancelUrl) {
        try {
            log.info("Processing PayPal payment for payment ID: {}", payment.getId());

            PayPalPaymentRequest paymentRequest = PayPalPaymentRequest.builder()
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .description("Ticket Order #" + payment.getOrder().getId())
                    .referenceId(payment.getId().toString())
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .build();

            PayPalPaymentResponse paypalResponse = payPalService.createPayment(paymentRequest);

            payment.markPayPalCreated(paypalResponse.getOrderId(), payment.getAmount(), paypalResponse.getApprovalUrl());
            PaymentEntity updatedPayment = paymentRepository.save(payment);

            log.info("PayPal payment created with order ID: {}", paypalResponse.getOrderId());
            return updatedPayment;

        } catch (Exception e) {
            log.error("Failed to process PayPal payment for payment ID: {}", payment.getId(), e);
            payment.markPaymentFailed("PayPal payment creation failed: " + e.getMessage());
            paymentRepository.save(payment);
            throw e;
        }
    }

    @Override
    public PaymentEntity approvePayPalPayment(String paypalOrderId, String payerId) {
        log.info("Approving PayPal payment with order ID: {} and payer ID: {}", paypalOrderId, payerId);

        PaymentEntity payment = paymentRepository.findByPaypalOrderId(paypalOrderId)
                .orElseThrow(() -> new RecordNotFoundException("Payment with PayPal order", paypalOrderId));

        if (!PaymentStatus.PAYMENT_CREATED.equals(payment.getPaymentStatus())) {
            throw new IllegalStateException("Payment is not in PAYMENT_CREATED status, cannot approve");
        }

        payment.markPayPalApproved(payerId);
        return paymentRepository.save(payment);
    }

    @Override
    public PaymentEntity capturePayPalPayment(String paypalOrderId) {
        try {
            log.info("Capturing PayPal payment with order ID: {}", paypalOrderId);

            PaymentEntity payment = paymentRepository.findByPaypalOrderId(paypalOrderId)
                    .orElseThrow(() -> new RecordNotFoundException("Payment with PayPal order", paypalOrderId));

            if (!PaymentStatus.PAYMENT_APPROVED.equals(payment.getPaymentStatus())) {
                throw new IllegalStateException("Payment is not in PAYMENT_APPROVED status, cannot capture");
            }

            PayPalPaymentResponse captureResponse = payPalService.capturePayment(paypalOrderId);

            payment.markPayPalCompleted(captureResponse.getCaptureId());
            PaymentEntity completedPayment = paymentRepository.save(payment);

            log.info("PayPal payment captured with capture ID: {}", captureResponse.getCaptureId());
            return completedPayment;

        } catch (Exception e) {
            log.error("Failed to capture PayPal payment with order ID: {}", paypalOrderId, e);

            PaymentEntity payment = paymentRepository.findByPaypalOrderId(paypalOrderId)
                    .orElseThrow(() -> new RecordNotFoundException("Payment with PayPal order", paypalOrderId));

            payment.markPaymentFailed("PayPal payment capture failed: " + e.getMessage());
            paymentRepository.save(payment);
            throw e;
        }
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