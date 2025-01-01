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
    
    @NotNull
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private List<Double> coordinates; // [longitude, latitude]

    private String type = "Point"; // GeoJSON type

    private LocalDateTime lastUpdated;

    public Location(Double latitude, Double longitude) {
        this.coordinates = Arrays.asList(longitude, latitude); // MongoDB expects [longitude, latitude]
        this.lastUpdated = LocalDateTime.now();
    }

    public Double getLatitude() {
        return coordinates != null && coordinates.size() > 1 ? coordinates.get(1) : null;
    }

    public Double getLongitude() {
        return coordinates != null && coordinates.size() > 0 ? coordinates.get(0) : null;
    }

    public void setLatitude(Double latitude) {
        if (coordinates == null) {
            coordinates = Arrays.asList(0.0, latitude);
        } else {
            coordinates.set(1, latitude);
        }
        this.lastUpdated = LocalDateTime.now();
    }

    public void setLongitude(Double longitude) {
        if (coordinates == null) {
            coordinates = Arrays.asList(longitude, 0.0);
        } else {
            coordinates.set(0, longitude);
        }
        this.lastUpdated = LocalDateTime.now();
    }
}