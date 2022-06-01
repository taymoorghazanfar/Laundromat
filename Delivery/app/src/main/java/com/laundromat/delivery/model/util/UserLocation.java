package com.laundromat.delivery.model.util;

public class UserLocation {

    private String locationName;
    private boolean isSelected;

    public UserLocation(){

        // required empty constructor
    }

    public UserLocation(String locationName, boolean isSelected) {
        this.locationName = locationName;
        this.isSelected = isSelected;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
