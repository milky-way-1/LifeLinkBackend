package com.example.LikeLink.Enum;

public enum BookingStatus {
    PENDING,    // Initial state when booking is created
    SEARCHING,  // Looking for drivers
    ASSIGNED,   // Driver has been assigned
    COMPLETED,  // Journey completed
    CANCELLED// Booking cancelled
}
