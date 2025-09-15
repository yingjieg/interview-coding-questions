package com.example.demo.order.controller;

import com.example.demo.order.dto.CreatePurchaseDto;
import com.example.demo.order.dto.PurchaseResponseDto;
import com.example.demo.order.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    @Operation(summary = "Purchase 4 tickets with optional visit date booking in one transaction (idempotent)")
    @ApiResponse(responseCode = "200", description = "Purchase completed (with optional booking)")
    @ApiResponse(responseCode = "400", description = "Invalid request data or business rule violation")
    public ResponseEntity<PurchaseResponseDto> createPurchase(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreatePurchaseDto createPurchaseDto) {

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new RuntimeException("Idempotency-Key header is required for purchase operations");
        }

        PurchaseResponseDto response = purchaseService.purchaseAndBook(idempotencyKey, createPurchaseDto);
        return ResponseEntity.ok(response);
    }
}