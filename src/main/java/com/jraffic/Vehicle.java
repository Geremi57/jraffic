package com.jraffic;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class Vehicle {

    private static final double WIDTH = 24;
    private static final double HEIGHT = 14;
    private static final double SPEED = 2.0;
    private static final double SAFETY_GAP = 30;

    private Direction direction;
    private final Direction approachDirection;
    private final Route route;
    private final Rectangle body;

    private double x;
    private double y;
    private boolean hasEnteredIntersection;
    private boolean hasTurned;
    private double turnProgress;
    private double turnStartX;
    private double turnStartY;
    private double control1X;
    private double control1Y;
    private double control2X;
    private double control2Y;
    private double turnEndX;
    private double turnEndY;
    private Direction exitDirection;

    public Vehicle(Direction direction, Route route) {
        this.direction = direction;
        this.approachDirection = direction;
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
        switch (approachDirection) {
            case NORTH -> {
                x = 538;
                y = 0;
            }
            case SOUTH -> {
                x = 438;
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

        if (hasEnteredIntersection && route != Route.STRAIGHT && !hasTurned) {
            moveTurningVehicle();
        } else {
            switch (direction) {
                case NORTH -> y += SPEED;
                case SOUTH -> y -= SPEED;
                case EAST -> x -= SPEED;
                case WEST -> x += SPEED;
            }
        }

        if (!hasEnteredIntersection && crossesStopLine(previousX, previousY)) {
            if (greenLight && intersectionAvailable) {
                hasEnteredIntersection = true;

                if (route != Route.STRAIGHT) {
                    initializeTurn();
                }
            } else {
                x = previousX;
                y = previousY;
            }
        }

        updateShape();
        clearIntersectionReservationIfExited();
    }

    private void initializeTurn() {
        turnProgress = 0;
        turnStartX = x;
        turnStartY = y;

        switch (approachDirection) {
            case NORTH -> {
                exitDirection = route == Route.LEFT
                        ? Direction.WEST : Direction.EAST;
                turnEndX = route == Route.LEFT ? 620 : 366;
                turnEndY = route == Route.LEFT ? 388 : 288;
            }
            case SOUTH -> {
                exitDirection = route == Route.LEFT
                        ? Direction.EAST : Direction.WEST;
                turnEndX = route == Route.LEFT ? 366 : 620;
                turnEndY = route == Route.LEFT ? 288 : 388;
            }
            case EAST -> {
                exitDirection = route == Route.LEFT
                        ? Direction.NORTH : Direction.SOUTH;
                turnEndX = route == Route.LEFT ? 538 : 438;
                turnEndY = route == Route.LEFT ? 470 : 216;
            }
            case WEST -> {
                exitDirection = route == Route.LEFT
                        ? Direction.SOUTH : Direction.NORTH;
                turnEndX = route == Route.LEFT ? 438 : 538;
                turnEndY = route == Route.LEFT ? 216 : 470;
            }
        }

        double[] entryVector = movementVector(approachDirection);
        double[] exitVector = movementVector(exitDirection);
        double controlDistance = 75;

        control1X = turnStartX + entryVector[0] * controlDistance;
        control1Y = turnStartY + entryVector[1] * controlDistance;
        control2X = turnEndX - exitVector[0] * controlDistance;
        control2Y = turnEndY - exitVector[1] * controlDistance;
    }

    private double[] movementVector(Direction direction) {
        return switch (direction) {
            case NORTH -> new double[]{0, 1};
            case SOUTH -> new double[]{0, -1};
            case EAST -> new double[]{-1, 0};
            case WEST -> new double[]{1, 0};
        };
    }

    private void moveTurningVehicle() {
        turnProgress = Math.min(1.0, turnProgress + SPEED / 100.0);

        double t = turnProgress;
        double inverse = 1.0 - t;

        x = inverse * inverse * inverse * turnStartX
                + 3 * inverse * inverse * t * control1X
                + 3 * inverse * t * t * control2X
                + t * t * t * turnEndX;

        y = inverse * inverse * inverse * turnStartY
                + 3 * inverse * inverse * t * control1Y
                + 3 * inverse * t * t * control2Y
                + t * t * t * turnEndY;

        body.setRotate((route == Route.LEFT ? -90 : 90) * t);

        if (turnProgress >= 1.0) {
            direction = exitDirection;
            hasTurned = true;
            body.setRotate(0);
        }
    }

    public boolean hasEnteredIntersection() {
        return hasEnteredIntersection;
    }

    private void clearIntersectionReservationIfExited() {
        if (!hasEnteredIntersection) {
            return;
        }

        if (route != Route.STRAIGHT && !hasTurned) {
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

            boolean otherIsAhead = switch (direction) {
                case NORTH -> other.y > y;
                case SOUTH -> other.y < y;
                case EAST -> other.x < x;
                case WEST -> other.x > x;
            };

            if (!otherIsAhead) {
                continue;
            }

            double gap = switch (direction) {
                case NORTH -> other.y - (y + body.getHeight());
                case SOUTH -> y - (other.y + other.body.getHeight());
                case EAST -> x - (other.x + other.body.getWidth());
                case WEST -> other.x - (x + body.getWidth());
            };

            if (gap < SAFETY_GAP) {
                return false;
            }
        }

        return true;
    }

    private boolean isAtStopLine() {
        return switch (approachDirection) {
            case NORTH -> y >= 216 && y < 250;
            case SOUTH -> y <= 470 && y > 450;
            case EAST -> x <= 620 && x > 600;
            case WEST -> x >= 366 && x < 400;
        };
    }

    private boolean crossesStopLine(double previousX, double previousY) {
        return switch (approachDirection) {
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
