package com.example.demo.payment.controller;

import com.example.demo.payment.dto.PaymentMapper;
import com.example.demo.payment.dto.PaymentResponseDto;
import com.example.demo.payment.entity.PaymentEntity;
import com.example.demo.payment.service.PaymentService;
import com.example.demo.payment.service.PayPalPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "APIs for managing payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PayPalPaymentService payPalPaymentService;
    private final PaymentMapper paymentMapper;

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment details")
    @ApiResponse(responseCode = "200", description = "Payment details retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    public ResponseEntity<PaymentResponseDto> getPayment(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId) {
        log.info("Getting payment details for payment ID: {}", paymentId);

        PaymentEntity payment = paymentService.getPaymentById(paymentId);
        PaymentResponseDto response = paymentMapper.toDto(payment);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment for specific order")
    @ApiResponse(responseCode = "200", description = "Payment details retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Payment not found for order")
    public ResponseEntity<PaymentResponseDto> getPaymentByOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        log.info("Getting payment for order ID: {}", orderId);

        PaymentEntity payment = paymentService.getPaymentByOrderId(orderId);
        PaymentResponseDto response = paymentMapper.toDto(payment);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all payments for a user")
    @ApiResponse(responseCode = "200", description = "User payments retrieved successfully")
    public ResponseEntity<List<PaymentResponseDto>> getUserPayments(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        log.info("Getting payments for user ID: {}", userId);

        List<PaymentEntity> payments = paymentService.getUserPayments(userId);
        List<PaymentResponseDto> response = payments.stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/paypal/success")
    @Operation(summary = "Handle PayPal payment success callback")
    @ApiResponse(responseCode = "302", description = "Redirect to frontend success page")
    public ResponseEntity<Void> handlePayPalSuccess(
            @Parameter(description = "PayPal Order ID") @RequestParam("token") String paypalOrderId,
            @Parameter(description = "PayPal Payer ID") @RequestParam("PayerID") String payerId) {
        log.info("Handling PayPal success for order ID: {} and payer ID: {}", paypalOrderId, payerId);

        try {
            // Approve the payment
            PaymentEntity approvedPayment = payPalPaymentService.approvePayPalPayment(paypalOrderId, payerId);

            // Capture the payment
            PaymentEntity completedPayment = payPalPaymentService.capturePayPalPayment(paypalOrderId);

            // Redirect to frontend success page
            String frontendUrl = getFrontendUrl();
            String redirectUrl = frontendUrl + "/order/confirmation?orderId=" + completedPayment.getOrder().getId() + "&paymentSuccess=true";

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();

        } catch (Exception e) {
            log.error("Failed to complete PayPal payment for order ID: {}", paypalOrderId, e);

            // Redirect to frontend error page
            String frontendUrl = getFrontendUrl();
            try {
                String errorUrl = frontendUrl + "/payment?error=payment_failed&message=" +
                        java.net.URLEncoder.encode("PayPal payment processing failed", "UTF-8");

                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", errorUrl)
                        .build();
            } catch (UnsupportedEncodingException encodingException) {
                log.error("Failed to encode error message", encodingException);
                String errorUrl = frontendUrl + "/payment?error=payment_failed";
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", errorUrl)
                        .build();
            }
        }
    }

    @GetMapping("/paypal/cancel")
    @Operation(summary = "Handle PayPal payment cancellation")
    @ApiResponse(responseCode = "302", description = "Redirect to frontend cancel page")
    public ResponseEntity<Void> handlePayPalCancel(
            @Parameter(description = "PayPal Order ID") @RequestParam("token") String paypalOrderId) {
        log.info("Handling PayPal cancellation for order ID: {}", paypalOrderId);

        // Redirect to frontend payment page with cancellation message
        String frontendUrl = getFrontendUrl();
        try {
            String cancelUrl = frontendUrl + "/payment?cancelled=true&message=" +
                    java.net.URLEncoder.encode("Payment was cancelled", "UTF-8");

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", cancelUrl)
                    .build();
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to encode cancellation message", e);
            String cancelUrl = frontendUrl + "/payment?cancelled=true";
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", cancelUrl)
                    .build();
        }
    }

    private String getFrontendUrl() {
        // Get from environment variable or default to localhost
        return System.getProperty("frontend.url",
                System.getenv("FRONTEND_URL") != null ? System.getenv("FRONTEND_URL") : "http://localhost:3001");
    }

    @PostMapping("/{paymentId}/cancel")
    @Operation(summary = "Cancel a payment")
    @ApiResponse(responseCode = "200", description = "Payment cancelled successfully")
    public ResponseEntity<PaymentResponseDto> cancelPayment(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId,
            @Parameter(description = "Cancellation reason") @RequestParam(required = false, defaultValue = "User requested cancellation") String reason) {
        log.info("Cancelling payment ID: {} with reason: {}", paymentId, reason);

        PaymentEntity cancelledPayment = paymentService.cancelPayment(paymentId, reason);
        PaymentResponseDto response = paymentMapper.toDto(cancelledPayment);

        return ResponseEntity.ok(response);
    }
}