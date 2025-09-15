package com.example.demo.order.entity;

import com.example.demo.order.entity.OrderEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "attraction_name", nullable = false, length = 100)
    private String attractionName;

    @Column(name = "attraction_external_id", nullable = false, length = 50)
    private String attractionExternalId;
}