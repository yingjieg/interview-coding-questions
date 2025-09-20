package com.example.demo.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateBookingDto {

    @NotNull(message = "Visit date is required")
    @Future(message = "Visit date must be in the future")
    private LocalDate visitDate;
}