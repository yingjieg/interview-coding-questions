package com.example.demo.order.mapper;

import com.example.demo.order.dto.OrderResponseDto;
import com.example.demo.order.dto.TicketDto;
import com.example.demo.order.entity.OrderEntity;
import com.example.demo.order.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "totalAmount", ignore = true)
    OrderResponseDto toResponseDto(OrderEntity orderEntity);

    List<OrderResponseDto> toResponseDtos(List<OrderEntity> orderEntities);

    @Mapping(source = "attractionExternalId", target = "attractionExternalId")
    OrderResponseDto.OrderItemResponseDto toOrderItemResponseDto(OrderItemEntity orderItemEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(source = "attractionId", target = "attractionExternalId")
    OrderItemEntity toOrderItemEntity(TicketDto ticketDto);

    List<OrderItemEntity> toOrderItemEntities(List<TicketDto> ticketDtos);
}