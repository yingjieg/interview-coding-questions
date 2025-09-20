package com.example.demo.order.service;

import com.example.demo.common.exception.BusinessRuleCode;
import com.example.demo.common.exception.BusinessRuleViolationException;
import com.example.demo.common.exception.RecordNotFoundException;
import com.example.demo.order.dto.CreateOrderDto;
import com.example.demo.order.dto.OrderResponseDto;
import com.example.demo.order.entity.OrderEntity;
import com.example.demo.order.entity.OrderItemEntity;
import com.example.demo.order.entity.OrderStatus;
import com.example.demo.order.mapper.OrderMapper;
import com.example.demo.order.repository.OrderRepository;
import com.example.demo.user.UserEntity;
import com.example.demo.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderMapper = orderMapper;
    }

    @Transactional
    public OrderResponseDto createOrder(CreateOrderDto createOrderDto) {
        // Validate user exists
        UserEntity user = userRepository.findByIdOrThrow(createOrderDto.getUserId(), "User");

        // Validate exactly 4 tickets - double check even though DTO validation should catch this
        if (CollectionUtils.isEmpty(createOrderDto.getTickets()) || createOrderDto.getTickets().size() != 4) {
            throw new BusinessRuleViolationException(BusinessRuleCode.INVALID_TICKET_COUNT);
        }

        // Check if user already has max 4 unfinished orders
        long unfinishedOrdersCount = orderRepository.countByUserIdAndOrderStatusNot(createOrderDto.getUserId(), OrderStatus.FINISHED);
        if (unfinishedOrdersCount >= 4) {
            throw new BusinessRuleViolationException(
                    BusinessRuleCode.MAX_UNFINISHED_ORDERS_EXCEEDED,
                    "User cannot have more than 4 unfinished orders. Current unfinished orders: " + unfinishedOrdersCount);
        }

        // Create order entity
        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING);

        // Create order items using MapStruct
        List<OrderItemEntity> orderItems = orderMapper.toOrderItemEntities(createOrderDto.getTickets());
        orderItems.forEach(item -> item.setOrder(order));

        order.setOrderItems(orderItems);

        // Save order
        OrderEntity savedOrder = orderRepository.save(order);

        log.info("Order created successfully for user {}",
                user.getEmail());

        return orderMapper.toResponseDto(savedOrder);
    }

    public OrderResponseDto getOrder(Long orderId) {
        // Note: Using custom query method as it includes items, not BaseRepository method
        OrderEntity order = orderRepository.findByIdWithItems(orderId);
        if (order == null) {
            throw new RecordNotFoundException("Order", orderId);
        }
        return orderMapper.toResponseDto(order);
    }

    public List<OrderResponseDto> getUserOrders(Long userId) {
        List<OrderEntity> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orderMapper.toResponseDtos(orders);
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        OrderEntity order = orderRepository.findByIdOrThrow(orderId, "Order");

        log.info("Updating order {} status from {} to {}", orderId, order.getOrderStatus(), newStatus);

        order.setOrderStatus(newStatus);
        OrderEntity savedOrder = orderRepository.save(order);

        return orderMapper.toResponseDto(savedOrder);
    }

    @Transactional
    public void confirmOrder(Long orderId) {
        updateOrderStatus(orderId, OrderStatus.CONFIRMED);
        log.info("Order {} confirmed", orderId);
    }
}