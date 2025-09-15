package com.example.demo.order.service;

import com.example.demo.order.dto.CreateOrderDto;
import com.example.demo.order.dto.OrderResponseDto;
import com.example.demo.order.dto.TicketDto;
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

import java.math.BigDecimal;
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
        UserEntity user = userRepository.findById(createOrderDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + createOrderDto.getUserId()));

        // Validate exactly 4 tickets - double check even though DTO validation should catch this
        if (createOrderDto.getTickets() == null || createOrderDto.getTickets().size() != 4) {
            throw new RuntimeException("Must select exactly 4 tickets");
        }

        // Calculate total amount
        BigDecimal totalAmount = createOrderDto.getTickets().stream()
                .map(TicketDto::getUnitPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order entity
        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setOrderStatus(OrderStatus.PENDING);

        // Create order items using MapStruct
        List<OrderItemEntity> orderItems = orderMapper.toOrderItemEntities(createOrderDto.getTickets());
        orderItems.forEach(item -> item.setOrder(order));

        order.setOrderItems(orderItems);

        // Save order
        OrderEntity savedOrder = orderRepository.save(order);

        log.info("Order created successfully for user {} with total amount {}",
                user.getEmail(), totalAmount);

        return orderMapper.toResponseDto(savedOrder);
    }

    public OrderResponseDto getOrder(Long orderId) {
        OrderEntity order = orderRepository.findByIdWithItems(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }
        return orderMapper.toResponseDto(order);
    }

    public List<OrderResponseDto> getUserOrders(Long userId) {
        List<OrderEntity> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orderMapper.toResponseDtos(orders);
    }
}