package com.example.demo.booking.repository;

import com.example.demo.booking.entity.BookingEntity;
import com.example.demo.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    List<BookingEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT b FROM BookingEntity b LEFT JOIN FETCH b.order LEFT JOIN FETCH b.user WHERE b.id = :bookingId")
    BookingEntity findByIdWithDetails(Long bookingId);

    boolean existsByOrderId(Long orderId);

    boolean existsByUserIdAndVisitDate(Long userId, LocalDate visitDate);
}