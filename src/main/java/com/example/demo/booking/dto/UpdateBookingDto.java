package com.example.demo.booking.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
public class UpdateBookingDto {

    @NotNull(message = "Visit date is required")
    @Future(message = "Visit date must be in the future")
    private LocalDate visitDate;
}