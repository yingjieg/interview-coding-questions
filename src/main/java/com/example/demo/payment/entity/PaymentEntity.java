package com.example.demo.payment.entity;

import com.example.demo.order.entity.OrderEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    // PayPal specific fields
    @Column(name = "paypal_order_id", length = 100)
    private String paypalOrderId;

    @Column(name = "paypal_capture_id", length = 100)
    private String paypalCaptureId;

    @Column(name = "paypal_payer_id", length = 50)
    private String paypalPayerId;

    @Column(name = "paypal_approval_url", columnDefinition = "TEXT")
    private String paypalApprovalUrl;

    // Generic payment fields for other providers
    @Column(name = "external_transaction_id", length = 100)
    private String externalTransactionId;

    @Column(name = "payment_method_details", columnDefinition = "TEXT")
    private String paymentMethodDetails; // JSON for additional payment info

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    // Timestamps
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods for PayPal payments
    public void markPayPalCreated(String paypalOrderId, BigDecimal amount, String approvalUrl) {
        this.paypalOrderId = paypalOrderId;
        this.amount = amount;
        this.paypalApprovalUrl = approvalUrl;
        this.paymentStatus = PaymentStatus.PAYMENT_CREATED;
        this.paymentType = PaymentType.PAYPAL;
        // PayPal payments typically expire in 3 hours
        this.expiresAt = LocalDateTime.now().plusHours(3);
    }

    public void markPayPalApproved(String payerId) {
        this.paypalPayerId = payerId;
        this.paymentStatus = PaymentStatus.PAYMENT_APPROVED;
    }

    public void markPayPalCompleted(String captureId) {
        this.paypalCaptureId = captureId;
        this.externalTransactionId = captureId; // Use capture ID as transaction ID
        this.paymentStatus = PaymentStatus.PAYMENT_COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markPaymentFailed(String reason) {
        this.paymentStatus = PaymentStatus.PAYMENT_FAILED;
        this.failureReason = reason;
    }

    public void markPaymentCancelled(String reason) {
        this.paymentStatus = PaymentStatus.PAYMENT_CANCELLED;
        this.failureReason = reason;
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isCompleted() {
        return PaymentStatus.PAYMENT_COMPLETED.equals(paymentStatus);
    }

    public boolean canBeRetried() {
        return PaymentStatus.PAYMENT_FAILED.equals(paymentStatus) ||
               PaymentStatus.PAYMENT_CANCELLED.equals(paymentStatus) ||
               isExpired();
    }
}