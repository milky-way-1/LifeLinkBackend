package com.example.LikeLink.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.example.LikeLink.Config.DriverLocationWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketService {
    
    private final DriverLocationWebSocketHandler driverLocationHandler;
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Register user session
    public void registerUserSession(String userId, WebSocketSession session) {
        userSessions.put(userId, session);
        log.info("User session registered: {}", userId);
    }

    // Remove user session
    public void removeUserSession(String userId) {
        userSessions.remove(userId);
        log.info("User session removed: {}", userId);
    }

    // Send message to user
    public void sendMessageToUser(String userId, Object message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
                log.debug("Message sent to user {}: {}", userId, jsonMessage);
            } catch (IOException e) {
                log.error("Error sending message to user: " + userId, e);
                removeUserSession(userId);
            }
        } else {
            log.warn("No active session found for user: {}", userId);
            removeUserSession(userId);
        }
    }

    // Send message to driver using existing handler
    public void sendMessageToDriver(String driverId, Object message) {
        WebSocketSession driverSession = driverLocationHandler.getDriverSession(driverId);
        if (driverSession != null && driverSession.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                driverSession.sendMessage(new TextMessage(jsonMessage));
                log.debug("Message sent to driver {}: {}", driverId, jsonMessage);
            } catch (IOException e) {
                log.error("Error sending message to driver: " + driverId, e);
            }
        } else {
            log.warn("No active session found for driver: {}", driverId);
        }
    }

    // Subscribe user to driver location updates
    public void subscribeToDriverLocation(String bookingId, String userId) {
        WebSocketSession userSession = userSessions.get(userId);
        if (userSession != null && userSession.isOpen()) {
            driverLocationHandler.subscribeToDriverLocation(bookingId, userSession);
            log.info("User {} subscribed to booking {} location updates", userId, bookingId);
        }
    }

    // Unsubscribe user from driver location updates
    public void unsubscribeFromDriverLocation(String bookingId, String userId) {
        WebSocketSession userSession = userSessions.get(userId);
        if (userSession != null) {
            driverLocationHandler.unsubscribeFromDriverLocation(bookingId, userSession);
            log.info("User {} unsubscribed from booking {} location updates", userId, bookingId);
        }
    }

    // Check if user is connected
    public boolean isUserConnected(String userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    // Get active user count
    public int getActiveUserCount() {
        return (int) userSessions.values().stream()
            .filter(WebSocketSession::isOpen)
            .count();
    }
}