package com.example.demo.payment.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.RecordNotFoundException;
import com.example.demo.order.entity.OrderEntity;
import com.example.demo.payment.dto.StripePaymentRequest;
import com.example.demo.payment.dto.StripePaymentResponse;
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
public class StripePaymentService implements PaymentProviderService {

    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;
    private final PaymentEntityFactory paymentEntityFactory;

    @Override
    public PaymentType getSupportedPaymentType() {
        return PaymentType.STRIPE;
    }

    @Override
    public PaymentEntity createPayment(OrderEntity order, BigDecimal amount) {
        return createStripePayment(order, amount);
    }

    @Override
    public PaymentEntity processPayment(PaymentEntity payment, PaymentProcessingContext context) {
        return processStripePayment(payment, context);
    }

    @Override
    public PaymentEntity cancelPayment(PaymentEntity payment, String reason) {
        return cancelPayment(payment.getId(), reason);
    }







    public PaymentEntity createStripePayment(OrderEntity order, BigDecimal amount) {
        log.info("Creating Stripe payment for order {} with amount {}", order.getId(), amount);

        PaymentEntity payment = paymentEntityFactory.createPayment(order, amount, PaymentType.STRIPE);
        PaymentEntity savedPayment = paymentRepository.save(payment);
        log.info("Created payment with ID: {}", savedPayment.getId());

        return savedPayment;
    }

    public PaymentEntity processStripePayment(PaymentEntity payment, PaymentProcessingContext context) {
        log.info("Processing Stripe payment for payment ID: {}", payment.getId());

        // Create Stripe Payment Intent
        StripePaymentRequest stripeRequest = new StripePaymentRequest();
        stripeRequest.setAmount(payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue()); // Convert to cents
        stripeRequest.setCurrency(payment.getCurrency().toLowerCase());
        stripeRequest.setDescription("Ticket Booking Order #" + payment.getOrder().getId());
        stripeRequest.setOrderId(payment.getOrder().getId().toString());
        stripeRequest.setUserId(context.getUserId() != null ? context.getUserId().toString() : null);

        StripePaymentResponse stripeResponse = stripeService.createPaymentIntent(stripeRequest);

        if (stripeResponse.getErrorMessage() != null) {
            log.error("Stripe Payment Intent creation failed: {}", stripeResponse.getErrorMessage());
            throw new BusinessException("STRIPE_PAYMENT_FAILED", "Failed to create Stripe payment: " + stripeResponse.getErrorMessage());
        }

        // Update payment entity with Stripe details
        payment.markStripeCreated(
                stripeResponse.getPaymentIntentId(),
                stripeResponse.getClientSecret(),
                payment.getAmount()
        );

        PaymentEntity processedPayment = paymentRepository.save(payment);
        log.info("Stripe payment processed: {} with Payment Intent ID: {}",
                processedPayment.getId(), processedPayment.getStripePaymentIntentId());
        return processedPayment;
    }




    public PaymentEntity cancelPayment(Long paymentId, String reason) {
        log.info("Cancelling payment with ID: {} for reason: {}", paymentId, reason);

        PaymentEntity payment = paymentRepository.findByIdOrThrow(paymentId, "Payment");

        if (payment.isCompleted()) {
            throw new IllegalStateException("Cannot cancel completed payment");
        }

        payment.markPaymentCancelled(reason);
        return paymentRepository.save(payment);
    }


}