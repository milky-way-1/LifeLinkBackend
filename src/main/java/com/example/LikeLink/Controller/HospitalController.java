package com.example.LikeLink.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.LikeLink.Config.Security.Service.UserDetailsImpl;
import com.example.LikeLink.Model.Booking;
import com.example.LikeLink.Model.Hospital;
import com.example.LikeLink.Model.IncomingPatient;
import com.example.LikeLink.Repository.IncomingPatientRepository;
import com.example.LikeLink.Service.HospitalService;
import com.example.LikeLink.dto.request.HospitalRegistrationRequest;
import com.example.LikeLink.dto.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/hospital")
@PreAuthorize("hasRole('HOSPITAL')")
@RequiredArgsConstructor
@Validated
@Slf4j
public class HospitalController {

    private final HospitalService hospitalService;
    private final IncomingPatientRepository incomingPatientRepository;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Hospital>> registerHospital(
            @Valid @RequestBody HospitalRegistrationRequest request,
            Authentication authentication) {
        log.info("Registering hospital for user: {}", authentication.getName());
        
        // Get user ID from Authentication object
        String userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        Hospital hospital = hospitalService.registerHospital(userId, request);
        
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Hospital registered successfully",
            hospital
        ));
    }

    @GetMapping("/{hospitalId}")
    public ResponseEntity<Hospital> getHospitalDetails(@PathVariable String hospitalId) {
        Hospital hospital = hospitalService.getHospitalById(hospitalId);
        
        if (hospital == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(hospital);
    } 
    


    @GetMapping("/{hospitalId}/incoming-patients")
    public ResponseEntity<?> getIncomingPatients(
            @PathVariable String hospitalId,
            Authentication authentication) {
        try {
            List<IncomingPatient> patients = incomingPatientRepository.findIncomingPatientsById(hospitalId);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            log.error("Error fetching incoming patients for hospital {}: {}", 
                hospitalId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to fetch incoming patients", null));
        }
    }
}