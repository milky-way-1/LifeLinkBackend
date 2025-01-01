package com.example.LikeLink.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.LikeLink.Enum.BookingStatus;
import com.example.LikeLink.Model.AmbulanceDriver;
import com.example.LikeLink.Model.Booking;
import com.example.LikeLink.Model.Location;
import com.example.LikeLink.Repository.BookingRepository;
import com.example.LikeLink.dto.request.BookingRequest;
import com.example.LikeLink.dto.response.BookingResponse;

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
    private static final double SEARCH_RADIUS_KM = 5.0;

    @Transactional
    public BookingResponse processBookingRequest(BookingRequest request) {
        try {
            // Create and save initial booking
            Booking booking = new Booking();
            booking.setUserId(request.getUserId());
            booking.setPickupLocation(request.getPickupLocation());
            booking.setDestinationLocation(request.getDestinationLocation());
            booking.setStatus(BookingStatus.SEARCHING);
            booking.setCreatedAt(LocalDateTime.now());
            booking.setUpdatedAt(LocalDateTime.now());
            
            Booking savedBooking = bookingRepository.save(booking);

            // Find nearby drivers
            List<AmbulanceDriver> nearbyDrivers = driverLocationService.findNearbyDrivers(
                request.getPickupLocation().getLatitude(),
                request.getPickupLocation().getLongitude(),
                SEARCH_RADIUS_KM
            );

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

            // Assign booking to nearest driver
            booking.setDriverId(nearestDriver.getId());
            booking.setStatus(BookingStatus.ASSIGNED);
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