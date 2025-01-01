package com.example.LikeLink.Model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "incoming_patients")
public class IncomingPatient {
    @Id
    private String id;
    private String hospitalId;
    private String patientName;
    private String emergencyType;
    private String condition;
    private String estimatedArrivalTime;
    private Status status;
    private Location currentLocation;
    private String ambulanceId;
    private String createdAt;
    private String updatedAt;

    public enum Status {
        PENDING,
        ARRIVED,
        CANCELLED
    }
}
