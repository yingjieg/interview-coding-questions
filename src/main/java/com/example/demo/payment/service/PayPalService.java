package com.example.demo.payment.service;

import com.example.demo.payment.config.PayPalConfig;
import com.example.demo.payment.dto.PayPalPaymentRequest;
import com.example.demo.payment.dto.PayPalPaymentResponse;
import com.example.demo.payment.exception.PayPalConfigurationException;
import com.example.demo.payment.exception.PayPalPaymentException;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PayPalService {

    private final PayPalHttpClient payPalHttpClient;
    private final PayPalConfig payPalConfig;

    public PayPalService(PayPalHttpClient payPalHttpClient, PayPalConfig payPalConfig) {
        this.payPalHttpClient = payPalHttpClient;
        this.payPalConfig = payPalConfig;
        validateConfiguration();
    }

    public PayPalPaymentResponse createPayment(PayPalPaymentRequest paymentRequest) {
        try {
            validatePaymentRequest(paymentRequest);

            // Create order request
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.checkoutPaymentIntent("CAPTURE");

            // Set application context (return and cancel URLs)
            ApplicationContext applicationContext = new ApplicationContext()
                .returnUrl(payPalConfig.getReturnUrl())
                .cancelUrl(payPalConfig.getCancelUrl())
                .brandName("Ticket Booking System")
                .userAction("PAY_NOW")
                .shippingPreference("NO_SHIPPING");

            orderRequest.applicationContext(applicationContext);

            // Set purchase units
            List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
            PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
                .referenceId(paymentRequest.getReferenceId())
                .description(paymentRequest.getDescription())
                .customId(paymentRequest.getCustomId())
                .amountWithBreakdown(new AmountWithBreakdown()
                    .currencyCode(paymentRequest.getCurrency())
                    .value(paymentRequest.getAmount().toString()));

            purchaseUnits.add(purchaseUnitRequest);
            orderRequest.purchaseUnits(purchaseUnits);

            // Create order
            OrdersCreateRequest request = new OrdersCreateRequest();
            request.requestBody(orderRequest);

            HttpResponse<Order> response = payPalHttpClient.execute(request);
            Order order = response.result();

            log.info("PayPal order created successfully: {} for reference: {}",
                    order.id(), paymentRequest.getReferenceId());

            // Extract approval URL
            String approvalUrl = order.links().stream()
                .filter(link -> "approve".equals(link.rel()))
                .findFirst()
                .map(LinkDescription::href)
                .orElseThrow(() -> new PayPalPaymentException("No approval URL found in PayPal response"));

            return PayPalPaymentResponse.builder()
                .orderId(order.id())
                .status(order.status())
                .approvalUrl(approvalUrl)
                .build();

        } catch (IOException e) {
            log.error("Failed to create PayPal payment for reference {}: {}",
                     paymentRequest.getReferenceId(), e.getMessage(), e);
            throw new PayPalPaymentException("payment creation", paymentRequest.getReferenceId(), e);
        }
    }

    public PayPalPaymentResponse capturePayment(String orderId) {
        try {
            OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
            HttpResponse<Order> response = payPalHttpClient.execute(request);
            Order order = response.result();

            log.info("PayPal payment captured successfully: {}", order.id());

            return PayPalPaymentResponse.builder()
                .orderId(order.id())
                .status(order.status())
                .captureId(extractCaptureId(order))
                .build();

        } catch (IOException e) {
            log.error("Failed to capture PayPal payment for order {}: {}", orderId, e.getMessage(), e);
            throw new PayPalPaymentException("payment capture", orderId, e);
        }
    }

    public PayPalPaymentResponse getPaymentDetails(String orderId) {
        try {
            OrdersGetRequest request = new OrdersGetRequest(orderId);
            HttpResponse<Order> response = payPalHttpClient.execute(request);
            Order order = response.result();

            return PayPalPaymentResponse.builder()
                .orderId(order.id())
                .status(order.status())
                .captureId(extractCaptureId(order))
                .build();

        } catch (IOException e) {
            log.error("Failed to get PayPal payment details for order {}: {}", orderId, e.getMessage(), e);
            throw new PayPalPaymentException("payment status check", orderId, e);
        }
    }

    private String extractCaptureId(Order order) {
        return order.purchaseUnits().stream()
            .flatMap(unit -> unit.payments().captures().stream())
            .findFirst()
            .map(Capture::id)
            .orElse(null);
    }

    private void validateConfiguration() {
        if (payPalConfig.getClientId() == null || payPalConfig.getClientId().trim().isEmpty()) {
            throw new PayPalConfigurationException("client-id", "missing or empty");
        }
        if (payPalConfig.getClientSecret() == null || payPalConfig.getClientSecret().trim().isEmpty()) {
            throw new PayPalConfigurationException("client-secret", "missing or empty");
        }
        if (payPalConfig.getReturnUrl() == null || payPalConfig.getCancelUrl() == null) {
            throw new PayPalConfigurationException("return/cancel URLs", "missing");
        }
    }

    private void validatePaymentRequest(PayPalPaymentRequest request) {
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new PayPalPaymentException("Invalid payment amount: must be greater than zero");
        }
        if (request.getReferenceId() == null || request.getReferenceId().trim().isEmpty()) {
            throw new PayPalPaymentException("Invalid payment request: reference ID is required");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new PayPalPaymentException("Invalid payment request: description is required");
        }
    }
}