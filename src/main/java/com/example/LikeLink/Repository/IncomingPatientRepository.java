package com.example.LikeLink.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.LikeLink.Model.IncomingPatient;

import java.util.List;

public interface IncomingPatientRepository extends MongoRepository<IncomingPatient, String> {
    List<IncomingPatient> findByHospitalIdAndStatus(String hospitalId, IncomingPatient.Status status);
}