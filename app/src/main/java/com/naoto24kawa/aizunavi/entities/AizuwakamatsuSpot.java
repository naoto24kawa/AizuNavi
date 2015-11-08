package com.naoto24kawa.aizunavi.entities;

public class AizuwakamatsuSpot extends Spot {

    private String name;

    private double Latitude;

    private double Longitude;

    public AizuwakamatsuSpot() {
        super();
    }

    public AizuwakamatsuSpot(String name, double latitude, double longitude) {
        super(name, null, latitude, longitude);
    }
}
