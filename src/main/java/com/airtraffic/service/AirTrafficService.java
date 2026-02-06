package com.airtraffic.service;

import com.airtraffic.model.Aircraft;
import com.airtraffic.model.Conflict;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AirTrafficService {
    private final Map<String, Aircraft> aircrafts = new ConcurrentHashMap<>();
    private final List<Conflict> activeConflicts = Collections.synchronizedList(new ArrayList<>());
    // Minimum safe distance in pixels (represents approximately 5 nautical miles at scale)
    private static final double MIN_SEPARATION = 50.0;
    private static final double DELTA_TIME = 0.1; // Time step for updates

    public Aircraft addAircraft(double x, double y) {
        String id = UUID.randomUUID().toString();
        // Random velocity between -2 and 2
        double velocityX = (Math.random() - 0.5) * 4;
        double velocityY = (Math.random() - 0.5) * 4;
        
        Aircraft aircraft = new Aircraft(id, x, y, velocityX, velocityY);
        aircrafts.put(id, aircraft);
        return aircraft;
    }

    public Collection<Aircraft> getAllAircrafts() {
        return aircrafts.values();
    }

    public void updatePositions() {
        for (Aircraft aircraft : aircrafts.values()) {
            aircraft.updatePosition(DELTA_TIME);
        }
        detectConflicts();
    }

    public List<Conflict> detectConflicts() {
        activeConflicts.clear();
        List<Aircraft> aircraftList = new ArrayList<>(aircrafts.values());
        
        for (int i = 0; i < aircraftList.size(); i++) {
            for (int j = i + 1; j < aircraftList.size(); j++) {
                Aircraft a1 = aircraftList.get(i);
                Aircraft a2 = aircraftList.get(j);
                double distance = a1.distanceTo(a2);
                
                if (distance < MIN_SEPARATION) {
                    Conflict conflict = new Conflict(a1, a2, distance);
                    activeConflicts.add(conflict);
                }
            }
        }
        
        return activeConflicts;
    }

    public List<Conflict> getActiveConflicts() {
        return new ArrayList<>(activeConflicts);
    }

    public void resolveConflict(Conflict conflict) {
        Aircraft a1 = conflict.getAircraft1();
        Aircraft a2 = conflict.getAircraft2();
        
        // AI-based resolution: adjust velocities to avoid collision
        double dx = a2.getX() - a1.getX();
        double dy = a2.getY() - a1.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0) {
            // Normalize direction vector
            dx /= distance;
            dy /= distance;
            
            // Adjust a1 to move away from a2
            a1.setVelocityX(a1.getVelocityX() - dx * 0.5);
            a1.setVelocityY(a1.getVelocityY() - dy * 0.5);
            
            // Adjust a2 to move away from a1
            a2.setVelocityX(a2.getVelocityX() + dx * 0.5);
            a2.setVelocityY(a2.getVelocityY() + dy * 0.5);
            
            String resolution = String.format(
                "%s: Turn %s, %s: Turn %s - Conflict resolved",
                a1.getCallSign(), 
                getDirectionChange(a1),
                a2.getCallSign(),
                getDirectionChange(a2)
            );
            
            conflict.setResolution(resolution);
            conflict.setResolved(true);
        }
    }

    private String getDirectionChange(Aircraft aircraft) {
        double heading = aircraft.getHeading();
        if (heading >= -45 && heading < 45) return "North";
        if (heading >= 45 && heading < 135) return "East";
        if (heading >= -135 && heading < -45) return "West";
        return "South";
    }

    public void removeAircraft(String id) {
        aircrafts.remove(id);
    }

    public void clearAll() {
        aircrafts.clear();
        activeConflicts.clear();
    }
}
