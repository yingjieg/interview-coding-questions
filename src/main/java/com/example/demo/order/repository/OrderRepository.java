package com.example.demo.order.repository;

import com.example.demo.order.entity.OrderEntity;
import com.example.demo.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    OrderEntity findByIdWithItems(Long orderId);

    List<OrderEntity> findByOrderStatusOrderByCreatedAtDesc(OrderStatus orderStatus);
}