package com.laundromat.admin.model.util;

public class FareData {

    private double baseFare;
    private double perKm;
    private double distance;

    public FareData(double baseFare, double perKm, double distance) {
        this.baseFare = baseFare;
        this.perKm = perKm;
        this.distance = distance;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(double baseFare) {
        this.baseFare = baseFare;
    }

    public double getPerKm() {
        return perKm;
    }

    public void setPerKm(double perKm) {
        this.perKm = perKm;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
