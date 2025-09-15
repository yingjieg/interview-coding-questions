package com.example.demo.order.controller;

import com.example.demo.common.exception.BusinessRuleCode;
import com.example.demo.common.exception.BusinessRuleViolationException;
import com.example.demo.order.dto.CreateOrderDto;
import com.example.demo.order.dto.OrderResponseDto;
import com.example.demo.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Create a new order with 4 tickets (idempotent)")
    @ApiResponse(responseCode = "200", description = "Order created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    public ResponseEntity<OrderResponseDto> createOrder(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateOrderDto createOrderDto) {

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new BusinessRuleViolationException(BusinessRuleCode.MISSING_IDEMPOTENCY_KEY);
        }

        OrderResponseDto order = orderService.createOrder(createOrderDto);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long orderId) {
        OrderResponseDto order = orderService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all orders for a user")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<List<OrderResponseDto>> getUserOrders(@PathVariable Long userId) {
        List<OrderResponseDto> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }
}