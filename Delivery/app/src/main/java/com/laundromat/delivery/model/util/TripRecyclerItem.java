package com.laundromat.delivery.model.util;

import com.laundromat.delivery.model.Trip;

public class TripRecyclerItem {

    private Trip trip;
    private String routeUrl;

    public TripRecyclerItem(){


    }

    public TripRecyclerItem(Trip trip, String routeUrl) {
        this.trip = trip;
        this.routeUrl = routeUrl;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public String getRouteUrl() {
        return routeUrl;
    }

    public void setRouteUrl(String routeUrl) {
        this.routeUrl = routeUrl;
    }
}
