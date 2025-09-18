package com.example.demo.booking.dto;

import com.example.demo.booking.entity.DocumentType;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Data
public class CreateBookingDto {

    @NotNull(message = "Order ID is required")
    @Min(value = 1, message = "Order ID must be greater than 0")
    private Long orderId;

    @NotNull(message = "Visit date is required")
    @Future(message = "Visit date must be in the future")
    private LocalDate visitDate;

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "Document number is required")
    @Size(max = 50, message = "Document number cannot exceed 50 characters")
    private String documentNumber;
}