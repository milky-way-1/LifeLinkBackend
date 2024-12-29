package com.example.LikeLink.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hospital")
@PreAuthorize("hasRole('HOSPITAL')")
@RequiredArgsConstructor
public class HospitalController {

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok("Hospital Profile");
    }

    @GetMapping("/blood-requests")
    public ResponseEntity<?> getBloodRequests() {
        return ResponseEntity.ok("Blood Requests List");
    }

    @GetMapping("/ambulance-requests")
    public ResponseEntity<?> getAmbulanceRequests() {
        return ResponseEntity.ok("Ambulance Requests List");
    }

    @PostMapping("/blood-request/{requestId}/process")
    public ResponseEntity<?> processBloodRequest(@PathVariable String requestId) {
        return ResponseEntity.ok("Blood Request Processed");
    }

    @PutMapping("/update-beds")
    public ResponseEntity<?> updateBedAvailability() {
        return ResponseEntity.ok("Bed Availability Updated");
    }
}
