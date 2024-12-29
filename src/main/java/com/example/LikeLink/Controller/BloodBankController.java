package com.example.LikeLink.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bloodbank")
@PreAuthorize("hasRole('BLOOD_BANK')")
@RequiredArgsConstructor
public class BloodBankController {

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok("Blood Bank Profile");
    }

    @GetMapping("/inventory")
    public ResponseEntity<?> getInventory() {
        return ResponseEntity.ok("Blood Inventory");
    }

    @PostMapping("/update-stock")
    public ResponseEntity<?> updateBloodStock() {
        return ResponseEntity.ok("Blood Stock Updated");
    }

    @GetMapping("/requests")
    public ResponseEntity<?> getBloodRequests() {
        return ResponseEntity.ok("Blood Requests List");
    }

    @PostMapping("/process-request/{requestId}")
    public ResponseEntity<?> processRequest(@PathVariable String requestId) {
        return ResponseEntity.ok("Request Processed");
    }
}
