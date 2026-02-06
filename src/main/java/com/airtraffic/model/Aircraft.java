package com.airtraffic.model;

public class Aircraft {
    private static final double CANVAS_WIDTH = 800.0;
    private static final double CANVAS_HEIGHT = 600.0;
    
    private String id;
    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private double heading;
    private String callSign;

    public Aircraft(String id, double x, double y, double velocityX, double velocityY) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.heading = Math.toDegrees(Math.atan2(velocityY, velocityX));
        this.callSign = "AC" + id.substring(0, Math.min(4, id.length()));
    }

    public void updatePosition(double deltaTime) {
        this.x += this.velocityX * deltaTime;
        this.y += this.velocityY * deltaTime;
        
        // Wrap around screen boundaries
        if (this.x < 0) this.x = CANVAS_WIDTH;
        if (this.x > CANVAS_WIDTH) this.x = 0;
        if (this.y < 0) this.y = CANVAS_HEIGHT;
        if (this.y > CANVAS_HEIGHT) this.y = 0;
    }

    public double distanceTo(Aircraft other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
        this.heading = Math.toDegrees(Math.atan2(this.velocityY, velocityX));
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
        this.heading = Math.toDegrees(Math.atan2(velocityY, this.velocityX));
    }

    public double getHeading() {
        return heading;
    }

    public String getCallSign() {
        return callSign;
    }

    public void setCallSign(String callSign) {
        this.callSign = callSign;
    }
}
