package com.example.demo.order.service;

import com.example.demo.booking.service.BookingService;
import com.example.demo.booking.service.TicketSubmissionOrchestrator;
import com.example.demo.booking.service.TicketSubmissionResult;
import com.example.demo.booking.dto.BookingResponseDto;
import com.example.demo.booking.dto.CreateBookingDto;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.BusinessRuleViolationException;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.exception.IdempotencyException;
import com.example.demo.idempotency.IdempotencyService;
import com.example.demo.order.dto.*;
import com.example.demo.user.UserEntity;
import com.example.demo.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
public class PurchaseService {

    private final OrderService orderService;
    private final BookingService bookingService;
    private final IdempotencyService idempotencyService;
    private final UserRepository userRepository;
    private final TicketSubmissionOrchestrator ticketSubmissionOrchestrator;

    public PurchaseService(OrderService orderService,
                          BookingService bookingService,
                          IdempotencyService idempotencyService,
                          UserRepository userRepository,
                          TicketSubmissionOrchestrator ticketSubmissionOrchestrator) {
        this.orderService = orderService;
        this.bookingService = bookingService;
        this.idempotencyService = idempotencyService;
        this.userRepository = userRepository;
        this.ticketSubmissionOrchestrator = ticketSubmissionOrchestrator;
    }

    public PurchaseResponseDto purchaseAndBook(String idempotencyKey, CreatePurchaseDto createPurchaseDto)
            throws BusinessException {
        // Get user for idempotency tracking
        UserEntity user = userRepository.findById(createPurchaseDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User", createPurchaseDto.getUserId()));

        // Execute with idempotency protection
        try {
            return idempotencyService.executeIdempotent(
                    idempotencyKey,
                    user,
                    createPurchaseDto,
                    () -> {
                        try {
                            return performPurchase(createPurchaseDto);
                        } catch (BusinessException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    PurchaseResponseDto.class
            );
        } catch (IdempotencyException e) {
            log.warn("Idempotency error for user {}: {}", createPurchaseDto.getUserId(), e.getMessage());
            throw e; // Re-throw idempotency exceptions as-is
        } catch (RuntimeException e) {
            // Unwrap BusinessException if it was wrapped
            if (e.getCause() instanceof BusinessException) {
                throw (BusinessException) e.getCause();
            }
            log.error("Purchase operation failed for user {}: {}", createPurchaseDto.getUserId(), e.getMessage());
            throw new BusinessException("PURCHASE_FAILED", "Purchase operation failed", e);
        }
    }

    @Transactional
    protected PurchaseResponseDto performPurchase(CreatePurchaseDto createPurchaseDto) throws BusinessException {
        try {
            // Create order
            OrderResponseDto order = createOrder(createPurchaseDto);

            // Create booking if visit date provided
            BookingResponseDto booking = createBookingIfRequired(createPurchaseDto, order);

            // Handle ticket submission
            TicketSubmissionResult ticketResult = ticketSubmissionOrchestrator.processTicketSubmission(
                booking, createPurchaseDto, order.getId());

            // Build response
            PurchaseResponseDto response = buildResponse(order, booking, ticketResult);

            logSuccess(order, booking);
            return response;

        } catch (BusinessRuleViolationException e) {
            log.warn("Business rule violation during purchase for user {}: {}",
                createPurchaseDto.getUserId(), e.getMessage());
            throw e; // Re-throw business rule violations as-is
        } catch (Exception e) {
            log.error("Unexpected error during purchase for user {}: {}",
                createPurchaseDto.getUserId(), e.getMessage(), e);
            throw new BusinessException("PURCHASE_FAILED", "Purchase operation failed", e);
        }
    }

    private OrderResponseDto createOrder(CreatePurchaseDto createPurchaseDto) {
        CreateOrderDto createOrderDto = new CreateOrderDto();
        createOrderDto.setUserId(createPurchaseDto.getUserId());
        createOrderDto.setTickets(createPurchaseDto.getTickets());

        OrderResponseDto order = orderService.createOrder(createOrderDto);
        log.info("Order created: {}", order.getId());
        return order;
    }

    private BookingResponseDto createBookingIfRequired(CreatePurchaseDto createPurchaseDto, OrderResponseDto order) {
        if (createPurchaseDto.getVisitDate() == null) {
            log.info("No visit date provided - purchase only for order {}", order.getId());
            return null;
        }

        CreateBookingDto createBookingDto = new CreateBookingDto();
        createBookingDto.setOrderId(order.getId());
        createBookingDto.setVisitDate(createPurchaseDto.getVisitDate());
        createBookingDto.setPassport(createPurchaseDto.getPassport());

        BookingResponseDto booking = bookingService.createBooking(createBookingDto);
        log.info("Booking created: {} for order {}", booking.getId(), order.getId());
        return booking;
    }

    private PurchaseResponseDto buildResponse(OrderResponseDto order, BookingResponseDto booking, TicketSubmissionResult ticketResult) {
        String message = booking == null ?
            "Purchase completed successfully. You can book a visit date separately if needed." :
            ticketResult.message();

        PurchaseResponseDto response = new PurchaseResponseDto();
        response.setOrder(order);
        response.setBooking(booking);
        response.setMessage(message);
        return response;
    }

    private void logSuccess(OrderResponseDto order, BookingResponseDto booking) {
        if (booking != null) {
            log.info("Purchase and booking completed for order {} with booking {}",
                order.getId(), booking.getId());
        } else {
            log.info("Purchase-only completed for order {}", order.getId());
        }
    }
}