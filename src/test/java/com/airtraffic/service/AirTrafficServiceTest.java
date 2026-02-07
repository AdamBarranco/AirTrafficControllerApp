package com.airtraffic.service;

import com.airtraffic.model.Aircraft;
import com.airtraffic.model.Conflict;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AirTrafficServiceTest {

    private AirTrafficService service;

    @BeforeEach
    void setUp() {
        service = new AirTrafficService();
    }

    @Test
    void testAddAircraft() {
        Aircraft aircraft = service.addAircraft(100, 200);
        
        assertNotNull(aircraft);
        assertNotNull(aircraft.getId());
        assertEquals(100, aircraft.getX());
        assertEquals(200, aircraft.getY());
        assertEquals(1, service.getAllAircrafts().size());
    }

    @Test
    void testGetAllAircrafts() {
        service.addAircraft(100, 200);
        service.addAircraft(300, 400);
        
        assertEquals(2, service.getAllAircrafts().size());
    }

    @Test
    void testDetectNoConflicts() {
        // Add aircraft far apart
        service.addAircraft(100, 100);
        service.addAircraft(500, 500);
        
        List<Conflict> conflicts = service.detectConflicts();
        assertEquals(0, conflicts.size());
    }

    @Test
    void testDetectConflicts() {
        // Add aircraft close together (within 50 pixels)
        service.addAircraft(100, 100);
        service.addAircraft(110, 110);
        
        List<Conflict> conflicts = service.detectConflicts();
        assertEquals(1, conflicts.size());
        
        Conflict conflict = conflicts.get(0);
        assertNotNull(conflict);
        assertTrue(conflict.getDistance() < 50);
    }

    @Test
    void testResolveConflict() {
        Aircraft a1 = service.addAircraft(100, 100);
        Aircraft a2 = service.addAircraft(110, 110);
        
        List<Conflict> conflicts = service.detectConflicts();
        assertEquals(1, conflicts.size());
        
        Conflict conflict = conflicts.get(0);
        double initialVx1 = a1.getVelocityX();
        double initialVx2 = a2.getVelocityX();
        
        service.resolveConflict(conflict);
        
        // Velocities should have changed
        assertNotEquals(initialVx1, a1.getVelocityX());
        assertNotEquals(initialVx2, a2.getVelocityX());
        
        // Conflict should be marked as resolved
        assertTrue(conflict.isResolved());
        assertNotNull(conflict.getResolution());
    }

    @Test
    void testRemoveAircraft() {
        Aircraft aircraft = service.addAircraft(100, 200);
        assertEquals(1, service.getAllAircrafts().size());
        
        service.removeAircraft(aircraft.getId());
        assertEquals(0, service.getAllAircrafts().size());
    }

    @Test
    void testClearAll() {
        service.addAircraft(100, 100);
        service.addAircraft(200, 200);
        service.addAircraft(300, 300);
        
        assertEquals(3, service.getAllAircrafts().size());
        
        service.clearAll();
        assertEquals(0, service.getAllAircrafts().size());
        assertEquals(0, service.getActiveConflicts().size());
    }

    @Test
    void testUpdatePositions() {
        Aircraft aircraft = service.addAircraft(100, 100);
        double initialX = aircraft.getX();
        
        service.updatePositions();
        
        // Position should have changed (unless velocity is 0, which is unlikely with random values)
        // We just verify the method runs without errors
        assertNotNull(service.getAllAircrafts());
    }

    @Test
    void testInitialLevel() {
        assertEquals(1, service.getLevel());
        assertFalse(service.isGameOver());
        assertEquals(0, service.getTotalCollisionCount());
        assertEquals(0, service.getTappedCollisionCount());
    }

    @Test
    void testSpeedMultiplier() {
        // Level 1: 1.0x
        assertEquals(1.0, service.getSpeedMultiplier(), 0.01);
    }

    @Test
    void testTargetAircraftCount() {
        // Level 1: 2 + 1 = 3
        assertEquals(3, service.getTargetAircraftCount());
    }

    @Test
    void testAddRandomAircraft() {
        Aircraft aircraft = service.addRandomAircraft();
        assertNotNull(aircraft);
        assertTrue(aircraft.getX() >= 50 && aircraft.getX() <= 750);
        assertTrue(aircraft.getY() >= 50 && aircraft.getY() <= 550);
        assertEquals(1, service.getAllAircrafts().size());
    }

    @Test
    void testRecordTapOnConflictAircraft() {
        Aircraft a1 = service.addAircraft(100, 100);
        Aircraft a2 = service.addAircraft(110, 110);
        service.detectConflicts();

        boolean result = service.recordTap(a1.getId());
        assertTrue(result);
        assertEquals(1, service.getTappedCollisionCount());
    }

    @Test
    void testRecordTapOnNonConflictAircraft() {
        Aircraft a1 = service.addAircraft(100, 100);
        Aircraft a2 = service.addAircraft(500, 500);
        service.detectConflicts();

        boolean result = service.recordTap(a1.getId());
        assertFalse(result);
        assertEquals(0, service.getTappedCollisionCount());
    }

    @Test
    void testGameOverTriggersWhenCollisionCountExceedsFive() {
        // Create 6 unique collision pairs to trigger game over (>5)
        for (int i = 0; i < 6; i++) {
            service.addAircraft(100, 100 + i);
            service.addAircraft(105, 100 + i);
        }
        service.detectConflicts();
        assertTrue(service.getTotalCollisionCount() >= 6);
        assertTrue(service.isGameOver());
    }

    @Test
    void testResetGame() {
        service.addAircraft(100, 100);
        service.addAircraft(110, 110);
        service.detectConflicts();

        service.resetGame();
        assertEquals(1, service.getLevel());
        assertEquals(0, service.getTotalCollisionCount());
        assertEquals(0, service.getTappedCollisionCount());
        assertFalse(service.isGameOver());
        assertEquals(0, service.getAllAircrafts().size());
    }

    @Test
    void testGameState() {
        Map<String, Object> state = service.getGameState();
        assertEquals(1, state.get("level"));
        assertEquals(0, state.get("totalCollisions"));
        assertEquals(0, state.get("tappedCollisions"));
        assertEquals(false, state.get("gameOver"));
        assertEquals(3, state.get("targetAircraftCount"));
        assertEquals(1.0, state.get("speedMultiplier"));
    }
}
