package com.airtraffic.controller;

import com.airtraffic.model.Aircraft;
import com.airtraffic.model.Conflict;
import com.airtraffic.service.AirTrafficService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AirTrafficController {

    @Autowired
    private AirTrafficService airTrafficService;

    @PostMapping("/aircraft")
    public Aircraft addAircraft(@RequestBody Map<String, Double> position) {
        double x = position.getOrDefault("x", 400.0);
        double y = position.getOrDefault("y", 300.0);
        return airTrafficService.addAircraft(x, y);
    }

    @GetMapping("/aircraft")
    public Collection<Aircraft> getAllAircrafts() {
        return airTrafficService.getAllAircrafts();
    }

    @GetMapping("/conflicts")
    public List<Conflict> getConflicts() {
        return airTrafficService.getActiveConflicts();
    }

    @PostMapping("/update")
    public void updatePositions() {
        airTrafficService.updatePositions();
    }

    @DeleteMapping("/aircraft/{id}")
    public void removeAircraft(@PathVariable String id) {
        airTrafficService.removeAircraft(id);
    }

    @DeleteMapping("/aircraft")
    public void clearAll() {
        airTrafficService.clearAll();
    }

    // Auto-update positions every 100ms
    @Scheduled(fixedRate = 100)
    public void scheduledUpdate() {
        airTrafficService.updatePositions();
        
        // Auto-resolve conflicts
        List<Conflict> conflicts = airTrafficService.getActiveConflicts();
        for (Conflict conflict : conflicts) {
            if (!conflict.isResolved()) {
                airTrafficService.resolveConflict(conflict);
            }
        }
    }
}
