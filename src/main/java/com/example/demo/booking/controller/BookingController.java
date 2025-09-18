package com.example.demo.booking.controller;

import com.example.demo.booking.dto.BookingResponseDto;
import com.example.demo.booking.dto.CreateBookingDto;
import com.example.demo.booking.dto.UpdateBookingDto;
import com.example.demo.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(summary = "Create a booking for visit date")
    @ApiResponse(responseCode = "200", description = "Booking created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data or date constraints")
    public ResponseEntity<BookingResponseDto> createBooking(@Valid @RequestBody CreateBookingDto createBookingDto) {
        BookingResponseDto booking = bookingService.createBooking(createBookingDto);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking by ID")
    @ApiResponse(responseCode = "200", description = "Booking found")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    public ResponseEntity<BookingResponseDto> getBooking(@PathVariable Long bookingId) {
        BookingResponseDto booking = bookingService.getBooking(bookingId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all bookings for a user")
    @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully")
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(@PathVariable Long userId) {
        List<BookingResponseDto> bookings = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/{bookingId}")
    @Operation(summary = "Update booking visit date")
    @ApiResponse(responseCode = "200", description = "Booking updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid date or booking cannot be updated")
    public ResponseEntity<BookingResponseDto> updateBooking(@PathVariable Long bookingId,
                                                            @Valid @RequestBody UpdateBookingDto updateBookingDto) {
        BookingResponseDto booking = bookingService.updateBooking(bookingId, updateBookingDto);
        return ResponseEntity.ok(booking);
    }

    @DeleteMapping("/{bookingId}")
    @Operation(summary = "Cancel a booking")
    @ApiResponse(responseCode = "200", description = "Booking cancelled successfully")
    @ApiResponse(responseCode = "400", description = "Booking cannot be cancelled")
    public ResponseEntity<String> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok("Booking cancelled successfully");
    }
}