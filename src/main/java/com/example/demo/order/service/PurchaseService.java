package com.example.demo.order.service;

import com.example.demo.booking.service.BookingService;
import com.example.demo.booking.service.ExternalTicketSubmissionService;
import com.example.demo.booking.entity.TicketSubmissionStatus;
import com.example.demo.booking.repository.BookingRepository;
import com.example.demo.booking.entity.BookingEntity;
import com.example.demo.booking.dto.BookingResponseDto;
import com.example.demo.booking.dto.CreateBookingDto;
import com.example.demo.idempotency.IdempotencyService;
import com.example.demo.order.dto.*;
import com.example.demo.user.UserEntity;
import com.example.demo.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PurchaseService {

    private final OrderService orderService;
    private final BookingService bookingService;
    private final IdempotencyService idempotencyService;
    private final UserRepository userRepository;
    private final ExternalTicketSubmissionService externalTicketSubmissionService;
    private final BookingRepository bookingRepository;

    public PurchaseService(OrderService orderService,
                          BookingService bookingService,
                          IdempotencyService idempotencyService,
                          UserRepository userRepository,
                          ExternalTicketSubmissionService externalTicketSubmissionService,
                          BookingRepository bookingRepository) {
        this.orderService = orderService;
        this.bookingService = bookingService;
        this.idempotencyService = idempotencyService;
        this.userRepository = userRepository;
        this.externalTicketSubmissionService = externalTicketSubmissionService;
        this.bookingRepository = bookingRepository;
    }

    public PurchaseResponseDto purchaseAndBook(String idempotencyKey, CreatePurchaseDto createPurchaseDto) {
        // Get user for idempotency tracking
        UserEntity user = userRepository.findById(createPurchaseDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + createPurchaseDto.getUserId()));

        // Execute with idempotency protection
        return idempotencyService.executeIdempotent(
                idempotencyKey,
                user,
                createPurchaseDto,
                () -> performPurchase(createPurchaseDto),
                PurchaseResponseDto.class
        );
    }

    @Transactional
    protected PurchaseResponseDto performPurchase(CreatePurchaseDto createPurchaseDto) {
        // Create order using existing OrderService
        CreateOrderDto createOrderDto = new CreateOrderDto();
        createOrderDto.setUserId(createPurchaseDto.getUserId());
        createOrderDto.setTickets(createPurchaseDto.getTickets());

        OrderResponseDto order = orderService.createOrder(createOrderDto);
        log.info("Order created: {}", order.getId());

        BookingResponseDto booking = null;
        String message;

        // If visitDate is provided, also create booking
        if (createPurchaseDto.getVisitDate() != null) {
            CreateBookingDto createBookingDto = new CreateBookingDto();
            createBookingDto.setOrderId(order.getId());
            createBookingDto.setVisitDate(createPurchaseDto.getVisitDate());

            booking = bookingService.createBooking(createBookingDto);
            log.info("Booking created: {}", booking.getId());

            // Check if visit date is tomorrow - if so, submit tickets immediately
            LocalDate tomorrow = LocalDate.now().plusDays(1);

            if (createPurchaseDto.getVisitDate().equals(tomorrow)) {
                log.info("Visit date is tomorrow, submitting tickets immediately for booking {}", booking.getId());

                // Get the actual booking entity to update ticket submission status
                Long bookingId = booking.getId();
                BookingEntity bookingEntity = bookingRepository.findById(bookingId)
                        .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

                // Extract attraction IDs from order
                List<String> attractionIds = createPurchaseDto.getTickets().stream()
                        .map(TicketDto::getAttractionId)
                        .collect(Collectors.toList());

                // Submit tickets immediately
                boolean submissionSuccess = externalTicketSubmissionService.submitTicketsToExternalService(
                        order.getId(),
                        booking.getId(),
                        createPurchaseDto.getVisitDate(),
                        attractionIds
                );

                if (submissionSuccess) {
                    bookingEntity.setTicketSubmissionStatus(TicketSubmissionStatus.SUBMITTED);
                    bookingEntity.setTicketSubmittedAt(LocalDateTime.now());
                    bookingRepository.save(bookingEntity);

                    // Update response with new status
                    booking.setTicketSubmissionStatus(TicketSubmissionStatus.SUBMITTED);
                    booking.setTicketSubmittedAt(LocalDateTime.now());

                    message = "Purchase, booking confirmed, and tickets submitted successfully (ready for tomorrow's visit)";
                    log.info("Tickets immediately submitted for booking {}", booking.getId());
                } else {
                    bookingEntity.setTicketSubmissionStatus(TicketSubmissionStatus.FAILED);
                    bookingRepository.save(bookingEntity);

                    booking.setTicketSubmissionStatus(TicketSubmissionStatus.FAILED);
                    message = "Purchase and booking confirmed, but ticket submission failed (will retry automatically)";
                    log.warn("Immediate ticket submission failed for booking {}", booking.getId());
                }
            } else {
                message = booking.getBookingStatus().toString().equals("CONFIRMED")
                    ? "Purchase and booking confirmed successfully (tickets will be submitted 24 hours before visit)"
                    : "Purchase completed but booking is pending external confirmation";
            }
        } else {
            // Purchase only - no booking
            message = "Purchase completed successfully. You can book a visit date separately if needed.";
            log.info("Purchase-only completed for order {}", order.getId());
        }

        // Build response
        PurchaseResponseDto response = new PurchaseResponseDto();
        response.setOrder(order);
        response.setBooking(booking);
        response.setMessage(message);

        if (booking != null) {
            log.info("Purchase and booking completed for order {} with booking {}", order.getId(), booking.getId());
        } else {
            log.info("Purchase completed for order {}", order.getId());
        }

        return response;
    }
}