package com.example.demo.booking.mapper;

import com.example.demo.booking.dto.BookingResponseDto;
import com.example.demo.booking.entity.BookingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookingMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "user.id", target = "userId")
    BookingResponseDto toResponseDto(BookingEntity bookingEntity);

    List<BookingResponseDto> toResponseDtos(List<BookingEntity> bookingEntities);
}