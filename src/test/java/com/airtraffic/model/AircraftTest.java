package com.airtraffic.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AircraftTest {

    @Test
    void testAircraftCreation() {
        Aircraft aircraft = new Aircraft("test-id", 100, 200, 1.5, 2.0);
        
        assertEquals("test-id", aircraft.getId());
        assertEquals(100, aircraft.getX());
        assertEquals(200, aircraft.getY());
        assertEquals(1.5, aircraft.getVelocityX());
        assertEquals(2.0, aircraft.getVelocityY());
        assertTrue(aircraft.getCallSign().startsWith("AC"));
    }

    @Test
    void testUpdatePosition() {
        Aircraft aircraft = new Aircraft("test-id", 100, 200, 2.0, 3.0);
        aircraft.updatePosition(1.0);
        
        assertEquals(102, aircraft.getX(), 0.01);
        assertEquals(203, aircraft.getY(), 0.01);
    }

    @Test
    void testPositionWrapping() {
        // Test X wrapping
        Aircraft aircraft1 = new Aircraft("test-1", -10, 300, -1.0, 0);
        aircraft1.updatePosition(1.0);
        assertEquals(800, aircraft1.getX(), 0.01);
        
        Aircraft aircraft2 = new Aircraft("test-2", 810, 300, 1.0, 0);
        aircraft2.updatePosition(1.0);
        assertEquals(0, aircraft2.getX(), 0.01);
    }

    @Test
    void testDistanceTo() {
        Aircraft aircraft1 = new Aircraft("test-1", 0, 0, 0, 0);
        Aircraft aircraft2 = new Aircraft("test-2", 3, 4, 0, 0);
        
        assertEquals(5.0, aircraft1.distanceTo(aircraft2), 0.01);
    }

    @Test
    void testVelocityUpdate() {
        Aircraft aircraft = new Aircraft("test-id", 100, 200, 1.0, 0);
        aircraft.setVelocityX(0);
        aircraft.setVelocityY(1.0);
        
        // Heading should be updated when velocity changes
        assertNotEquals(0, aircraft.getHeading());
    }
}
