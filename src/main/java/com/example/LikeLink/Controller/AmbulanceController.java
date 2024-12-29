package com.example.LikeLink.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ambulance")
@PreAuthorize("hasRole('AMBULANCE_DRIVER')")
@RequiredArgsConstructor
public class AmbulanceController {

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok("Ambulance Driver Profile");
    }

    @GetMapping("/requests")
    public ResponseEntity<?> getRequests() {
        return ResponseEntity.ok("Ambulance Requests List");
    }

    @PutMapping("/status")
    public ResponseEntity<?> updateStatus(@RequestParam boolean available) {
        return ResponseEntity.ok("Status Updated");
    }

    @PutMapping("/location")
    public ResponseEntity<?> updateLocation() {
        return ResponseEntity.ok("Location Updated");
    }

    @PostMapping("/accept-request/{requestId}")
    public ResponseEntity<?> acceptRequest(@PathVariable String requestId) {
        return ResponseEntity.ok("Request Accepted");
    }
}
