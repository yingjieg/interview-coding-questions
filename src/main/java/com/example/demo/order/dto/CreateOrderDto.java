package com.example.demo.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderDto {

    @NotNull(message = "User ID is required")
    @Min(value = 1, message = "User ID must be greater than 0")
    private Long userId;

    @NotNull(message = "Tickets are required")
    @Size(min = 4, max = 4, message = "Must select exactly 4 tickets")
    @Valid
    private List<TicketDto> tickets;
}