package com.example.LikeLink.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.LikeLink.Enum.BookingStatus;
import com.example.LikeLink.Model.Booking;


@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    // Find active booking for a user
    @Query("{ 'userId': ?0, 'status': { $in: ['PENDING', 'SEARCHING', 'ACCEPTED', 'ARRIVED', 'IN_PROGRESS'] }}")
    Optional<Booking> findActiveBookingByUserId(String userId);

    // Find active booking for a driver
    @Query("{ 'driverId': ?0, 'status': { $in: ['ACCEPTED', 'ARRIVED', 'IN_PROGRESS'] }}")
    Optional<Booking> findActiveBookingByDriverId(String driverId);

    // Find all bookings for a user
    List<Booking> findByUserIdOrderByCreatedAtDesc(String userId);

    // Find all bookings for a driver
    List<Booking> findByDriverIdOrderByCreatedAtDesc(String driverId);

    // Find bookings by status
    List<Booking> findByStatus(BookingStatus status);

    // Find bookings created between dates
    List<Booking> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Find completed bookings for a driver
    @Query("{ 'driverId': ?0, 'status': 'COMPLETED' }")
    List<Booking> findCompletedBookingsByDriver(String driverId);

    // Find cancelled bookings
    List<Booking> findByStatusAndCreatedAtBetween(
        BookingStatus status, 
        LocalDateTime start, 
        LocalDateTime end
    );

    // Check if user has any active booking
    boolean existsByUserIdAndStatusIn(
        String userId, 
        List<BookingStatus> statuses
    );

    // Check if driver has any active booking
    boolean existsByDriverIdAndStatusIn(
        String driverId, 
        List<BookingStatus> statuses
    );

    // Find recent bookings with specific status
    @Query("{ 'status': ?0, 'createdAt': { $gte: ?1 } }")
    List<Booking> findRecentBookingsByStatus(
        BookingStatus status, 
        LocalDateTime since
    );

    // Count bookings by status for a specific driver
    long countByDriverIdAndStatus(String driverId, BookingStatus status);

    // Delete old completed/cancelled bookings
    void deleteByStatusInAndCreatedAtBefore(
        List<BookingStatus> statuses, 
        LocalDateTime before
    );
}
