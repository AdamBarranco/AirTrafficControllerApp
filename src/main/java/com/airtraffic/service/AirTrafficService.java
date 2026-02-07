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
    private static final double WARNING_DISTANCE = 100.0;
    private static final double COLLISION_DISTANCE = 15.0;
    private static final double DELTA_TIME = 0.1; // Time step for updates
    private static final double CANVAS_WIDTH = 800.0;
    private static final double CANVAS_HEIGHT = 600.0;

    private int level = 1;
    private int lives = 3;
    private int totalCollisionCount = 0;
    private int tappedCollisionCount = 0;
    private boolean gameOver = false;
    private final Set<String> tappedAircraftIds = Collections.synchronizedSet(new HashSet<>());
    private final List<double[]> recentExplosions = Collections.synchronizedList(new ArrayList<>());

    public Aircraft addAircraft(double x, double y) {
        String id = UUID.randomUUID().toString();
        double speedMultiplier = getSpeedMultiplier();
        // Random velocity between -2 and 2, scaled by level
        double velocityX = (Math.random() - 0.5) * 4 * speedMultiplier;
        double velocityY = (Math.random() - 0.5) * 4 * speedMultiplier;
        
        Aircraft aircraft = new Aircraft(id, x, y, velocityX, velocityY);
        aircrafts.put(id, aircraft);
        return aircraft;
    }

    public Aircraft addRandomAircraft() {
        double margin = 50.0;
        double x = margin + Math.random() * (CANVAS_WIDTH - 2 * margin);
        double y = margin + Math.random() * (CANVAS_HEIGHT - 2 * margin);
        return addAircraft(x, y);
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
        List<String[]> collisionPairs = new ArrayList<>();
        
        for (int i = 0; i < aircraftList.size(); i++) {
            for (int j = i + 1; j < aircraftList.size(); j++) {
                Aircraft a1 = aircraftList.get(i);
                Aircraft a2 = aircraftList.get(j);
                double distance = a1.distanceTo(a2);
                
                if (distance < COLLISION_DISTANCE) {
                    // Actual collision - planes blow up
                    double midX = (a1.getX() + a2.getX()) / 2;
                    double midY = (a1.getY() + a2.getY()) / 2;
                    recentExplosions.add(new double[]{midX, midY});
                    collisionPairs.add(new String[]{a1.getId(), a2.getId()});
                    totalCollisionCount++;
                    lives--;
                    if (lives <= 0) {
                        gameOver = true;
                    }
                } else if (distance < MIN_SEPARATION) {
                    Conflict conflict = new Conflict(a1, a2, distance, "danger");
                    activeConflicts.add(conflict);
                } else if (distance < WARNING_DISTANCE) {
                    Conflict conflict = new Conflict(a1, a2, distance, "warning");
                    activeConflicts.add(conflict);
                }
            }
        }
        
        // Remove collided aircraft after iteration
        for (String[] pair : collisionPairs) {
            aircrafts.remove(pair[0]);
            aircrafts.remove(pair[1]);
        }
        
        return activeConflicts;
    }

    public List<Conflict> getActiveConflicts() {
        return new ArrayList<>(activeConflicts);
    }

    public boolean recordTap(String aircraftId) {
        if (tappedAircraftIds.contains(aircraftId)) {
            return false;
        }
        Conflict matchedConflict = activeConflicts.stream().filter(c ->
                !c.isResolved() && (c.getAircraft1().getId().equals(aircraftId)
                        || c.getAircraft2().getId().equals(aircraftId)))
                .findFirst().orElse(null);
        if (matchedConflict != null) {
            tappedAircraftIds.add(aircraftId);
            tappedCollisionCount++;

            // Turn the tapped aircraft away from the other
            Aircraft tapped = matchedConflict.getAircraft1().getId().equals(aircraftId)
                    ? matchedConflict.getAircraft1() : matchedConflict.getAircraft2();
            Aircraft other = matchedConflict.getAircraft1().getId().equals(aircraftId)
                    ? matchedConflict.getAircraft2() : matchedConflict.getAircraft1();
            turnAway(tapped, other);

            if (tappedCollisionCount > 0 && tappedCollisionCount % 10 == 0) {
                level++;
            }
            return true;
        }
        return false;
    }

    private void turnAway(Aircraft tapped, Aircraft other) {
        double dx = other.getX() - tapped.getX();
        double dy = other.getY() - tapped.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance > 0) {
            dx /= distance;
            dy /= distance;
            // Turn tapped aircraft away from the other
            double speed = Math.sqrt(tapped.getVelocityX() * tapped.getVelocityX()
                    + tapped.getVelocityY() * tapped.getVelocityY());
            if (speed < 0.5) speed = 1.0;
            tapped.setVelocityX(-dx * speed);
            tapped.setVelocityY(-dy * speed);
        }
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

    public double getSpeedMultiplier() {
        return 1.0 + (level - 1) * 0.3;
    }

    public int getTargetAircraftCount() {
        return 2 + level;
    }

    public int getLevel() {
        return level;
    }

    public int getLives() {
        return lives;
    }

    public int getTotalCollisionCount() {
        return totalCollisionCount;
    }

    public int getTappedCollisionCount() {
        return tappedCollisionCount;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Map<String, Object> getGameState() {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("level", level);
        state.put("lives", lives);
        state.put("totalCollisions", totalCollisionCount);
        state.put("tappedCollisions", tappedCollisionCount);
        state.put("gameOver", gameOver);
        state.put("targetAircraftCount", getTargetAircraftCount());
        state.put("speedMultiplier", getSpeedMultiplier());
        // Include recent explosions and clear them
        List<double[]> explosions;
        synchronized (recentExplosions) {
            explosions = new ArrayList<>(recentExplosions);
            recentExplosions.clear();
        }
        List<Map<String, Double>> explosionList = new ArrayList<>();
        for (double[] pos : explosions) {
            Map<String, Double> exp = new LinkedHashMap<>();
            exp.put("x", pos[0]);
            exp.put("y", pos[1]);
            explosionList.add(exp);
        }
        state.put("explosions", explosionList);
        return state;
    }

    public void resetGame() {
        aircrafts.clear();
        activeConflicts.clear();
        tappedAircraftIds.clear();
        recentExplosions.clear();
        level = 1;
        lives = 3;
        totalCollisionCount = 0;
        tappedCollisionCount = 0;
        gameOver = false;
    }

    public void removeAircraft(String id) {
        aircrafts.remove(id);
    }

    public void clearAll() {
        aircrafts.clear();
        activeConflicts.clear();
    }
}
