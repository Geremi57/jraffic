package com.jraffic;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Jraffic extends Application {

    private static final double WIDTH = 1000;
    private static final double HEIGHT = 700;
    private static final double VEHICLE_LENGTH = 24;
    private static final double SAFETY_GAP = 30;
    private static final double VERTICAL_LANE_LENGTH = 216;
    private static final double HORIZONTAL_LANE_LENGTH = 366;
    private static final int VERTICAL_LANE_CAPACITY =
            (int) Math.floor(
                    VERTICAL_LANE_LENGTH / (VEHICLE_LENGTH + SAFETY_GAP));
    private static final int HORIZONTAL_LANE_CAPACITY =
            (int) Math.floor(
                    HORIZONTAL_LANE_LENGTH / (VEHICLE_LENGTH + SAFETY_GAP));
    private static final long MIN_LIGHT_PHASE_DURATION = 4_000_000_000L;
    private static final long MAX_LIGHT_PHASE_DURATION = 12_000_000_000L;
    private static final long EXTRA_TIME_PER_VEHICLE = 1_000_000_000L;

    private final List<Vehicle> vehicles = new ArrayList<>();
    private final Random random = new Random();

    private Pane root;

    private TrafficLight northLight;
    private TrafficLight southLight;
    private TrafficLight eastLight;
    private TrafficLight westLight;

    @Override
    public void start(Stage stage) {
        root = new Pane();

        drawBackground();
        drawRoads();
        drawStopLines();
        drawTrafficLights();

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                stage.close();
                return;
            }

            Direction direction = directionFromKey(event.getCode());

            if (direction != null) {
                spawnVehicle(direction);
            }
        });

        stage.setTitle("Jraffic");
        stage.setScene(scene);
        stage.show();

        root.setFocusTraversable(true);
        root.requestFocus();

        AnimationTimer timer = new AnimationTimer() {
            private long phaseStart;
            private long phaseDuration;
            private boolean verticalGreen = true;

            @Override
            public void handle(long now) {
                if (phaseStart == 0) {
                    phaseStart = now;
                    phaseDuration = calculateGreenDuration(verticalGreen);
                } else if (now - phaseStart >= phaseDuration) {
                    verticalGreen = !verticalGreen;
                    phaseStart = now;
                    phaseDuration = calculateGreenDuration(verticalGreen);
                }

                updateTrafficLights(verticalGreen);
                updateVehicles();
            }
        };

        timer.start();
    }

    private Direction directionFromKey(KeyCode key) {
        return switch (key) {
            case UP -> Direction.SOUTH;
            case DOWN -> Direction.NORTH;
            case LEFT -> Direction.EAST;
            case RIGHT -> Direction.WEST;
            case R -> randomDirection();
            default -> null;
        };
    }

    private Direction randomDirection() {
        Direction[] directions = Direction.values();
        return directions[random.nextInt(directions.length)];
    }

    private void spawnVehicle(Direction direction) {
        Route route = randomRoute();
        Vehicle vehicle = new Vehicle(direction, route);

        if (!canSpawn(vehicle)) {
            return;
        }

        vehicles.add(vehicle);
        root.getChildren().add(vehicle.getBody());
    }

    private Route randomRoute() {
        Route[] routes = Route.values();
        return routes[random.nextInt(routes.length)];
    }

    private boolean canSpawn(Vehicle newVehicle) {
        long laneVehicleCount = vehicles.stream()
                .filter(vehicle ->
                        vehicle.getDirection() == newVehicle.getDirection())
                .count();

        int laneCapacity = switch (newVehicle.getDirection()) {
            case NORTH, SOUTH -> VERTICAL_LANE_CAPACITY;
            case EAST, WEST -> HORIZONTAL_LANE_CAPACITY;
        };

        if (laneVehicleCount >= laneCapacity) {
            return false;
        }

        for (Vehicle vehicle : vehicles) {
            if (vehicle.getDirection() != newVehicle.getDirection()) {
                continue;
            }

            if (distanceFromSpawn(vehicle) < 50) {
                return false;
            }
        }

        return true;
    }

    private double distanceFromSpawn(Vehicle vehicle) {
        return switch (vehicle.getDirection()) {
            case NORTH -> vehicle.getY();
            case SOUTH -> HEIGHT - vehicle.getY();
            case EAST -> WIDTH - vehicle.getX();
            case WEST -> vehicle.getX();
        };
    }

    private long calculateGreenDuration(boolean verticalGreen) {
        long waitingVehicles = vehicles.stream()
                .filter(vehicle -> !vehicle.hasEnteredIntersection())
                .filter(vehicle -> switch (vehicle.getDirection()) {
                    case NORTH, SOUTH -> verticalGreen;
                    case EAST, WEST -> !verticalGreen;
                })
                .count();

        long duration = MIN_LIGHT_PHASE_DURATION
                + waitingVehicles * EXTRA_TIME_PER_VEHICLE;

        return Math.min(duration, MAX_LIGHT_PHASE_DURATION);
    }

    private void updateTrafficLights(boolean verticalGreen) {
        northLight.setGreen(verticalGreen);
        southLight.setGreen(verticalGreen);
        eastLight.setGreen(!verticalGreen);
        westLight.setGreen(!verticalGreen);
    }

    private void updateVehicles() {
        for (Vehicle vehicle : List.copyOf(vehicles)) {
            boolean greenLight = switch (vehicle.getDirection()) {
                case NORTH, SOUTH -> northLight.isGreen();
                case EAST, WEST -> eastLight.isGreen();
            };

            boolean intersectionAvailable = vehicles.stream()
                    .noneMatch(other ->
                            other != vehicle && other.hasEnteredIntersection());

            vehicle.update(greenLight, vehicles, intersectionAvailable);
        }
    }

    private void drawBackground() {
        root.setStyle("-fx-background-color: #2f6f3e;");
    }

    private void drawRoads() {
        Rectangle horizontalRoad = new Rectangle(0, 250, WIDTH, 200);
        horizontalRoad.setFill(Color.DIMGRAY);

        Rectangle verticalRoad = new Rectangle(400, 0, 200, HEIGHT);
        verticalRoad.setFill(Color.DIMGRAY);

        root.getChildren().addAll(horizontalRoad, verticalRoad);

        for (int x = 0; x < WIDTH; x += 40) {
            Rectangle marking = new Rectangle(x, 348, 25, 4);
            marking.setFill(Color.WHITE);
            root.getChildren().add(marking);
        }

        for (int y = 0; y < HEIGHT; y += 40) {
            Rectangle marking = new Rectangle(498, y, 4, 25);
            marking.setFill(Color.WHITE);
            root.getChildren().add(marking);
        }
    }

    private void drawStopLines() {
        Rectangle northStop = new Rectangle(400, 230, 200, 6);
        Rectangle southStop = new Rectangle(400, 464, 200, 6);
        Rectangle westStop = new Rectangle(380, 250, 6, 200);
        Rectangle eastStop = new Rectangle(614, 250, 6, 200);

        northStop.setFill(Color.WHITE);
        southStop.setFill(Color.WHITE);
        westStop.setFill(Color.WHITE);
        eastStop.setFill(Color.WHITE);

        root.getChildren().addAll(
                northStop,
                southStop,
                westStop,
                eastStop
        );
    }

    private void drawTrafficLights() {
        northLight = new TrafficLight(375, 210);
        southLight = new TrafficLight(605, 475);
        westLight = new TrafficLight(365, 235);
        eastLight = new TrafficLight(625, 450);

        root.getChildren().addAll(
                northLight.getBody(),
                southLight.getBody(),
                westLight.getBody(),
                eastLight.getBody()
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
