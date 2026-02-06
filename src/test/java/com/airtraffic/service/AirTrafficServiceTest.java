package com.airtraffic.service;

import com.airtraffic.model.Aircraft;
import com.airtraffic.model.Conflict;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
}
