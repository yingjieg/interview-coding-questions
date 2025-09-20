package com.example.demo.booking.service;

import com.example.demo.booking.dto.BookingResponseDto;
import com.example.demo.booking.dto.CreateBookingDto;
import com.example.demo.booking.dto.UpdateBookingDto;
import com.example.demo.booking.entity.BookingEntity;
import com.example.demo.booking.entity.BookingStatus;
import com.example.demo.booking.entity.TicketSubmissionStatus;
import com.example.demo.booking.mapper.BookingMapper;
import com.example.demo.booking.repository.BookingRepository;
import com.example.demo.common.exception.BusinessRuleCode;
import com.example.demo.common.exception.BusinessRuleViolationException;
import com.example.demo.common.exception.ExternalServiceException;
import com.example.demo.common.exception.RecordNotFoundException;
import com.example.demo.common.utils.DateUtils;
import com.example.demo.common.validation.DateValidationUtils;
import com.example.demo.order.entity.OrderEntity;
import com.example.demo.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final OrderRepository orderRepository;
    private final BookingMapper bookingMapper;
    private final ExternalBookingService externalBookingService;

    public BookingService(BookingRepository bookingRepository,
                          OrderRepository orderRepository,
                          BookingMapper bookingMapper,
                          ExternalBookingService externalBookingService) {
        this.bookingRepository = bookingRepository;
        this.orderRepository = orderRepository;
        this.bookingMapper = bookingMapper;
        this.externalBookingService = externalBookingService;
    }

    @Transactional
    public BookingResponseDto createBooking(CreateBookingDto createBookingDto) {
        // Validate order exists
        OrderEntity order = orderRepository.findByIdOrThrow(createBookingDto.getOrderId(), "Order");

        // Check if booking already exists for this order
        if (bookingRepository.existsByOrderId(createBookingDto.getOrderId())) {
            throw new BusinessRuleViolationException(
                    BusinessRuleCode.BOOKING_ALREADY_EXISTS,
                    "Booking already exists for order: " + createBookingDto.getOrderId());
        }

        // Validate visit date is at least tomorrow
        DateValidationUtils.validateVisitDateIsAtLeastTomorrow(createBookingDto.getVisitDate());

        // Check if user already has a booking for the same date
        if (bookingRepository.existsByUserIdAndVisitDate(order.getUser().getId(), createBookingDto.getVisitDate())) {
            throw new BusinessRuleViolationException(
                    BusinessRuleCode.ONE_BOOKING_PER_USER_PER_DAY,
                    "User already has a booking for this date: " + createBookingDto.getVisitDate());
        }

        // Create booking entity
        BookingEntity booking = new BookingEntity();
        booking.setOrder(order);
        booking.setUser(order.getUser());
        booking.setVisitDate(createBookingDto.getVisitDate());
        booking.setDocumentType(createBookingDto.getDocumentType());
        booking.setDocumentNumber(createBookingDto.getDocumentNumber());
        booking.setBookingStatus(BookingStatus.PENDING);

        // Save booking first
        BookingEntity savedBooking = bookingRepository.save(booking);

        // Call external booking API
        boolean externalBookingSuccess = externalBookingService.makeExternalBooking(
                order.getId(), createBookingDto.getVisitDate());

        if (externalBookingSuccess) {
            savedBooking.setBookingStatus(BookingStatus.CONFIRMED);
            savedBooking = bookingRepository.save(savedBooking);
            log.info("Booking created and confirmed for order {} on date {}",
                    order.getId(), createBookingDto.getVisitDate());
        } else {
            log.error("External booking failed for order {}, keeping status as PENDING", order.getId());
        }

        return bookingMapper.toResponseDto(savedBooking);
    }

    public BookingResponseDto getBooking(Long bookingId) {
        BookingEntity booking = bookingRepository.findByIdWithDetails(bookingId);
        if (booking == null) {
            throw new RecordNotFoundException("Booking", bookingId);
        }
        return bookingMapper.toResponseDto(booking);
    }

    public List<BookingResponseDto> getUserBookings(Long userId) {
        List<BookingEntity> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return bookingMapper.toResponseDtos(bookings);
    }

    @Transactional
    public BookingResponseDto updateBooking(Long bookingId, UpdateBookingDto updateBookingDto) {
        BookingEntity booking = bookingRepository.findByIdWithDetails(bookingId);
        if (booking == null) {
            throw new RecordNotFoundException("Booking", bookingId);
        }

        // Validate new visit date is at least tomorrow
        DateValidationUtils.validateVisitDateIsAtLeastTomorrow(updateBookingDto.getVisitDate());

        // Check if the visit date is in the past (cannot update past bookings)
        DateValidationUtils.validateDateIsNotInPastForUpdate(booking.getVisitDate());

        LocalDate oldDate = booking.getVisitDate();
        booking.setVisitDate(updateBookingDto.getVisitDate());
        booking.setBookingStatus(BookingStatus.PENDING);

        BookingEntity savedBooking = bookingRepository.save(booking);

        // Call external booking API to update
        boolean externalUpdateSuccess = externalBookingService.updateExternalBooking(
                booking.getOrder().getId(), oldDate, updateBookingDto.getVisitDate());

        if (externalUpdateSuccess) {
            savedBooking.setBookingStatus(BookingStatus.CONFIRMED);
            savedBooking = bookingRepository.save(savedBooking);
            log.info("Booking updated and confirmed for order {} from {} to {}",
                    booking.getOrder().getId(), oldDate, updateBookingDto.getVisitDate());
        } else {
            log.error("External booking update failed for order {}, keeping status as PENDING",
                    booking.getOrder().getId());
        }

        return bookingMapper.toResponseDto(savedBooking);
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        BookingEntity booking = bookingRepository.findByIdWithDetails(bookingId);
        if (booking == null) {
            throw new RecordNotFoundException("Booking", bookingId);
        }

        // Check if the visit date is in the past (cannot cancel past bookings)
        DateValidationUtils.validateDateIsNotInPastForCancel(booking.getVisitDate());

        // Call external booking API to cancel
        boolean externalCancelSuccess = externalBookingService.cancelExternalBooking(
                booking.getOrder().getId(), booking.getVisitDate());

        if (externalCancelSuccess) {
            booking.setBookingStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            log.info("Booking cancelled for order {} on date {}",
                    booking.getOrder().getId(), booking.getVisitDate());
        } else {
            throw new ExternalServiceException(
                    "EXTERNAL_BOOKING_SERVICE",
                    "Failed to cancel booking through external system");
        }
    }

    @Transactional
    public BookingResponseDto submitTickets(Long bookingId) {
        BookingEntity booking = bookingRepository.findByIdWithDetails(bookingId);
        if (booking == null) {
            throw new RecordNotFoundException("Booking", bookingId);
        }

        // Check if booking is confirmed
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessRuleViolationException(
                    BusinessRuleCode.INVALID_BOOKING_STATUS,
                    "Cannot submit tickets for booking that is not confirmed. Current status: " + booking.getBookingStatus());
        }

        // Check if tickets are already submitted
        if (booking.getTicketSubmissionStatus() == TicketSubmissionStatus.SUBMITTED) {
            throw new BusinessRuleViolationException(
                    BusinessRuleCode.TICKETS_ALREADY_SUBMITTED,
                    "Tickets have already been submitted for this booking");
        }

        // Check if visit date is in the past
        if (booking.getVisitDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleViolationException(
                    BusinessRuleCode.CANNOT_SUBMIT_PAST_TICKETS,
                    "Cannot submit tickets for past visit date: " + booking.getVisitDate());
        }

        // Call external service to submit tickets
        boolean submissionSuccess = externalBookingService.submitTickets(
                booking.getId(), booking.getOrder().getId(), booking.getVisitDate());

        if (submissionSuccess) {
            booking.setTicketSubmissionStatus(TicketSubmissionStatus.SUBMITTED);
            booking.setTicketSubmittedAt(LocalDateTime.now());
            bookingRepository.save(booking);

            log.info("Tickets submitted successfully for booking {} (order {}) on visit date {}",
                    booking.getId(), booking.getOrder().getId(), booking.getVisitDate());
        } else {
            throw new ExternalServiceException(
                    "EXTERNAL_TICKETING_SERVICE",
                    "Failed to submit tickets to external system");
        }

        return bookingMapper.toResponseDto(booking);
    }

    public List<BookingResponseDto> getBookingsForTicketSubmission() {
        // Get bookings that need ticket submission (confirmed, not yet submitted, visit date is tomorrow)
        List<BookingEntity> bookings = bookingRepository.findBookingsForTicketSubmission(DateUtils.tomorrow());
        return bookingMapper.toResponseDtos(bookings);
    }
}