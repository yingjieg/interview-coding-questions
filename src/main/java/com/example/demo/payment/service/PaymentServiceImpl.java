package com.example.demo.payment.service;

import com.example.demo.common.exception.RecordNotFoundException;
import com.example.demo.order.entity.OrderEntity;
import com.example.demo.payment.entity.PaymentEntity;
import com.example.demo.payment.entity.PaymentStatus;
import com.example.demo.payment.entity.PaymentType;
import com.example.demo.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Main PaymentService implementation that uses the provider pattern.
 * This service delegates to specific payment provider services based on payment type.
 */
@Service
@Primary
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final List<PaymentProviderService> paymentProviders;
    private final Map<PaymentType, PaymentProviderService> providerMap;

    public PaymentServiceImpl(PaymentRepository paymentRepository, List<PaymentProviderService> paymentProviders) {
        this.paymentRepository = paymentRepository;
        this.paymentProviders = paymentProviders;
        this.providerMap = paymentProviders.stream()
                .collect(Collectors.toMap(
                        PaymentProviderService::getSupportedPaymentType,
                        Function.identity()
                ));
        log.info("Initialized PaymentServiceImpl with {} providers: {}",
                providerMap.size(), providerMap.keySet());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentEntity getPaymentById(Long id) {
        return paymentRepository.findByIdOrThrow(id, "Payment");
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
    public PaymentEntity createPayment(OrderEntity order, BigDecimal amount, PaymentType paymentType) {
        PaymentProviderService provider = getProvider(paymentType);
        log.info("Creating {} payment for order {} with amount {}", paymentType, order.getId(), amount);
        return provider.createPayment(order, amount);
    }

    @Override
    public PaymentEntity processPayment(PaymentEntity payment, PaymentProcessingContext context) {
        PaymentProviderService provider = getProvider(payment.getPaymentType());
        log.info("Processing {} payment with ID: {}", payment.getPaymentType(), payment.getId());
        return provider.processPayment(payment, context);
    }

    @Override
    public PaymentEntity cancelPayment(Long paymentId, String reason) {
        PaymentEntity payment = getPaymentById(paymentId);
        PaymentProviderService provider = getProvider(payment.getPaymentType());
        log.info("Cancelling {} payment with ID: {}", payment.getPaymentType(), paymentId);
        return provider.cancelPayment(payment, reason);
    }


    @Override
    public PaymentEntity getPaymentStatus(Long paymentId) {
        return getPaymentById(paymentId);
    }

    @Override
    public boolean isPaymentCompleted(Long paymentId) {
        PaymentEntity payment = getPaymentById(paymentId);
        return payment.isCompleted();
    }

    @Override
    public boolean canPaymentBeRetried(Long paymentId) {
        PaymentEntity payment = getPaymentById(paymentId);
        return payment.canBeRetried();
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

    /**
     * Get the appropriate payment provider for the given payment type
     */
    private PaymentProviderService getProvider(PaymentType paymentType) {
        PaymentProviderService provider = providerMap.get(paymentType);
        if (provider == null) {
            throw new IllegalArgumentException("No payment provider found for payment type: " + paymentType);
        }
        return provider;
    }
}