package com.example.demo.booking.repository;

import com.example.demo.booking.entity.BookingEntity;
import com.example.demo.booking.entity.BookingStatus;
import com.example.demo.booking.entity.TicketSubmissionStatus;
import com.example.demo.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends BaseRepository<BookingEntity, Long> {

    List<BookingEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT b FROM BookingEntity b LEFT JOIN FETCH b.order LEFT JOIN FETCH b.user WHERE b.id = :bookingId")
    BookingEntity findByIdWithDetails(Long bookingId);

    boolean existsByOrderId(Long orderId);

    boolean existsByUserIdAndVisitDate(Long userId, LocalDate visitDate);

    @Query("SELECT b FROM BookingEntity b LEFT JOIN FETCH b.order LEFT JOIN FETCH b.user " +
            "WHERE b.bookingStatus = 'CONFIRMED' AND b.ticketSubmissionStatus = 'NOT_SUBMITTED' AND b.visitDate = :visitDate")
    List<BookingEntity> findBookingsForTicketSubmission(LocalDate visitDate);
}