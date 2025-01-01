package com.example.LikeLink.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import com.example.LikeLink.Enum.BookingStatus;
import com.example.LikeLink.Model.AmbulanceDriver;
import com.example.LikeLink.Model.Booking;
import com.example.LikeLink.Model.Location;
import com.example.LikeLink.Repository.BookingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final DriverLocationService driverLocationService;
    private final WebSocketService webSocketService;
    
    private static final double SEARCH_RADIUS_KM = 5.0;
    private static final long DRIVER_RESPONSE_TIMEOUT_MS = 30000; // 30 seconds
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public void processBookingRequest(Booking booking) {
        try {
            // Save initial booking
            booking.setCreatedAt(LocalDateTime.now());
            booking.setUpdatedAt(LocalDateTime.now());
            Booking savedBooking = bookingRepository.save(booking);

            // Process booking asynchronously
            executorService.submit(() -> findDriver(savedBooking));

        } catch (Exception e) {
            log.error("Error processing booking request", e);
            throw new RuntimeException("Failed to process booking request", e);
        }
    }

    private void findDriver(Booking booking) {
        try {
            booking.setStatus(BookingStatus.SEARCHING);
            bookingRepository.save(booking);

            // Find nearby drivers
            List<AmbulanceDriver> nearbyDrivers = driverLocationService.findNearbyDrivers(
                booking.getPickupLocation().getLatitude(),
                booking.getPickupLocation().getLongitude(),
                SEARCH_RADIUS_KM
            );

            if (nearbyDrivers.isEmpty()) {
                handleNoDriversAvailable(booking);
                return;
            }

            // Try each driver until one accepts
            for (AmbulanceDriver driver : nearbyDrivers) {
                if (tryDriver(booking, driver)) {
                    return; // Driver found and accepted
                }
            }

            // No drivers accepted
            handleNoDriversAvailable(booking);

        } catch (Exception e) {
            log.error("Error finding driver for booking {}", booking.getId(), e);
            handleBookingError(booking);
        }
    }

    private boolean tryDriver(Booking booking, AmbulanceDriver driver) {
        try {
            // Send booking request to driver
            sendBookingRequest(booking, driver);

            // Wait for driver response
            synchronized (booking) {
                booking.wait(DRIVER_RESPONSE_TIMEOUT_MS);
            }

            // Check if booking was accepted by this driver
            Booking updatedBooking = bookingRepository.findById(booking.getId()).orElse(null);
            return updatedBooking != null && 
                   updatedBooking.getStatus() == BookingStatus.ACCEPTED &&
                   driver.getId().equals(updatedBooking.getDriverId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.error("Error trying driver {} for booking {}", driver.getId(), booking.getId(), e);
            return false;
        }
    }

    private void sendBookingRequest(Booking booking, AmbulanceDriver driver) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("type", "booking_request");
        requestData.put("bookingId", booking.getId());
        requestData.put("pickupLocation", booking.getPickupLocation());
        requestData.put("destinationLocation", booking.getDestinationLocation());
        
        webSocketService.sendMessageToDriver(driver.getId(), requestData);
    }

    public void handleDriverResponse(String bookingId, String driverId, boolean accepted) {
        try {
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();

                if (booking.getStatus() == BookingStatus.SEARCHING) {
                    if (accepted) {
                        acceptBooking(booking, driverId);
                    }
                    
                    // Notify waiting thread
                    synchronized (booking) {
                        booking.notify();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error handling driver response", e);
        }
    }

    private void acceptBooking(Booking booking, String driverId) {
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setDriverId(driverId);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // Notify user
        Map<String, Object> acceptanceData = new HashMap<>();
        acceptanceData.put("type", "booking_accepted");
        acceptanceData.put("bookingId", booking.getId());
        acceptanceData.put("driverId", driverId);
        
        webSocketService.sendMessageToUser(booking.getUserId(), acceptanceData);
    }

    public void updateBookingStatus(String bookingId, BookingStatus newStatus) {
        try {
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();
                
                // Validate status transition
                if (isValidStatusTransition(booking.getStatus(), newStatus)) {
                    booking.setStatus(newStatus);
                    booking.setUpdatedAt(LocalDateTime.now());
                    bookingRepository.save(booking);

                    // Notify user about status change
                    notifyStatusChange(booking);
                } else {
                    throw new IllegalStateException("Invalid status transition from " + 
                        booking.getStatus() + " to " + newStatus);
                }
            }
        } catch (Exception e) {
            log.error("Error updating booking status", e);
            throw new RuntimeException("Failed to update booking status", e);
        }
    }

    private boolean isValidStatusTransition(BookingStatus currentStatus, BookingStatus newStatus) {
        switch (currentStatus) {
            case ACCEPTED:
                return newStatus == BookingStatus.ARRIVED || newStatus == BookingStatus.CANCELLED;
            case ARRIVED:
                return newStatus == BookingStatus.IN_PROGRESS || newStatus == BookingStatus.CANCELLED;
            case IN_PROGRESS:
                return newStatus == BookingStatus.COMPLETED || newStatus == BookingStatus.CANCELLED;
            default:
                return false;
        }
    }

    private void notifyStatusChange(Booking booking) {
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("type", "booking_status_update");
        statusUpdate.put("bookingId", booking.getId());
        statusUpdate.put("status", booking.getStatus());
        
        webSocketService.sendMessageToUser(booking.getUserId(), statusUpdate);
        if (booking.getDriverId() != null) {
            webSocketService.sendMessageToDriver(booking.getDriverId(), statusUpdate);
        }
    }

    private void handleNoDriversAvailable(Booking booking) {
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        Map<String, Object> noDriverData = new HashMap<>();
        noDriverData.put("type", "no_drivers_available");
        noDriverData.put("bookingId", booking.getId());
        
        webSocketService.sendMessageToUser(booking.getUserId(), noDriverData);
    }

    private void handleBookingError(Booking booking) {
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        Map<String, Object> errorData = new HashMap<>();
        errorData.put("type", "booking_error");
        errorData.put("bookingId", booking.getId());
        errorData.put("message", "An error occurred processing your booking");
        
        webSocketService.sendMessageToUser(booking.getUserId(), errorData);
    }

    // Cleanup method to be called when application shuts down
    public void shutdown() {
        executorService.shutdown();
    }
}