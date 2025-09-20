package com.example.demo.order.dto;

import com.example.demo.booking.entity.DocumentType;
import com.example.demo.common.validation.ValidVisitDate;
import com.example.demo.payment.entity.PaymentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@ValidVisitDate
public class CreatePurchaseDto {

    private Long userId;

    @Future(message = "Visit date must be in the future")
    private LocalDate visitDate;

    private DocumentType documentType;

    @Size(max = 50, message = "Document number cannot exceed 50 characters")
    private String documentNumber;

    @NotNull(message = "Tickets are required")
    @Size(min = 4, max = 4, message = "Must select exactly 4 tickets")
    @Valid
    private List<TicketDto> tickets;

    @NotNull(message = "Payment method is required")
    private PaymentType paymentMethod = PaymentType.PAYPAL; // Default to PayPal for backward compatibility
}