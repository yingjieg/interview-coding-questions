package com.example.demo.payment.repository;

import com.example.demo.payment.entity.PaymentEntity;
import com.example.demo.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByOrderId(Long orderId);

    List<PaymentEntity> findByPaymentStatus(PaymentStatus paymentStatus);

    @Query("SELECT p FROM PaymentEntity p WHERE p.paymentStatus = :status AND p.expiresAt < :currentTime")
    List<PaymentEntity> findExpiredPaymentsByStatus(@Param("status") PaymentStatus status, @Param("currentTime") LocalDateTime currentTime);

    Optional<PaymentEntity> findByPaypalOrderId(String paypalOrderId);

    Optional<PaymentEntity> findByExternalTransactionId(String externalTransactionId);

    @Query("SELECT p FROM PaymentEntity p WHERE p.order.user.id = :userId ORDER BY p.createdAt DESC")
    List<PaymentEntity> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}