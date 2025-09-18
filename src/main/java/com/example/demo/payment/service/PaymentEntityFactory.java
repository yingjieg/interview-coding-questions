package com.example.demo.payment.service;

import com.example.demo.order.entity.OrderEntity;
import com.example.demo.payment.entity.PaymentEntity;
import com.example.demo.payment.entity.PaymentStatus;
import com.example.demo.payment.entity.PaymentType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentEntityFactory {

    public PaymentEntity createPayment(OrderEntity order, BigDecimal amount, PaymentType paymentType) {
        PaymentEntity payment = new PaymentEntity();
        payment.setOrder(order);
        payment.setPaymentType(paymentType);
        payment.setAmount(amount);
        payment.setCurrency("USD");
        payment.setPaymentStatus(PaymentStatus.PENDING);
        return payment;
    }
}