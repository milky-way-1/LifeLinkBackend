package com.example.LikeLink.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.LikeLink.Model.Booking;
import com.example.LikeLink.Service.BookingService;
import com.example.LikeLink.dto.request.BookingRequest;
import com.example.LikeLink.dto.response.ApiResponse;
import com.example.LikeLink.dto.response.BookingResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; 
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/request")
    public ResponseEntity<?> requestAmbulance(@Valid @RequestBody BookingRequest request) {
        try {
            BookingResponse response = bookingService.processBooking(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing booking request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to process booking request", null));
        }
    }

    @GetMapping("/driver/assigned")
    public ResponseEntity<?> getAssignedBookings(@RequestParam String driverId) {
        try {
            List<Booking> bookings = bookingService.getAssignedBookings(driverId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Retrieved assigned bookings", bookings));
        } catch (Exception e) {
            log.error("Error retrieving assigned bookings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve assigned bookings", null));
        }
    }
}
