package com.example.LikeLink.Config;


import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.example.LikeLink.Model.AmbulanceDriver;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class MongoConfig {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void initIndexes() {
        try {
            // Drop existing index
            mongoTemplate.indexOps("ambulance_drivers").dropIndex("currentLocation_2dsphere");
            
            // Create new 2dsphere index
            GeospatialIndex geospatialIndex = new GeospatialIndex("currentLocation");
            mongoTemplate.indexOps("ambulance_drivers").ensureIndex(geospatialIndex);
            
            log.info("Successfully created 2dsphere index on currentLocation");
        } catch (Exception e) {
            log.error("Error creating MongoDB indexes: " + e.getMessage(), e);
        }
    }
}