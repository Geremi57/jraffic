package com.jraffic;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Jraffic extends Application {

    private static final double WIDTH = 1000;
    private static final double HEIGHT = 700;

    @Override
    public void start(Stage stage) {
        Pane root = new Pane();

        drawBackground(root);
        drawRoads(root);
        drawStopLines(root);
        drawTrafficLights(root);

        Vehicle vehicle = new Vehicle(
                Direction.NORTH,
                Route.STRAIGHT
        );

        root.getChildren().add(vehicle.getBody());

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                vehicle.update();
            }
        };

        timer.start();

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        stage.setTitle("Jraffic");
        stage.setScene(scene);
        stage.show();
    }

    private void drawBackground(Pane root) {
        root.setStyle("-fx-background-color: #2f6f3e;");
    }

    private void drawRoads(Pane root) {
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

    private void drawStopLines(Pane root) {
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

    private void drawTrafficLights(Pane root) {
        addTrafficLight(root, 375, 210);
        addTrafficLight(root, 605, 475);
        addTrafficLight(root, 365, 235);
        addTrafficLight(root, 625, 450);
    }

    private void addTrafficLight(Pane root, double x, double y) {
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
