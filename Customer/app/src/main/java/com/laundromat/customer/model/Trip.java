package com.laundromat.customer.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.laundromat.customer.model.order.Order;
import com.laundromat.customer.model.util.TripStatus;
import com.laundromat.customer.model.util.TripType;
import com.laundromat.customer.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Trip {

    String id;
    String driverId;
    LatLng source;
    LatLng destination;
    double distance;
    double cost;
    TripType type;
    TripStatus status;
    Order order;
    boolean isPayed;
    String dateStarted;
    String dateFinished;
    String dateCreated;

    public Trip() {

        // Required empty constructor
        dateCreated = StringUtils.getCurrentDateTime();
        dateFinished = "";
        this.isPayed = false;
    }

    public Trip(String id, String driverId, LatLng source,
                LatLng destination, double distance,
                double cost, TripType type, TripStatus status,
                Order order, boolean isPayed, String dateStarted,
                String dateFinished, String dateCreated) {

        this.id = id;
        this.driverId = driverId;
        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.cost = cost;
        this.type = type;
        this.status = status;
        this.order = order;
        this.isPayed = isPayed;
        this.dateStarted = dateStarted;
        this.dateFinished = dateFinished;
        this.dateCreated = dateCreated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public LatLng getSource() {
        return source;
    }

    public void setSource(LatLng source) {
        this.source = source;
    }

    public LatLng getDestination() {
        return destination;
    }

    public void setDestination(LatLng destination) {
        this.destination = destination;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public TripType getType() {
        return type;
    }

    public void setType(TripType type) {
        this.type = type;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(String dateStarted) {
        this.dateStarted = dateStarted;
    }

    public String getDateFinished() {
        return dateFinished;
    }

    public void setDateFinished(String dateFinished) {
        this.dateFinished = dateFinished;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isPayed() {
        return isPayed;
    }

    public void setPayed(boolean payed) {
        isPayed = payed;
    }

    public JSONObject toJson() {

        Gson gson = new Gson();
        String jsonString = gson.toJson(this);

        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            return null;
        }
    }
}
