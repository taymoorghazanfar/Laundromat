package com.laundromat.delivery.model;

import com.google.android.gms.maps.model.LatLng;
import com.laundromat.delivery.model.observers.ITripObserver;
import com.laundromat.delivery.model.observers.ITripSubject;
import com.laundromat.delivery.model.util.TripStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeliveryBoy extends User implements ITripSubject {

    private final ArrayList<ITripObserver> observers = new ArrayList<>();
    private Date dateOfBirth;
    private String nicNumber;
    private LatLng currentLocation;
    private float currentSpeed;
    private String nicImageUrl;
    private String licenseNumber;
    private String licenseImageUrl;
    private Vehicle vehicle;
    private List<Trip> trips;
    private boolean isAvailable;

    public DeliveryBoy() {

        // Required empty constructor
        super();
        this.currentSpeed = 0;
        isAvailable = true;
        trips = new ArrayList<>();
    }

    public DeliveryBoy(Date dateOfBirth, String nicNumber,
                       LatLng currentLocation, String nicImageUrl, String licenseNumber,
                       String licenseImageUrl, Vehicle vehicle, List<Trip> trips, boolean isAvailable) {
        this.dateOfBirth = dateOfBirth;
        this.nicNumber = nicNumber;
        this.currentLocation = currentLocation;
        this.currentSpeed = 0;
        this.nicImageUrl = nicImageUrl;
        this.licenseNumber = licenseNumber;
        this.licenseImageUrl = licenseImageUrl;
        this.vehicle = vehicle;
        this.trips = trips;
        this.isAvailable = isAvailable;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getNicNumber() {
        return nicNumber;
    }

    public void setNicNumber(String nicNumber) {
        this.nicNumber = nicNumber;
    }

    public LatLng getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(LatLng currentLocation) {
        this.currentLocation = currentLocation;
    }

    public float getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(float currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public String getNicImageUrl() {
        return nicImageUrl;
    }

    public void setNicImageUrl(String nicImageUrl) {
        this.nicImageUrl = nicImageUrl;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getLicenseImageUrl() {
        return licenseImageUrl;
    }

    public void setLicenseImageUrl(String licenseImageUrl) {
        this.licenseImageUrl = licenseImageUrl;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public List<Trip> getTrips() {
        return trips;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public void registerObserver(ITripObserver observer) {

        this.observers.add(observer);
    }

    @Override
    public void notifyObservers(String task, String tripId, TripStatus status) {

        for (ITripObserver observer : observers) {

            observer.updateView(task, tripId, status);
        }
    }
}
