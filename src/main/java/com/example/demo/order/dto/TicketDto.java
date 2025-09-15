package com.example.demo.order.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class TicketDto {

    @NotBlank(message = "Attraction ID is required")
    @Size(max = 50, message = "Attraction ID must not exceed 50 characters")
    private String attractionId;

    @NotBlank(message = "Attraction name is required")
    @Size(max = 100, message = "Attraction name must not exceed 100 characters")
    private String attractionName;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Unit price format is invalid")
    private BigDecimal unitPrice;
}