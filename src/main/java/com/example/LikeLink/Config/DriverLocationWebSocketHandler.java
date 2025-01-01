package com.example.LikeLink.Config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.LikeLink.Model.AmbulanceDriver;
import com.example.LikeLink.Repository.AmbulanceDriverRepository;
import com.example.LikeLink.dto.request.LocationUpdateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DriverLocationWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private AmbulanceDriverRepository driverRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Store active WebSocket sessions
    private final Map<String, WebSocketSession> driverSessions = new ConcurrentHashMap<>();
    private final Map<String, Set<WebSocketSession>> bookingSubscribers = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String driverId = extractDriverId(session);
        if (driverId != null) {
            driverSessions.put(driverId, session);
            log.info("Driver {} connected", driverId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            LocationUpdateDto locationUpdate = objectMapper.readValue(payload, LocationUpdateDto.class);
            
            // Update driver location in database
            updateDriverLocation(locationUpdate);

            // Broadcast location to subscribers if this is for a booking
            if (locationUpdate.getBookingId() != null) {
                broadcastLocationUpdate(locationUpdate);
            }

        } catch (Exception e) {
            log.error("Error handling location update", e);
            sendErrorMessage(session, "Error processing location update");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String driverId = extractDriverId(session);
        if (driverId != null) {
            driverSessions.remove(driverId);
            log.info("Driver {} disconnected", driverId);
        }
    }

    private void updateDriverLocation(LocationUpdateDto locationUpdate) {
        try {
            Optional<AmbulanceDriver> driverOpt = driverRepository.findById(locationUpdate.getDriverId());
            
            if (driverOpt.isPresent()) {
                AmbulanceDriver driver = driverOpt.get();
                driver.updateLocation(locationUpdate.getLatitude(), locationUpdate.getLongitude());
                driverRepository.save(driver);
                log.debug("Updated location for driver {}", locationUpdate.getDriverId());
            } else {
                log.warn("Driver not found: {}", locationUpdate.getDriverId());
            }
        } catch (Exception e) {
            log.error("Error updating driver location", e);
        }
    }

    private void broadcastLocationUpdate(LocationUpdateDto locationUpdate) {
        String bookingId = locationUpdate.getBookingId();
        Set<WebSocketSession> subscribers = bookingSubscribers.get(bookingId);
        
        if (subscribers != null) {
            String message = null;
            try {
                message = objectMapper.writeValueAsString(locationUpdate);
            } catch (JsonProcessingException e) {
                log.error("Error serializing location update", e);
                return;
            }

            subscribers.removeIf(session -> !session.isOpen());
            
            for (WebSocketSession subscriber : subscribers) {
                try {
                    subscriber.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.error("Error sending location update to subscriber", e);
                }
            }
        }
    }

    public void subscribeToDriverLocation(String bookingId, WebSocketSession session) {
        bookingSubscribers.computeIfAbsent(bookingId, k -> ConcurrentHashMap.newKeySet())
                         .add(session);
    }

    public void unsubscribeFromDriverLocation(String bookingId, WebSocketSession session) {
        Set<WebSocketSession> subscribers = bookingSubscribers.get(bookingId);
        if (subscribers != null) {
            subscribers.remove(session);
            if (subscribers.isEmpty()) {
                bookingSubscribers.remove(bookingId);
            }
        }
    }

    private String extractDriverId(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && "driverId".equals(keyValue[0])) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }

    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            Map<String, String> error = new HashMap<>();
            error.put("type", "error");
            error.put("message", errorMessage);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        } catch (IOException e) {
            log.error("Error sending error message", e);
        }
    }
    
    public WebSocketSession getDriverSession(String driverId) {
        return driverSessions.get(driverId);
    }
}