package com.example.demo.booking.entity;

/**
 * Enum representing the types of identification documents accepted for bookings.
 */
public enum DocumentType {
    PASSPORT("Passport"),
    MAINLAND_TRAVEL_PERMIT("Mainland Travel Permit");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return this.name();
    }
}