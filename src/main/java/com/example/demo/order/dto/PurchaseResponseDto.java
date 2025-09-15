package com.example.demo.order.dto;

import com.example.demo.booking.dto.BookingResponseDto;
import lombok.Data;

@Data
public class PurchaseResponseDto {
    private OrderResponseDto order;
    private BookingResponseDto booking;
    private String message;
}