package com.example.demo.booking.dto;

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

    @NotBlank(message = "Passport is required")
    @Size(max = 50, message = "Passport cannot exceed 50 characters")
    private String passport;
}