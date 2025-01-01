package com.example.LikeLink.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.example.LikeLink.Enum.VerificationStatus;
import com.example.LikeLink.Model.AmbulanceDriver;

import java.util.List;
import java.util.Optional;

public interface AmbulanceDriverRepository extends MongoRepository<AmbulanceDriver, String> {
    
    Optional<AmbulanceDriver> findByEmail(String email);
    
    Optional<AmbulanceDriver> findByPhoneNumber(String phoneNumber);
    
    Optional<AmbulanceDriver> findByDriversLicenseNumber(String licenseNumber);
    
    Optional<AmbulanceDriver> findByVehicleRegistrationNumber(String regNumber);
    
    Optional<AmbulanceDriver> findByInsurancePolicyNumber(String policyNumber);
    
   
    @Query("{ 'currentLocation': { $near: { $geometry: { type: 'Point', coordinates: [?1, ?0] }, $maxDistance: ?2 } } }")
    List<AmbulanceDriver> findNearbyDrivers(Double latitude, Double longitude, Double maxDistanceInMeters);
    
    @Query("{ 'currentLocation.coordinates': { $geoWithin: { $centerSphere: [ [?1, ?0], ?2 ] } } }")
    List<AmbulanceDriver> findDriversWithinRadius(Double latitude, Double longitude, Double radiusInRadians);
    
    
    boolean existsByEmail(String email);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    boolean existsByDriversLicenseNumber(String licenseNumber);
    
}
