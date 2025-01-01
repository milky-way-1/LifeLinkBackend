package com.example.LikeLink.Service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.LikeLink.Enum.BookingStatus;
import com.example.LikeLink.Model.Booking;
import com.example.LikeLink.Repository.BookingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingManagementService {

    private final BookingRepository bookingRepository;

    public boolean canUserCreateBooking(String userId) {
        List<BookingStatus> activeStatuses = Arrays.asList(
            BookingStatus.PENDING,
            BookingStatus.SEARCHING,
            BookingStatus.ACCEPTED,
            BookingStatus.ARRIVED,
            BookingStatus.IN_PROGRESS
        );
        
        return !bookingRepository.existsByUserIdAndStatusIn(userId, activeStatuses);
    }

    public boolean canDriverAcceptBooking(String driverId) {
        List<BookingStatus> activeStatuses = Arrays.asList(
            BookingStatus.ACCEPTED,
            BookingStatus.ARRIVED,
            BookingStatus.IN_PROGRESS
        );
        
        return !bookingRepository.existsByDriverIdAndStatusIn(driverId, activeStatuses);
    }

    public List<Booking> getUserBookingHistory(String userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Booking> getDriverBookingHistory(String driverId) {
        return bookingRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
    }

    public Optional<Booking> getUserActiveBooking(String userId) {
        return bookingRepository.findActiveBookingByUserId(userId);
    }

    public Optional<Booking> getDriverActiveBooking(String driverId) {
        return bookingRepository.findActiveBookingByDriverId(driverId);
    }

    @Scheduled(cron = "0 0 0 * * *") // Run daily at midnight
    public void cleanupOldBookings() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<BookingStatus> completedStatuses = Arrays.asList(
            BookingStatus.COMPLETED,
            BookingStatus.CANCELLED
        );
        
        bookingRepository.deleteByStatusInAndCreatedAtBefore(
            completedStatuses, 
            thirtyDaysAgo
        );
        log.info("Cleaned up old bookings before {}", thirtyDaysAgo);
    }

    public Map<String, Long> getDriverStats(String driverId) {
        Map<String, Long> stats = new HashMap<>();
        
        stats.put("completed", 
            bookingRepository.countByDriverIdAndStatus(driverId, BookingStatus.COMPLETED));
        stats.put("cancelled", 
            bookingRepository.countByDriverIdAndStatus(driverId, BookingStatus.CANCELLED));
        
        return stats;
    }

    public List<Booking> getRecentBookings(BookingStatus status, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return bookingRepository.findRecentBookingsByStatus(status, since);
    }
}