package com.example.demo.booking.service;

import com.example.demo.booking.dto.BookingResponseDto;
import com.example.demo.booking.entity.BookingEntity;
import com.example.demo.booking.entity.TicketSubmissionStatus;
import com.example.demo.booking.repository.BookingRepository;
import com.example.demo.common.exception.RecordNotFoundException;
import com.example.demo.common.utils.DateUtils;
import com.example.demo.order.dto.CreatePurchaseDto;
import com.example.demo.order.dto.TicketDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketSubmissionOrchestrator {

    private final ExternalTicketSubmissionService externalService;
    private final BookingRepository bookingRepository;

    @Transactional
    public TicketSubmissionResult processTicketSubmission(
            BookingResponseDto booking,
            CreatePurchaseDto purchaseDto,
            Long orderId) {

        if (booking == null || purchaseDto.getVisitDate() == null) {
            return TicketSubmissionResult.notRequired();
        }

        if (shouldSubmitImmediately(purchaseDto.getVisitDate())) {
            return performImmediateSubmission(booking, purchaseDto, orderId);
        }

        return TicketSubmissionResult.scheduled();
    }

    private boolean shouldSubmitImmediately(LocalDate visitDate) {
        return visitDate.equals(DateUtils.tomorrow());
    }

    private TicketSubmissionResult performImmediateSubmission(
            BookingResponseDto booking,
            CreatePurchaseDto purchaseDto,
            Long orderId) {

        log.info("Visit date is tomorrow, submitting tickets immediately for booking {}", booking.getId());

        try {
            List<String> attractionIds = extractAttractionIds(purchaseDto.getTickets());

            boolean submissionSuccess = externalService.submitTicketsToExternalService(
                    orderId,
                    booking.getId(),
                    purchaseDto.getVisitDate(),
                    attractionIds
            );

            updateBookingSubmissionStatus(booking, submissionSuccess);

            if (submissionSuccess) {
                log.info("Tickets immediately submitted for booking {}", booking.getId());
                return TicketSubmissionResult.immediateSuccess();
            } else {
                log.warn("Immediate ticket submission failed for booking {}", booking.getId());
                return TicketSubmissionResult.immediateFailure();
            }

        } catch (Exception e) {
            log.error("Error during immediate ticket submission for booking {}: {}",
                    booking.getId(), e.getMessage(), e);

            updateBookingSubmissionStatus(booking, false);
            return TicketSubmissionResult.immediateFailure();
        }
    }

    private List<String> extractAttractionIds(List<TicketDto> tickets) {
        return tickets.stream()
                .map(TicketDto::getAttractionId)
                .toList();
    }

    private void updateBookingSubmissionStatus(BookingResponseDto booking, boolean success) {
        try {
            Long bookingId = booking.getId();
            BookingEntity bookingEntity = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RecordNotFoundException("Booking", bookingId));

            TicketSubmissionStatus status = success ?
                    TicketSubmissionStatus.SUBMITTED :
                    TicketSubmissionStatus.FAILED;

            bookingEntity.setTicketSubmissionStatus(status);

            if (success) {
                LocalDateTime now = LocalDateTime.now();
                bookingEntity.setTicketSubmittedAt(now);
                // Update response DTO as well
                booking.setTicketSubmissionStatus(status);
                booking.setTicketSubmittedAt(now);
            } else {
                booking.setTicketSubmissionStatus(status);
            }

            bookingRepository.save(bookingEntity);

        } catch (Exception e) {
            log.error("Failed to update booking submission status for booking {}: {}",
                    booking.getId(), e.getMessage(), e);
            // Don't rethrow - we don't want to fail the entire purchase for a status update issue
        }
    }
}