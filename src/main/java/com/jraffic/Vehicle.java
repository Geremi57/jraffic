package com.jraffic;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class Vehicle {

    private static final double WIDTH = 24;
    private static final double HEIGHT = 14;
    private static final double SPEED = 2.0;
    private static final double SAFETY_GAP = 30;

    private final Direction direction;
    private final Route route;
    private final Rectangle body;

    private double x;
    private double y;
    private boolean hasEnteredIntersection;

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
        update(true, List.of());
    }

    public void update(boolean greenLight) {
        update(greenLight, List.of());
    }

    public void update(boolean greenLight, List<Vehicle> vehicles) {
        update(greenLight, vehicles, true);
    }

    public void update(
            boolean greenLight,
            List<Vehicle> vehicles,
            boolean intersectionAvailable
    ) {
        if (!greenLight && !hasEnteredIntersection && isAtStopLine()) {
            return;
        }

        if (!hasSpaceAhead(vehicles)) {
            return;
        }

        double previousX = x;
        double previousY = y;

        switch (direction) {
            case NORTH -> y += SPEED;
            case SOUTH -> y -= SPEED;
            case EAST -> x -= SPEED;
            case WEST -> x += SPEED;
        }

        if (!hasEnteredIntersection && crossesStopLine(previousX, previousY)) {
            if (greenLight && intersectionAvailable) {
                hasEnteredIntersection = true;
            } else {
                x = previousX;
                y = previousY;
            }
        }

        updateShape();
        clearIntersectionReservationIfExited();
    }

    public boolean hasEnteredIntersection() {
        return hasEnteredIntersection;
    }

    private void clearIntersectionReservationIfExited() {
        if (!hasEnteredIntersection) {
            return;
        }

        boolean cleared = switch (direction) {
            case NORTH -> y >= 450;
            case SOUTH -> y + body.getHeight() <= 250;
            case EAST -> x + body.getWidth() <= 400;
            case WEST -> x >= 600;
        };

        if (cleared) {
            hasEnteredIntersection = false;
        }
    }

    private boolean hasSpaceAhead(List<Vehicle> vehicles) {
        for (Vehicle other : vehicles) {
            if (other == this || other.direction != direction) {
                continue;
            }

            double gap = switch (direction) {
                case NORTH -> other.y - (y + body.getHeight());
                case SOUTH -> y - (other.y + other.body.getHeight());
                case EAST -> x - (other.x + other.body.getWidth());
                case WEST -> other.x - (x + body.getWidth());
            };

            if (gap >= 0 && gap < SAFETY_GAP) {
                return false;
            }
        }

        return true;
    }

    private boolean isAtStopLine() {
        return switch (direction) {
            case NORTH -> y >= 216 && y < 250;
            case SOUTH -> y <= 470 && y > 450;
            case EAST -> x <= 620 && x > 600;
            case WEST -> x >= 366 && x < 400;
        };
    }

    private boolean crossesStopLine(double previousX, double previousY) {
        return switch (direction) {
            case NORTH -> previousY < 216 && y >= 216;
            case SOUTH -> previousY > 470 && y <= 470;
            case EAST -> previousX > 620 && x <= 620;
            case WEST -> previousX < 366 && x >= 366;
        };
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
