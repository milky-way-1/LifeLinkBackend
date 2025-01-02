package com.example.LikeLink.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "incoming_patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncomingPatient {
    @Id
    private String id;  // This will be the hospital ID
    private String userId;
}
