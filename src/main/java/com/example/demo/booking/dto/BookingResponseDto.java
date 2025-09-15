package com.example.demo.booking.dto;

import com.example.demo.booking.entity.BookingStatus;
import com.example.demo.booking.entity.TicketSubmissionStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingResponseDto {
    private Long id;
    private Long orderId;
    private Long userId;
    private LocalDate visitDate;
    private BookingStatus bookingStatus;
    private TicketSubmissionStatus ticketSubmissionStatus;
    private LocalDateTime ticketSubmittedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}