package com.example.demo.order.dto;

import com.example.demo.common.validation.ValidVisitDate;
import lombok.Data;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Data
@ValidVisitDate
public class CreatePurchaseDto {

    @NotNull(message = "User ID is required")
    @Min(value = 1, message = "User ID must be greater than 0")
    private Long userId;

    @Future(message = "Visit date must be in the future")
    private LocalDate visitDate;

    @Size(max = 50, message = "Passport cannot exceed 50 characters")
    private String passport;

    @NotNull(message = "Tickets are required")
    @Size(min = 4, max = 4, message = "Must select exactly 4 tickets")
    @Valid
    private List<TicketDto> tickets;
}