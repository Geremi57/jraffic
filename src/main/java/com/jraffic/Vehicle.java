package com.jraffic;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Vehicle {

    private static final double WIDTH = 24;
    private static final double HEIGHT = 14;
    private static final double SPEED = 2.0;

    private final Direction direction;
    private final Route route;
    private final Rectangle body;

    private double x;
    private double y;

    public Vehicle(Direction direction, Route route) {
        this.direction = direction;
        this.route = route;

        body = new Rectangle(WIDTH, HEIGHT);
        body.setFill(routeColor(route));
        body.setArcWidth(5);
        body.setArcHeight(5);

        setSpawnPosition();
    }

    private Color routeColor(Route route) {
        return switch (route) {
            case STRAIGHT -> Color.BLUE;
            case LEFT -> Color.YELLOW;
            case RIGHT -> Color.ORANGE;
        };
    }

    private void setSpawnPosition() {
        switch (direction) {
            case NORTH -> {
                x = 438;
                y = 0;
            }
            case SOUTH -> {
                x = 538;
                y = 686;
            }
            case EAST -> {
                x = 986;
                y = 288;
            }
            case WEST -> {
                x = 0;
                y = 388;
            }
        }

        updateShape();
    }

    public void update() {
        switch (direction) {
            case NORTH -> y += SPEED;
            case SOUTH -> y -= SPEED;
            case EAST -> x -= SPEED;
            case WEST -> x += SPEED;
        }

        updateShape();
    }

    private void updateShape() {
        body.setX(x);
        body.setY(y);

        if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            body.setWidth(WIDTH);
            body.setHeight(HEIGHT);
        } else {
            body.setWidth(HEIGHT);
            body.setHeight(WIDTH);
        }
    }

    public Rectangle getBody() {
        return body;
    }

    public Direction getDirection() {
        return direction;
    }

    public Route getRoute() {
        return route;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
