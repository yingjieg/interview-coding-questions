package com.example.demo.payment.dto;

import com.example.demo.payment.entity.PaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "paypalApprovalUrl", target = "paypalApprovalUrl")
    @Mapping(expression = "java(payment.isExpired())", target = "expired")
    PaymentResponseDto toDto(PaymentEntity payment);
}