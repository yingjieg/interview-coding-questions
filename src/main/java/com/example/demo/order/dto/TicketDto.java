package com.example.demo.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketDto {

    @NotBlank(message = "Attraction ID is required")
    @Size(max = 50, message = "Attraction ID must not exceed 50 characters")
    private String attractionId;

    @NotBlank(message = "Attraction name is required")
    @Size(max = 100, message = "Attraction name must not exceed 100 characters")
    private String attractionName;
}