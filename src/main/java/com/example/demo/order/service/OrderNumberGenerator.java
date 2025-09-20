package com.example.demo.order.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OrderNumberGenerator {

    private static final String ORDER_PREFIX = "SP"; // Shanghai Pass

    /**
     * Generates a readable order number in format: SP2024001234
     * - SP: Shanghai Pass prefix
     * - 2024: Current year
     * - 001234: Zero-padded 6-digit order ID
     *
     * @param orderId The database order ID
     * @return Formatted order number (e.g., SP2024001234)
     */
    public String generateOrderNumber(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }

        int currentYear = LocalDateTime.now().getYear();
        String paddedOrderId = String.format("%06d", orderId);

        return ORDER_PREFIX + currentYear + paddedOrderId;
    }

    /**
     * Extracts the original order ID from a formatted order number
     *
     * @param orderNumber The formatted order number (e.g., SP2024001234)
     * @return The original order ID
     */
    public Long extractOrderId(String orderNumber) {
        if (orderNumber == null || orderNumber.length() != 12 || !orderNumber.startsWith(ORDER_PREFIX)) {
            throw new IllegalArgumentException("Invalid order number format. Expected format: SP2024001234");
        }

        // Extract the last 6 digits (order ID part)
        String orderIdPart = orderNumber.substring(6); // Skip "SP2024" = 6 characters

        try {
            return Long.parseLong(orderIdPart);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid order number format. Order ID part is not numeric: " + orderIdPart);
        }
    }

    /**
     * Validates if the order number follows the correct format
     *
     * @param orderNumber The order number to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.length() != 12) {
            return false;
        }

        if (!orderNumber.startsWith(ORDER_PREFIX)) {
            return false;
        }

        // Check year part (4 digits)
        String yearPart = orderNumber.substring(2, 6);
        try {
            int year = Integer.parseInt(yearPart);
            if (year < 2020 || year > 2100) { // Reasonable year range
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        // Check order ID part (6 digits)
        String orderIdPart = orderNumber.substring(6);
        try {
            Long.parseLong(orderIdPart);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}