package com.laundromat.customer.model.util;

import com.laundromat.customer.model.Laundry;

public class LaundryRecyclerItem{

    private Laundry laundry;
    private long duration;
    private double distance;

    public LaundryRecyclerItem() {

    }

    public LaundryRecyclerItem(Laundry laundry, long duration, double distance) {
        this.laundry = laundry;
        this.duration = duration;
        this.distance = distance;
    }

    public Laundry getLaundry() {
        return laundry;
    }

    public void setLaundry(Laundry laundry) {
        this.laundry = laundry;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
