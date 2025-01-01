package com.example.LikeLink.Controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.LikeLink.Enum.BookingStatus;
import com.example.LikeLink.Model.Booking;
import com.example.LikeLink.Service.BookingService;
import com.example.LikeLink.dto.request.BookingRequest;
import com.example.LikeLink.dto.response.BookingStatusUpdate;
import com.example.LikeLink.dto.response.DriverResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/request")
    public ResponseEntity<?> requestAmbulance(@RequestBody BookingRequest request) {
        try {
            Booking booking = new Booking();
            booking.setUserId(request.getUserId());
            booking.setPickupLocation(request.getPickupLocation());
            booking.setDestinationLocation(request.getDestinationLocation());
            booking.setStatus(BookingStatus.PENDING);
            
            bookingService.processBookingRequest(booking);
            
            return ResponseEntity.ok(Map.of(
                "message", "Processing your request",
                "bookingId", booking.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to process booking request"));
        }
    }

    @PostMapping("/driver/response")
    public ResponseEntity<?> handleDriverResponse(@RequestBody DriverResponse response) {
        bookingService.handleDriverResponse(
            response.getBookingId(), 
            response.getDriverId(), 
            response.isAccepted()
        );
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{bookingId}/status")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable String bookingId,
            @RequestBody BookingStatusUpdate statusUpdate) {
        try {
            bookingService.updateBookingStatus(bookingId, statusUpdate.getStatus());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update booking status"));
        }
    }
}