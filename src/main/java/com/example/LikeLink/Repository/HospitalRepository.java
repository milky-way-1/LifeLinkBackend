package com.example.LikeLink.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.LikeLink.Model.Hospital;

public interface HospitalRepository extends MongoRepository<Hospital, String> {
    boolean existsByUserId(String userId);
    Hospital findByUserId(String userId);
}