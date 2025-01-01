package com.example.LikeLink.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.LikeLink.Config.DriverLocationWebSocketHandler;
import com.example.LikeLink.Model.AmbulanceDriver;
import com.example.LikeLink.Model.Location;
import com.example.LikeLink.Repository.AmbulanceDriverRepository;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j

public class DriverLocationService {

    @Autowired
    private AmbulanceDriverRepository driverRepository;

    public List<AmbulanceDriver> findNearbyDrivers(Double latitude, Double longitude, Double radiusInKm) {
        // Convert km to meters
        double radiusInMeters = radiusInKm * 1000;
        return driverRepository.findNearbyDrivers(latitude, longitude, radiusInMeters);
    }

    public List<AmbulanceDriver> findDriversWithinRadius(Double latitude, Double longitude, Double radiusInKm) {
        // Convert km to radians (Earth's radius is approximately 6371 km)
        double radiusInRadians = radiusInKm / 6371.0;
        return driverRepository.findDriversWithinRadius(latitude, longitude, radiusInRadians);
    }

    public void updateDriverLocation(String driverId, Double latitude, Double longitude) {
        Optional<AmbulanceDriver> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isPresent()) {
            AmbulanceDriver driver = driverOpt.get();
            driver.updateLocation(latitude, longitude);
            driverRepository.save(driver);
            log.info("Updated location for driver {}: ({}, {})", driverId, latitude, longitude);
        } else {
            log.warn("Driver not found: {}", driverId);
            throw new RuntimeException("Driver not found: " + driverId);
        }
    }
}