package com.example.demo.order.repository;

import com.example.demo.common.repository.BaseRepository;
import com.example.demo.order.entity.OrderEntity;
import com.example.demo.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends BaseRepository<OrderEntity, Long> {

    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    OrderEntity findByIdWithItems(Long orderId);

    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.orderItems WHERE o.orderNumber = :orderNumber")
    OrderEntity findByOrderNumberWithItems(String orderNumber);

    long countByUserIdAndOrderStatusNot(Long userId, OrderStatus orderStatus);

}