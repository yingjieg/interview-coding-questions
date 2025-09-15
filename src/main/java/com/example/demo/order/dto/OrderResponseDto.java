package com.example.demo.order.dto;

import com.example.demo.order.entity.OrderStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDto {
    private Long id;
    private Long userId;
    private BigDecimal totalAmount;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
    private List<OrderItemResponseDto> orderItems;

    @Data
    public static class OrderItemResponseDto {
        private Long id;
        private String attractionName;
        private String attractionExternalId;
        private BigDecimal unitPrice;
    }
}