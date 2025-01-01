package com.example.LikeLink.Model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    
    private String type = "Point";
    private List<Double> coordinates; // [longitude, latitude]

    public Location(Double latitude, Double longitude) {
        this.coordinates = Arrays.asList(longitude, latitude); // MongoDB expects [longitude, latitude]
    }

    // Getters and setters
    public Double getLatitude() {
        return coordinates != null && coordinates.size() > 1 ? coordinates.get(1) : null;
    }

    public Double getLongitude() {
        return coordinates != null && coordinates.size() > 0 ? coordinates.get(0) : null;
    }
}