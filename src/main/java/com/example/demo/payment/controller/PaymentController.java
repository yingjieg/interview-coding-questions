package com.example.demo.payment.controller;

import com.example.demo.payment.dto.PaymentMapper;
import com.example.demo.payment.dto.PaymentResponseDto;
import com.example.demo.payment.entity.PaymentEntity;
import com.example.demo.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "APIs for managing payments")
public class PaymentController {

    private final PaymentService paymentService;
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
    @ApiResponse(responseCode = "200", description = "Payment approved successfully")
    public ResponseEntity<PaymentResponseDto> handlePayPalSuccess(
            @Parameter(description = "PayPal Order ID") @RequestParam("token") String paypalOrderId,
            @Parameter(description = "PayPal Payer ID") @RequestParam("PayerID") String payerId) {
        log.info("Handling PayPal success for order ID: {} and payer ID: {}", paypalOrderId, payerId);

        try {
            // Approve the payment
            PaymentEntity approvedPayment = paymentService.approvePayPalPayment(paypalOrderId, payerId);

            // Capture the payment
            PaymentEntity completedPayment = paymentService.capturePayPalPayment(paypalOrderId);

            PaymentResponseDto response = paymentMapper.toDto(completedPayment);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to complete PayPal payment for order ID: {}", paypalOrderId, e);
            throw e;
        }
    }

    @GetMapping("/paypal/cancel")
    @Operation(summary = "Handle PayPal payment cancellation")
    @ApiResponse(responseCode = "200", description = "Payment cancelled successfully")
    public ResponseEntity<String> handlePayPalCancel(
            @Parameter(description = "PayPal Order ID") @RequestParam("token") String paypalOrderId) {
        log.info("Handling PayPal cancellation for order ID: {}", paypalOrderId);

        // For now, just log the cancellation
        // In a real implementation, you might want to update the payment status
        return ResponseEntity.ok("Payment cancelled successfully");
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