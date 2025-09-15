package com.example.demo.order.controller;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.exception.IdempotencyException;
import com.example.demo.common.validation.ValidIdempotencyKey;
import com.example.demo.order.dto.CreatePurchaseDto;
import com.example.demo.order.dto.PurchaseResponseDto;
import com.example.demo.order.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchases")
@Validated
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    @Operation(summary = "Purchase 4 tickets with optional visit date booking in one transaction (idempotent)")
    @ApiResponse(responseCode = "200", description = "Purchase completed (with optional booking)")
    @ApiResponse(responseCode = "400", description = "Invalid request data or business rule violation")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "409", description = "Idempotency conflict")
    @ApiResponse(responseCode = "502", description = "External service error")
    public ResponseEntity<PurchaseResponseDto> createPurchase(
            @RequestHeader("Idempotency-Key") @ValidIdempotencyKey String idempotencyKey,
            @Valid @RequestBody CreatePurchaseDto createPurchaseDto) throws BusinessException, IdempotencyException {

        PurchaseResponseDto response = purchaseService.purchaseAndBook(idempotencyKey, createPurchaseDto);
        return ResponseEntity.ok(response);
    }
}