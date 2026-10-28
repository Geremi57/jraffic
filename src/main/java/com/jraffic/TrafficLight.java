package com.jraffic;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class TrafficLight {

    private final Rectangle body;
    private boolean green;

    public TrafficLight(double x, double y) {
        body = new Rectangle(x, y, 20, 20);
        body.setArcWidth(5);
        body.setArcHeight(5);
        setGreen(false);
    }

    public void setGreen(boolean green) {
        this.green = green;
        body.setFill(green ? Color.LIMEGREEN : Color.RED);
    }

    public boolean isGreen() {
        return green;
    }

    public Rectangle getBody() {
        return body;
    }
}
