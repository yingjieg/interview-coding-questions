package com.example.demo.order.service;

import com.example.demo.booking.service.BookingService;
import com.example.demo.booking.service.TicketSubmissionOrchestrator;
import com.example.demo.booking.service.TicketSubmissionResult;
import com.example.demo.booking.dto.BookingResponseDto;
import com.example.demo.booking.dto.CreateBookingDto;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.BusinessRuleViolationException;
import com.example.demo.common.exception.RecordNotFoundException;
import com.example.demo.common.exception.IdempotencyException;
import com.example.demo.idempotency.IdempotencyService;
import com.example.demo.order.dto.*;
import com.example.demo.order.entity.OrderEntity;
import com.example.demo.order.repository.OrderRepository;
import com.example.demo.payment.entity.PaymentEntity;
import com.example.demo.payment.entity.PaymentType;
import com.example.demo.payment.service.PayPalPaymentService;
import com.example.demo.payment.service.StripePaymentService;
import com.example.demo.payment.service.StripeService;
import com.example.demo.payment.dto.PaymentMapper;
import com.example.demo.payment.dto.StripePaymentRequest;
import com.example.demo.payment.dto.StripePaymentResponse;
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
    private final PayPalPaymentService payPalPaymentService;
    private final StripePaymentService stripePaymentService;
    private final StripeService stripeService;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;

    public PurchaseService(OrderService orderService,
                           BookingService bookingService,
                           IdempotencyService idempotencyService,
                           UserRepository userRepository,
                           TicketSubmissionOrchestrator ticketSubmissionOrchestrator,
                           PayPalPaymentService payPalPaymentService,
                           StripePaymentService stripePaymentService,
                           StripeService stripeService,
                           OrderRepository orderRepository,
                           PaymentMapper paymentMapper) {
        this.orderService = orderService;
        this.bookingService = bookingService;
        this.idempotencyService = idempotencyService;
        this.userRepository = userRepository;
        this.ticketSubmissionOrchestrator = ticketSubmissionOrchestrator;
        this.payPalPaymentService = payPalPaymentService;
        this.stripePaymentService = stripePaymentService;
        this.stripeService = stripeService;
        this.orderRepository = orderRepository;
        this.paymentMapper = paymentMapper;
    }

    public PurchaseResponseDto purchaseAndBook(String idempotencyKey, CreatePurchaseDto createPurchaseDto)
            throws BusinessException {
        // Get user for idempotency tracking
        UserEntity user = userRepository.findById(createPurchaseDto.getUserId())
                .orElseThrow(() -> new RecordNotFoundException("User", createPurchaseDto.getUserId()));

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

            // Create payment for the order
            PaymentEntity payment = createPayment(order, createPurchaseDto.getPaymentMethod());

            // Process payment based on method
            PaymentEntity processedPayment = processPayment(payment, createPurchaseDto);

            // Create booking if visit date provided
            BookingResponseDto booking = createBookingIfRequired(createPurchaseDto, order);

            // Handle ticket submission
            TicketSubmissionResult ticketResult = ticketSubmissionOrchestrator.processTicketSubmission(
                    booking, createPurchaseDto, order.getId());

            // Build response
            PurchaseResponseDto response = buildResponse(order, booking, ticketResult, processedPayment);

            logSuccess(order, booking, processedPayment);
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

    private PaymentEntity createPayment(OrderResponseDto orderDto, PaymentType paymentMethod) {
        // Get the full order entity to create payment
        OrderEntity order = orderRepository.findById(orderDto.getId())
                .orElseThrow(() -> new RecordNotFoundException("Order", orderDto.getId()));

        PaymentEntity payment;
        switch (paymentMethod) {
            case STRIPE -> {
                payment = stripePaymentService.createStripePayment(order, orderDto.getTotalAmount());
                log.info("Stripe payment created: {} for order: {}", payment.getId(), order.getId());
            }
            case PAYPAL -> {
                payment = payPalPaymentService.createPayPalPayment(order, orderDto.getTotalAmount());
                log.info("PayPal payment created: {} for order: {}", payment.getId(), order.getId());
            }
            default -> throw new BusinessRuleViolationException(
                    com.example.demo.common.exception.BusinessRuleCode.INVALID_PAYMENT_METHOD,
                    "Unsupported payment method: " + paymentMethod
            );
        }
        return payment;
    }

    private PaymentEntity processPayment(PaymentEntity payment, CreatePurchaseDto createPurchaseDto) {
        return switch (createPurchaseDto.getPaymentMethod()) {
            case PAYPAL -> processPayPalPayment(payment);
            case STRIPE -> processStripePayment(payment, createPurchaseDto);
            default -> throw new BusinessRuleViolationException(
                    com.example.demo.common.exception.BusinessRuleCode.INVALID_PAYMENT_METHOD,
                    "Unsupported payment method: " + createPurchaseDto.getPaymentMethod()
            );
        };
    }

    private PaymentEntity processPayPalPayment(PaymentEntity payment) {
        // Build PayPal URLs - in real implementation, these would come from configuration
        String returnUrl = "http://localhost:8888/api/payments/paypal/success";
        String cancelUrl = "http://localhost:8888/api/payments/paypal/cancel";

        PaymentEntity processedPayment = payPalPaymentService.processPayPalPayment(payment, returnUrl, cancelUrl);
        log.info("PayPal payment processed: {} with PayPal order ID: {}",
                processedPayment.getId(), processedPayment.getPaypalOrderId());
        return processedPayment;
    }

    private PaymentEntity processStripePayment(PaymentEntity payment, CreatePurchaseDto createPurchaseDto) {
        // Create Stripe Payment Intent
        StripePaymentRequest stripeRequest = new StripePaymentRequest();
        stripeRequest.setAmount(payment.getAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue()); // Convert to cents
        stripeRequest.setCurrency(payment.getCurrency().toLowerCase());
        stripeRequest.setDescription("Ticket Booking Order #" + payment.getOrder().getId());
        stripeRequest.setOrderId(payment.getOrder().getId().toString());
        stripeRequest.setUserId(createPurchaseDto.getUserId().toString());

        StripePaymentResponse stripeResponse = stripeService.createPaymentIntent(stripeRequest);

        if (stripeResponse.getErrorMessage() != null) {
            log.error("Stripe Payment Intent creation failed: {}", stripeResponse.getErrorMessage());
            throw new BusinessException("STRIPE_PAYMENT_FAILED", "Failed to create Stripe payment: " + stripeResponse.getErrorMessage());
        }

        // Update payment entity with Stripe details
        payment.markStripeCreated(
                stripeResponse.getPaymentIntentId(),
                stripeResponse.getClientSecret(),
                payment.getAmount()
        );

        PaymentEntity processedPayment = stripePaymentService.savePayment(payment);
        log.info("Stripe payment processed: {} with Payment Intent ID: {}",
                processedPayment.getId(), processedPayment.getStripePaymentIntentId());
        return processedPayment;
    }

    private BookingResponseDto createBookingIfRequired(CreatePurchaseDto createPurchaseDto, OrderResponseDto order) {
        if (createPurchaseDto.getVisitDate() == null) {
            log.info("No visit date provided - purchase only for order {}", order.getId());
            return null;
        }

        CreateBookingDto createBookingDto = new CreateBookingDto();
        createBookingDto.setOrderId(order.getId());
        createBookingDto.setVisitDate(createPurchaseDto.getVisitDate());
        createBookingDto.setDocumentType(createPurchaseDto.getDocumentType());
        createBookingDto.setDocumentNumber(createPurchaseDto.getDocumentNumber());

        BookingResponseDto booking = bookingService.createBooking(createBookingDto);
        log.info("Booking created: {} for order {}", booking.getId(), order.getId());
        return booking;
    }

    private PurchaseResponseDto buildResponse(OrderResponseDto order, BookingResponseDto booking, TicketSubmissionResult ticketResult, PaymentEntity payment) {
        String message = booking == null ?
                "Purchase initiated successfully. Complete payment to finalize your order." :
                "Purchase and booking initiated. Complete payment to finalize your order and booking.";

        PurchaseResponseDto response = new PurchaseResponseDto();
        response.setOrder(order);
        response.setBooking(booking);
        response.setPayment(paymentMapper.toDto(payment));
        response.setMessage(message);
        return response;
    }

    private void logSuccess(OrderResponseDto order, BookingResponseDto booking, PaymentEntity payment) {
        if (booking != null) {
            log.info("Purchase and booking initiated for order {} with booking {} and payment {}",
                    order.getId(), booking.getId(), payment.getId());
        } else {
            log.info("Purchase-only initiated for order {} with payment {}",
                    order.getId(), payment.getId());
        }
    }
}