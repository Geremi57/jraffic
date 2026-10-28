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

    private final List<Vehicle> vehicles = new ArrayList<>();
    private final Random random = new Random();

    private Pane root;

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

        root.requestFocus();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateVehicles();
            }
        };

        timer.start();
    }

    private Direction directionFromKey(KeyCode key) {
        return switch (key) {
            case UP -> Direction.NORTH;
            case DOWN -> Direction.SOUTH;
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

    private void updateVehicles() {
        for (Vehicle vehicle : vehicles) {
            vehicle.update();
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
        addTrafficLight(375, 210);
        addTrafficLight(605, 475);
        addTrafficLight(365, 235);
        addTrafficLight(625, 450);
    }

    private void addTrafficLight(double x, double y) {
        Rectangle light = new Rectangle(x, y, 20, 20);
        light.setFill(Color.RED);
        light.setArcWidth(5);
        light.setArcHeight(5);
        root.getChildren().add(light);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
