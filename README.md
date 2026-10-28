# Jraffic 🚦

A JavaFX traffic simulation of a four-way intersection with vehicle routing, traffic lights, lane capacity, and collision prevention.

## Requirements

- Java 21+
- Maven 3.8+

## Run

```bash
./run.sh
```

Or manually:

```bash
mvn clean compile
mvn javafx:run
```

## Controls

| Key | Spawn |
| --- | --- |
| `↑` | From south |
| `↓` | From north |
| `→` | From west |
| `←` | From east |
| `R` | Random direction |
| `Esc` | Exit |

## Routes

Each vehicle receives a fixed route when spawned.

| Color | Route |
| --- | --- |
| 🔵 Blue | Straight |
| 🟡 Yellow | Left |
| 🟠 Orange | Right |

## Traffic

- 🚦 Adaptive traffic-light timing
- 🚗 Fixed vehicle speed
- 📏 Minimum safety gap
- 🛣️ Lane capacity limits
- 💥 Intersection collision prevention
- 🇰🇪 Keep-left traffic layout

## Lane Capacity

Capacity is calculated using:

```text
floor(lane length / (vehicle length + safety gap))
```

Current limits:

- **Vertical:** 4 vehicles
- **Horizontal:** 6 vehicles

## Tech Stack

- Java 21
- JavaFX
- Maven

## Project Structure

```text
src/main/java/com/jraffic/
├── Jraffic.java
├── Vehicle.java
├── Direction.java
├── Route.java
└── TrafficLight.java
```