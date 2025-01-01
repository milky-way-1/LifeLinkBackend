package com.example.LikeLink.Model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.LikeLink.Enum.BookingStatus;
import com.example.LikeLink.dto.request.LocationRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    private String id;
    
    private String userId;
    private String driverId;
    private LocationRequest pickupLocation;
    private LocationRequest destinationLocation;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    

}