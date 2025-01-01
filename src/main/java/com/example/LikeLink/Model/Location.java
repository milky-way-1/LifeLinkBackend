package com.example.LikeLink.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private String type = "Point";
    private double[] coordinates;  // [longitude, latitude]

    public Location(double latitude, double longitude) {
        this.coordinates = new double[]{longitude, latitude};
    }

    public double getLatitude() {
        return coordinates[1];
    }

    public double getLongitude() {
        return coordinates[0];
    }
}