package com.example.demo.order.dto;

import com.example.demo.booking.dto.BookingResponseDto;
import com.example.demo.payment.dto.PaymentResponseDto;
import lombok.Data;

@Data
public class PurchaseResponseDto {
    private OrderResponseDto order;
    private BookingResponseDto booking;
    private PaymentResponseDto payment;
    private String message;
}