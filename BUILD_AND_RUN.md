# Air Traffic Controller Application

This Air Traffic Controller application uses Java Spring Boot for the backend and a web-based frontend.

## Features

- ✈️ Interactive radar display with aircraft visualization
- 🎯 Click anywhere on the radar to add aircraft
- 🔍 Real-time collision detection
- 🤖 AI-based conflict resolution that automatically adjusts aircraft velocities
- 📊 Live statistics and conflict notifications
- 🎨 Modern, responsive web UI

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Building the Application

```bash
mvn clean package
```

## Running Tests

```bash
mvn test
```

## Running the Application

```bash
mvn spring-boot:run
```

The application will start on http://localhost:8080

## Using the Application

1. Open your web browser and navigate to http://localhost:8080
2. Click anywhere on the radar to add aircraft
3. Aircraft will automatically fly across the screen
4. When aircraft get too close (within 50 pixels), a conflict is detected
5. The AI system automatically resolves conflicts by adjusting aircraft velocities
6. Notifications appear showing conflict detection and resolution
7. Use the "Clear All Aircraft" button to remove all aircraft

## How It Works

### Backend (Java Spring Boot)

- **Aircraft Model**: Represents an aircraft with position, velocity, and heading
- **AirTrafficService**: Manages aircraft, detects conflicts, and resolves them using AI logic
- **AirTrafficController**: REST API endpoints for the frontend
- **Conflict Detection**: Checks distances between all aircraft pairs every 100ms
- **AI Resolution**: Adjusts aircraft velocities to move them apart when conflicts occur

### Frontend (HTML/CSS/JavaScript)

- **Canvas Rendering**: Displays aircraft as triangular icons with velocity vectors
- **Real-time Updates**: Polls the backend every 100ms for updates
- **Notifications**: Shows alerts for conflict detection and resolution
- **Interactive Controls**: Click to add aircraft, button to clear all

### API Endpoints

- `POST /api/aircraft` - Add a new aircraft at specified coordinates
- `GET /api/aircraft` - Get all aircraft
- `GET /api/conflicts` - Get all active conflicts
- `DELETE /api/aircraft/{id}` - Remove a specific aircraft
- `DELETE /api/aircraft` - Clear all aircraft

## Testing

The application includes comprehensive unit tests for:

- Aircraft position updates and wrapping
- Distance calculations between aircraft
- Conflict detection algorithm
- Conflict resolution logic
- Service layer functionality

Run tests with: `mvn test`

## Architecture

```
src/
├── main/
│   ├── java/com/airtraffic/
│   │   ├── AirTrafficControllerApplication.java  # Main Spring Boot app
│   │   ├── controller/
│   │   │   └── AirTrafficController.java         # REST API
│   │   ├── model/
│   │   │   ├── Aircraft.java                     # Aircraft model
│   │   │   └── Conflict.java                     # Conflict model
│   │   └── service/
│   │       └── AirTrafficService.java            # Business logic
│   └── resources/
│       ├── static/
│       │   ├── index.html                        # Web UI
│       │   ├── styles.css                        # Styling
│       │   └── app.js                            # Frontend logic
│       └── application.properties                # Configuration
└── test/
    └── java/com/airtraffic/
        ├── model/
        │   └── AircraftTest.java                 # Aircraft tests
        └── service/
            └── AirTrafficServiceTest.java        # Service tests
```

## Future Enhancements

- Persist aircraft data to a database
- Add altitude dimension for 3D conflict detection
- Implement flight paths and waypoints
- Add more sophisticated AI algorithms for conflict resolution
- Support for different aircraft types with varying speeds
- Historical conflict data and analytics
