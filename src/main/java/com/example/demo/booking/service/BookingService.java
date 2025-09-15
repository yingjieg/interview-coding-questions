package com.example.demo.booking.service;

import com.example.demo.booking.dto.BookingResponseDto;
import com.example.demo.booking.dto.CreateBookingDto;
import com.example.demo.booking.dto.UpdateBookingDto;
import com.example.demo.booking.entity.BookingEntity;
import com.example.demo.booking.entity.BookingStatus;
import com.example.demo.booking.mapper.BookingMapper;
import com.example.demo.booking.repository.BookingRepository;
import com.example.demo.order.entity.OrderEntity;
import com.example.demo.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
        OrderEntity order = orderRepository.findById(createBookingDto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + createBookingDto.getOrderId()));

        // Check if booking already exists for this order
        if (bookingRepository.existsByOrderId(createBookingDto.getOrderId())) {
            throw new RuntimeException("Booking already exists for order: " + createBookingDto.getOrderId());
        }

        // Validate visit date is at least tomorrow
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        if (createBookingDto.getVisitDate().isBefore(tomorrow)) {
            throw new RuntimeException("Visit date must be at least tomorrow. Cannot book for today or past dates.");
        }

        // Create booking entity
        BookingEntity booking = new BookingEntity();
        booking.setOrder(order);
        booking.setUser(order.getUser());
        booking.setVisitDate(createBookingDto.getVisitDate());
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
            throw new RuntimeException("Booking not found with id: " + bookingId);
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
            throw new RuntimeException("Booking not found with id: " + bookingId);
        }

        // Validate new visit date is at least tomorrow
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        if (updateBookingDto.getVisitDate().isBefore(tomorrow)) {
            throw new RuntimeException("Visit date must be at least tomorrow. Cannot book for today or past dates.");
        }

        // Check if the visit date is in the past (cannot update past bookings)
        if (booking.getVisitDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot update booking for past dates");
        }

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
            throw new RuntimeException("Booking not found with id: " + bookingId);
        }

        // Check if the visit date is in the past (cannot cancel past bookings)
        if (booking.getVisitDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot cancel booking for past dates");
        }

        // Call external booking API to cancel
        boolean externalCancelSuccess = externalBookingService.cancelExternalBooking(
                booking.getOrder().getId(), booking.getVisitDate());

        if (externalCancelSuccess) {
            booking.setBookingStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            log.info("Booking cancelled for order {} on date {}",
                    booking.getOrder().getId(), booking.getVisitDate());
        } else {
            throw new RuntimeException("Failed to cancel booking through external system");
        }
    }
}