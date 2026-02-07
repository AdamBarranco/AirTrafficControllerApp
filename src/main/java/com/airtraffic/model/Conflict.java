package com.airtraffic.model;

public class Conflict {
    private Aircraft aircraft1;
    private Aircraft aircraft2;
    private double distance;
    private String resolution;
    private boolean resolved;
    private String severity; // "warning" or "danger"

    public Conflict(Aircraft aircraft1, Aircraft aircraft2, double distance) {
        this.aircraft1 = aircraft1;
        this.aircraft2 = aircraft2;
        this.distance = distance;
        this.resolved = false;
        this.severity = "danger";
    }

    public Conflict(Aircraft aircraft1, Aircraft aircraft2, double distance, String severity) {
        this.aircraft1 = aircraft1;
        this.aircraft2 = aircraft2;
        this.distance = distance;
        this.resolved = false;
        this.severity = severity;
    }

    public Aircraft getAircraft1() {
        return aircraft1;
    }

    public Aircraft getAircraft2() {
        return aircraft2;
    }

    public double getDistance() {
        return distance;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    @Override
    public String toString() {
        return String.format("Conflict between %s and %s at distance %.2f", 
            aircraft1.getCallSign(), aircraft2.getCallSign(), distance);
    }
}
