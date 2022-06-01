package com.laundromat.merchant.model.util;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class Location implements Parcelable {

    private String name;
    private LatLng latLng;

    public Location() {


    }

    public Location(String name, LatLng latLng) {
        this.name = name;
        this.latLng = latLng;
    }

    protected Location(Parcel in) {
        name = in.readString();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeParcelable(latLng, i);
    }
}
