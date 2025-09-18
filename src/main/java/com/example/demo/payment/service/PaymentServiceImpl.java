package com.example.demo.payment.service;

import com.example.demo.common.exception.RecordNotFoundException;
import com.example.demo.order.entity.OrderEntity;
import com.example.demo.payment.entity.PaymentEntity;
import com.example.demo.payment.entity.PaymentType;
import com.example.demo.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Primary
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PayPalPaymentService payPalPaymentService;
    private final StripePaymentService stripePaymentService;

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
        return payPalPaymentService.createPayPalPayment(order, amount);
    }

    @Override
    public PaymentEntity createStripePayment(OrderEntity order, BigDecimal amount) {
        return stripePaymentService.createStripePayment(order, amount);
    }

    @Override
    public PaymentEntity processPayPalPayment(PaymentEntity payment, String returnUrl, String cancelUrl) {
        return payPalPaymentService.processPayPalPayment(payment, returnUrl, cancelUrl);
    }

    @Override
    public PaymentEntity approvePayPalPayment(String paypalOrderId, String payerId) {
        return payPalPaymentService.approvePayPalPayment(paypalOrderId, payerId);
    }

    @Override
    public PaymentEntity capturePayPalPayment(String paypalOrderId) {
        return payPalPaymentService.capturePayPalPayment(paypalOrderId);
    }

    @Override
    public PaymentEntity cancelPayment(Long paymentId, String reason) {
        // Determine which service to use based on payment type
        PaymentEntity payment = getPaymentById(paymentId);

        if (payment.getPaymentType() == PaymentType.PAYPAL) {
            return payPalPaymentService.cancelPayment(paymentId, reason);
        } else if (payment.getPaymentType() == PaymentType.STRIPE) {
            return stripePaymentService.cancelPayment(paymentId, reason);
        } else {
            // Default to repository-level cancellation
            if (payment.isCompleted()) {
                throw new IllegalStateException("Cannot cancel completed payment");
            }
            payment.markPaymentCancelled(reason);
            return paymentRepository.save(payment);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentEntity> getExpiredPayments() {
        // Use PayPal service for now, both services have same implementation
        return payPalPaymentService.getExpiredPayments();
    }

    @Override
    public void cleanupExpiredPayments() {
        // Use PayPal service for now, both services have same implementation
        payPalPaymentService.cleanupExpiredPayments();
    }
}