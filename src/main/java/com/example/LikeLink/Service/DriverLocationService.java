package com.example.LikeLink.Service;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;

import com.example.LikeLink.Model.AmbulanceDriver;
import com.example.LikeLink.Model.Location;
import com.example.LikeLink.Repository.AmbulanceDriverRepository;
import com.example.LikeLink.Exception.DriverNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;

@Service
@Slf4j
@RequiredArgsConstructor
public class DriverLocationService {

    private final AmbulanceDriverRepository driverRepository;
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double DEFAULT_SEARCH_RADIUS_KM = 5.0;

    @CacheEvict(value = "nearbyDrivers", allEntries = true)
    public void updateDriverLocation(String email, Location location) {
        AmbulanceDriver driver = driverRepository.findByEmail(email)
            .orElseThrow(() -> new DriverNotFoundException("Driver not found with email: " + email));

        driver.setCurrentLocation(location);
        driver.setUpdatedAt(LocalDateTime.now());
        driverRepository.save(driver);
        
        log.info("Updated location for driver {}: {}", email, location);
    }

    @Cacheable(value = "nearbyDrivers", key = "#latitude + '_' + #longitude + '_' + #radiusKm")
    public List<AmbulanceDriver> findNearbyDrivers(Double latitude, Double longitude, Double radiusKm) {
        double radiusInRadians = radiusKm / EARTH_RADIUS_KM;
        List<AmbulanceDriver> nearbyDrivers = driverRepository.findNearbyDrivers(longitude, latitude, radiusInRadians);
        
        log.info("Found {} nearby drivers within {}km of ({}, {})", 
            nearbyDrivers.size(), radiusKm, latitude, longitude);
        
        return nearbyDrivers;
    }

    public Optional<AmbulanceDriver> findNearestDriver(Double latitude, Double longitude) {
        List<AmbulanceDriver> nearbyDrivers = findNearbyDrivers(latitude, longitude, DEFAULT_SEARCH_RADIUS_KM);
        
        return nearbyDrivers.stream()
            .min(Comparator.comparingDouble(driver -> 
                calculateDistance(
                    latitude, 
                    longitude, 
                    driver.getCurrentLocation().getLatitude(),
                    driver.getCurrentLocation().getLongitude()
                )
            ));
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        
        return EARTH_RADIUS_KM * c;
    }

    public int calculateEstimatedTime(Location driverLocation, Location userLocation) {
        double distance = calculateDistance(
            driverLocation.getLatitude(),
            driverLocation.getLongitude(),
            userLocation.getLatitude(),
            userLocation.getLongitude()
        );
        
        // Assume average speed of 40 km/h in city traffic
        double averageSpeedKmH = 40.0;
        
        // Convert to minutes
        return (int) Math.ceil((distance / averageSpeedKmH) * 60);
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @CacheEvict(value = "nearbyDrivers", allEntries = true)
    public void clearNearbyDriversCache() {
        log.debug("Clearing nearby drivers cache");
    }

    public boolean isDriverAvailable(String driverId) {
        return driverRepository.findById(driverId)
            .map(driver -> driver.getUpdatedAt() != null &&
                 driver.getUpdatedAt().plusMinutes(5).isAfter(LocalDateTime.now()))
            .orElse(false);
    }

    public void validateDriverLocation(String driverId, Location location) {
        // Add validation logic if needed
        // For example, check if the new location is within a reasonable distance from the last location
        AmbulanceDriver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new DriverNotFoundException("Driver not found with id: " + driverId));

        if (driver.getCurrentLocation() != null) {
            double distance = calculateDistance(
                driver.getCurrentLocation().getLatitude(),
                driver.getCurrentLocation().getLongitude(),
                location.getLatitude(),
                location.getLongitude()
            );

            // If distance is more than 100km in 5 minutes, it might be suspicious
            if (distance > 100) {
                log.warn("Suspicious location update for driver {}: Distance {} km", driverId, distance);
                // You might want to handle this case (e.g., flag for review, reject update)
            }
        }
    }
}
