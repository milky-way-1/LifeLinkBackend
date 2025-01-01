package com.example.LikeLink.dto.request;

import com.example.LikeLink.Model.Location;

import lombok.Data;

@Data
public class BookingRequest {
    private String userId;
    private LocationRequest pickupLocation;
    private LocationRequest destinationLocation;
}
