package com.laundromat.customer.model.util;

public class LiveLocationData {

    private double latitude;
    private double longitude;
    private float speed;

    public LiveLocationData() {


    }

    public LiveLocationData(double latitude, double longitude, float speed) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
