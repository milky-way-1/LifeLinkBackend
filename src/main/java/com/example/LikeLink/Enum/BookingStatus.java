package com.example.LikeLink.Enum;

public enum BookingStatus {
    PENDING,      // Initial request
    SEARCHING,    // Looking for drivers
    ACCEPTED,     // Driver accepted
    ARRIVED,      // Driver arrived at pickup
    IN_PROGRESS,  // Journey started
    COMPLETED,    // Journey completed
    CANCELLED    // Booking cancelled
}
