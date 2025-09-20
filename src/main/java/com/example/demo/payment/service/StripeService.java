package com.example.demo.payment.service;

import com.example.demo.payment.config.StripeConfig;
import com.example.demo.payment.dto.StripePaymentRequest;
import com.example.demo.payment.dto.StripePaymentResponse;
import com.example.demo.payment.exception.StripePaymentException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeService {

    private final StripeConfig stripeConfig;

    /**
     * Create a Payment Intent for immediate payment
     */
    public StripePaymentResponse createPaymentIntent(StripePaymentRequest request) {
        if (!stripeConfig.isConfigured()) {
            throw new StripePaymentException("Stripe is not properly configured");
        }

        try {
            // Build metadata
            Map<String, String> metadata = new HashMap<>();
            if (request.getOrderId() != null) {
                metadata.put("order_id", request.getOrderId());
            }
            if (request.getUserId() != null) {
                metadata.put("user_id", request.getUserId());
            }

            // Create Payment Intent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(request.getAmount())
                    .setCurrency(request.getCurrency().toLowerCase())
                    .setDescription(request.getDescription())
                    .putAllMetadata(metadata)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            log.info("Created Stripe Payment Intent: {} for amount: {} {}",
                    paymentIntent.getId(), request.getAmount(), request.getCurrency());

            return StripePaymentResponse.builder()
                    .paymentIntentId(paymentIntent.getId())
                    .clientSecret(paymentIntent.getClientSecret())
                    .status(paymentIntent.getStatus())
                    .amount(paymentIntent.getAmount())
                    .currency(paymentIntent.getCurrency())
                    .description(paymentIntent.getDescription())
                    .orderId(request.getOrderId())
                    .userId(request.getUserId())
                    .message("Payment Intent created successfully")
                    .build();

        } catch (StripeException e) {
            log.error("Stripe Payment Intent creation failed: {}", e.getMessage(), e);
            return StripePaymentResponse.builder()
                    .errorMessage(e.getMessage())
                    .errorCode(e.getCode())
                    .orderId(request.getOrderId())
                    .userId(request.getUserId())
                    .message("Payment Intent creation failed")
                    .build();
        }
    }

    /**
     * Confirm a Payment Intent (for server-side confirmation)
     */
    public StripePaymentResponse confirmPaymentIntent(String paymentIntentId) {
        if (!stripeConfig.isConfigured()) {
            throw new StripePaymentException("Stripe is not properly configured");
        }

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            if ("succeeded".equals(paymentIntent.getStatus())) {
                log.info("Payment Intent {} is already succeeded", paymentIntentId);
                return buildResponseFromPaymentIntent(paymentIntent, "Payment already completed");
            }

            // Confirm the payment intent
            PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder()
                    .setReturnUrl("https://your-website.com/return") // Configure this
                    .build();

            PaymentIntent confirmedPaymentIntent = paymentIntent.confirm(params);

            log.info("Confirmed Stripe Payment Intent: {} with status: {}",
                    confirmedPaymentIntent.getId(), confirmedPaymentIntent.getStatus());

            return buildResponseFromPaymentIntent(confirmedPaymentIntent, "Payment confirmed");

        } catch (StripeException e) {
            log.error("Stripe Payment Intent confirmation failed: {}", e.getMessage(), e);
            return StripePaymentResponse.builder()
                    .paymentIntentId(paymentIntentId)
                    .errorMessage(e.getMessage())
                    .errorCode(e.getCode())
                    .message("Payment confirmation failed")
                    .build();
        }
    }

    /**
     * Retrieve Payment Intent status
     */
    public StripePaymentResponse getPaymentIntent(String paymentIntentId) {
        if (!stripeConfig.isConfigured()) {
            throw new StripePaymentException("Stripe is not properly configured");
        }

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            return buildResponseFromPaymentIntent(paymentIntent, "Payment Intent retrieved");

        } catch (StripeException e) {
            log.error("Failed to retrieve Stripe Payment Intent {}: {}", paymentIntentId, e.getMessage(), e);
            return StripePaymentResponse.builder()
                    .paymentIntentId(paymentIntentId)
                    .errorMessage(e.getMessage())
                    .errorCode(e.getCode())
                    .message("Failed to retrieve payment")
                    .build();
        }
    }

    /**
     * Cancel a Payment Intent
     */
    public StripePaymentResponse cancelPaymentIntent(String paymentIntentId) {
        if (!stripeConfig.isConfigured()) {
            throw new StripePaymentException("Stripe is not properly configured");
        }

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            if ("succeeded".equals(paymentIntent.getStatus())) {
                throw new StripePaymentException("Cannot cancel a succeeded payment. Use refund instead.");
            }

            if ("canceled".equals(paymentIntent.getStatus())) {
                return buildResponseFromPaymentIntent(paymentIntent, "Payment already canceled");
            }

            PaymentIntent canceledPaymentIntent = paymentIntent.cancel();

            log.info("Canceled Stripe Payment Intent: {}", canceledPaymentIntent.getId());

            return buildResponseFromPaymentIntent(canceledPaymentIntent, "Payment canceled successfully");

        } catch (StripeException e) {
            log.error("Failed to cancel Stripe Payment Intent {}: {}", paymentIntentId, e.getMessage(), e);
            return StripePaymentResponse.builder()
                    .paymentIntentId(paymentIntentId)
                    .errorMessage(e.getMessage())
                    .errorCode(e.getCode())
                    .message("Payment cancellation failed")
                    .build();
        }
    }

    /**
     * Helper method to build response from PaymentIntent
     */
    private StripePaymentResponse buildResponseFromPaymentIntent(PaymentIntent paymentIntent, String message) {
        return StripePaymentResponse.builder()
                .paymentIntentId(paymentIntent.getId())
                .clientSecret(paymentIntent.getClientSecret())
                .status(paymentIntent.getStatus())
                .amount(paymentIntent.getAmount())
                .currency(paymentIntent.getCurrency())
                .description(paymentIntent.getDescription())
                .orderId(paymentIntent.getMetadata().get("order_id"))
                .userId(paymentIntent.getMetadata().get("user_id"))
                .message(message)
                .build();
    }

    /**
     * Get Stripe publishable key for frontend
     */
    public String getPublishableKey() {
        return stripeConfig.getPublishableKey();
    }
}