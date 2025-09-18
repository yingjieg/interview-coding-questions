package com.example.demo.payment.controller;

import com.example.demo.payment.dto.StripePaymentRequest;
import com.example.demo.payment.dto.StripePaymentResponse;
import com.example.demo.payment.service.StripeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
@Tag(name = "Stripe Payments", description = "Stripe payment processing endpoints")
public class StripeController {

    private final StripeService stripeService;

    @Operation(summary = "Get Stripe publishable key", description = "Get the publishable key for Stripe frontend integration")
    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getStripeConfig() {
        log.debug("Getting Stripe publishable key");

        String publishableKey = stripeService.getPublishableKey();
        return ResponseEntity.ok(Map.of(
                "publishableKey", publishableKey != null ? publishableKey : "",
                "configured", String.valueOf(publishableKey != null && !publishableKey.isEmpty())
        ));
    }

    @Operation(summary = "Create Payment Intent", description = "Create a new Stripe Payment Intent for processing payment")
    @PostMapping("/payment-intent")
    public ResponseEntity<StripePaymentResponse> createPaymentIntent(
            @Valid @RequestBody StripePaymentRequest request) {
        log.info("Creating Stripe Payment Intent for amount: {} {}", request.getAmount(), request.getCurrency());

        StripePaymentResponse response = stripeService.createPaymentIntent(request);

        if (response.getErrorMessage() != null) {
            log.error("Failed to create Payment Intent: {}", response.getErrorMessage());
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Confirm Payment Intent", description = "Confirm a Payment Intent (server-side confirmation)")
    @PostMapping("/payment-intent/{paymentIntentId}/confirm")
    public ResponseEntity<StripePaymentResponse> confirmPaymentIntent(
            @Parameter(description = "Payment Intent ID") @PathVariable String paymentIntentId) {
        log.info("Confirming Stripe Payment Intent: {}", paymentIntentId);

        StripePaymentResponse response = stripeService.confirmPaymentIntent(paymentIntentId);

        if (response.getErrorMessage() != null) {
            log.error("Failed to confirm Payment Intent {}: {}", paymentIntentId, response.getErrorMessage());
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Payment Intent status", description = "Retrieve the current status of a Payment Intent")
    @GetMapping("/payment-intent/{paymentIntentId}")
    public ResponseEntity<StripePaymentResponse> getPaymentIntent(
            @Parameter(description = "Payment Intent ID") @PathVariable String paymentIntentId) {
        log.debug("Getting Stripe Payment Intent: {}", paymentIntentId);

        StripePaymentResponse response = stripeService.getPaymentIntent(paymentIntentId);

        if (response.getErrorMessage() != null) {
            log.error("Failed to get Payment Intent {}: {}", paymentIntentId, response.getErrorMessage());
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel Payment Intent", description = "Cancel a Payment Intent before it's completed")
    @PostMapping("/payment-intent/{paymentIntentId}/cancel")
    public ResponseEntity<StripePaymentResponse> cancelPaymentIntent(
            @Parameter(description = "Payment Intent ID") @PathVariable String paymentIntentId) {
        log.info("Canceling Stripe Payment Intent: {}", paymentIntentId);

        StripePaymentResponse response = stripeService.cancelPaymentIntent(paymentIntentId);

        if (response.getErrorMessage() != null) {
            log.error("Failed to cancel Payment Intent {}: {}", paymentIntentId, response.getErrorMessage());
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

}