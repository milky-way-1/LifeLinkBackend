package com.example.LikeLink.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.LikeLink.Enum.BookingStatus;
import com.example.LikeLink.Exception.BookingException;
import com.example.LikeLink.Model.AmbulanceDriver;
import com.example.LikeLink.Model.Booking;
import com.example.LikeLink.Model.Hospital;
import com.example.LikeLink.Model.Location;
import com.example.LikeLink.Repository.BookingRepository;
import com.example.LikeLink.Repository.HospitalRepository;
import com.example.LikeLink.dto.request.BookingRequest;
import com.example.LikeLink.dto.response.BookingResponse;
import com.example.LikeLink.dto.response.HospitalResponse;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final DriverLocationService driverLocationService;
    private final HospitalRepository hospitalRepository;
    private static final double SEARCH_RADIUS_KM = 5.0;
    
    
    private List<AmbulanceDriver> findNearbyDrivers(Location pickupLocation) {
        try {
            if (pickupLocation == null || pickupLocation.getCoordinates() == null) {
                throw new BookingException("Invalid pickup location");
            }

            List<AmbulanceDriver> drivers = driverLocationService.findNearbyDrivers(
                    pickupLocation.getLatitude(),
                pickupLocation.getLongitude()
            );
            
            log.info("Found {} drivers within {}km", drivers.size(), SEARCH_RADIUS_KM);
            return drivers;

        } catch (Exception e) {
            log.error("Error finding nearby drivers: {}", e.getMessage(), e);
            throw new BookingException("Unable to find nearby drivers", e);
        }
    }
    
    public HospitalResponse findNearestHospital(Location userLocation) {
        try {
            log.info("Finding nearest hospital for location: {}, {}", 
                userLocation.getLatitude(), userLocation.getLongitude());

            List<Hospital> allHospitals = hospitalRepository.findAll();
            if (allHospitals.isEmpty()) {
                throw new BookingException("No hospitals found in the system");
            }

            Hospital nearestHospital = allHospitals.stream()
                .min((h1, h2) -> {
                    double dist1 = calculateDistance(
                        h1.getLatitude(),
                        h1.getLongitude(),
                        userLocation.getLatitude(),
                        userLocation.getLongitude()
                    );
                    double dist2 = calculateDistance(
                        h2.getLatitude(),
                        h2.getLongitude(),
                        userLocation.getLatitude(),
                        userLocation.getLongitude()
                    );
                    return Double.compare(dist1, dist2);
                })
                .orElseThrow(() -> new BookingException("No hospital found"));

            double distance = calculateDistance(
                nearestHospital.getLatitude(),
                nearestHospital.getLongitude(),
                userLocation.getLatitude(),
                userLocation.getLongitude()
            );

            // Convert to HospitalResponse
            HospitalResponse response = new HospitalResponse();
            response.setHospitalId(nearestHospital.getId());
            response.setHospitalName(nearestHospital.getHospitalName());
            response.setLatitude(nearestHospital.getLatitude());
            response.setLongitude(nearestHospital.getLongitude());

            log.info("Found nearest hospital: {} at distance: {}km", 
                nearestHospital.getHospitalName());

            return response;

        } catch (Exception e) {
            log.error("Error finding nearest hospital: {}", e.getMessage(), e);
            throw new BookingException("Unable to find nearest hospital", e);
        }
    }
   

    @Transactional
    public BookingResponse processBooking(BookingRequest request) {
        try {
            log.info("Processing booking request for user: {}", request.getUserId());
            
            validateBookingRequest(request); 

            // First find nearest hospital
            HospitalResponse nearestHospital = findNearestHospital(request.getPickupLocation());
            log.info("Found nearest hospital: {}", nearestHospital.getHospitalName());

            // Update destination location to nearest hospital
            request.setDestinationLocation(new Location(
                nearestHospital.getLatitude(),
                nearestHospital.getLongitude()
            ));
            
            Booking booking = new Booking();

            List<AmbulanceDriver> nearbyDrivers = findNearbyDrivers(request.getPickupLocation());
            if (nearbyDrivers.isEmpty()) {
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                return new BookingResponse("No drivers available", null, "No nearby drivers found");
            }

            // Find nearest driver
            AmbulanceDriver nearestDriver = findNearestDriver(
                nearbyDrivers, 
                request.getPickupLocation()
            );

            booking.setDriverId(nearestDriver.getId());
            booking.setStatus(BookingStatus.ASSIGNED);
            booking.setPickupLocation(request.getPickupLocation()); 
            booking.setDestinationLocation(request.getDestinationLocation()); 
            booking.setUserId(request.getUserId());
            booking.setCreatedAt(LocalDateTime.now());
            bookingRepository.save(booking);

            return new BookingResponse(
                "Driver assigned successfully",
                booking.getId(),
                null,
                BookingStatus.ASSIGNED.toString(),
                nearestDriver.getId(),
                calculateEstimatedTime(nearestDriver.getCurrentLocation(), request.getPickupLocation())
            );

        } catch (Exception e) {
            log.error("Error processing booking request", e);
            throw new RuntimeException("Failed to process booking request", e);
        }
    }
    
    private void validateBookingRequest(BookingRequest request) {
        if (request == null) {
            throw new BookingException("Booking request cannot be null");
        }
        
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new BookingException("User ID is required");
        }
        
        if (request.getPickupLocation() == null || 
            request.getPickupLocation().getCoordinates() == null || 
            request.getPickupLocation().getCoordinates().length != 2) {
            throw new BookingException("Invalid pickup location");
        }

        if (request.getDestinationLocation() == null || 
            request.getDestinationLocation().getCoordinates() == null || 
            request.getDestinationLocation().getCoordinates().length != 2) {
            throw new BookingException("Invalid drop location");
        }
    }

    private AmbulanceDriver findNearestDriver(List<AmbulanceDriver> drivers, Location pickupLocation) {
        return drivers.stream()
            .min((d1, d2) -> {
                double dist1 = calculateDistance(
                    d1.getCurrentLocation().getLatitude(),
                    d1.getCurrentLocation().getLongitude(),
                    pickupLocation.getLatitude(),
                    pickupLocation.getLongitude()
                );
                double dist2 = calculateDistance(
                    d2.getCurrentLocation().getLatitude(),
                    d2.getCurrentLocation().getLongitude(),
                    pickupLocation.getLatitude(),
                    pickupLocation.getLongitude()
                );
                return Double.compare(dist1, dist2);
            })
            .orElseThrow(() -> new RuntimeException("No drivers available"));
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula
        double R = 6371; // Earth's radius in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private Integer calculateEstimatedTime(Location driverLocation, Location pickupLocation) {
        double distance = calculateDistance(
            driverLocation.getLatitude(),
            driverLocation.getLongitude(),
            pickupLocation.getLatitude(),
            pickupLocation.getLongitude()
        );
        // Assume average speed of 40 km/h
        return (int) Math.ceil(distance / 40.0 * 60);
    }

    public List<Booking> getAssignedBookings(String driverId) {
        return bookingRepository.findByDriverIdAndStatus(driverId, BookingStatus.ASSIGNED);
    }
    
}