package com.example.LikeLink.Model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.example.LikeLink.Enum.LicenseType;
import com.example.LikeLink.Enum.VerificationStatus;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Document(collection = "ambulance_drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmbulanceDriver {
    
    @Id
    private String id;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Indexed(unique = true)
    private String email; // Reference to user table

    @NotBlank(message = "Date of birth is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date should be in YYYY-MM-DD format")
    private String dateOfBirth;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
    @Indexed(unique = true)
    private String phoneNumber;

    @NotBlank(message = "Current address is required")
    private String currentAddress;

    @NotBlank(message = "Driver's license number is required")
    @Indexed(unique = true)
    private String driversLicenseNumber;

    @NotNull(message = "License type is required")
    private LicenseType licenseType;

    private boolean experienceWithEmergencyVehicle;

    @Min(value = 0, message = "Years of experience cannot be negative")
    private Integer yearsOfEmergencyExperience;

    @NotBlank(message = "Vehicle registration number is required")
    @Indexed(unique = true)
    private String vehicleRegistrationNumber;

    private boolean hasAirConditioning;
    private boolean hasOxygenCylinderHolder;
    private boolean hasStretcher;

    @NotBlank(message = "Insurance policy number is required")
    @Indexed(unique = true)
    private String insurancePolicyNumber;

    @NotBlank(message = "Insurance expiry date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date should be in YYYY-MM-DD format")
    private String insuranceExpiryDate;

    private boolean isAvailable = true;

    @NotNull(message = "Current location is required")
    private Location currentLocation;

    private String profileImageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    

    @AssertTrue(message = "Insurance must not be expired")
    private boolean isInsuranceValid() {
        if (insuranceExpiryDate == null) return false;
        LocalDate expiryDate = LocalDate.parse(insuranceExpiryDate);
        return expiryDate.isAfter(LocalDate.now());
    }

    public void updateLocation(Double latitude, Double longitude) {
        this.currentLocation = new Location(latitude, longitude);
    }
    public boolean isEligibleForService() {
        return verificationStatus == VerificationStatus.VERIFIED &&
               isAvailable &&
               isInsuranceValid();
    }

    @Component
    public class AmbulanceDriverListener extends AbstractMongoEventListener<AmbulanceDriver> {
        
        @Override
        public void onBeforeConvert(BeforeConvertEvent<AmbulanceDriver> event) {
            AmbulanceDriver driver = event.getSource();
            LocalDateTime now = LocalDateTime.now();
            
            if (driver.getCreatedAt() == null) {
                driver.setCreatedAt(now);
            }
            driver.setUpdatedAt(now);
        }
    }
}