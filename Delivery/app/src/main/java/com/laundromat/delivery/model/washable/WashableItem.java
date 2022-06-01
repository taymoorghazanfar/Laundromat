package com.laundromat.delivery.model.washable;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WashableItem implements Parcelable {

    public static final Creator<WashableItem> CREATOR = new Creator<WashableItem>() {
        @Override
        public WashableItem createFromParcel(Parcel in) {
            return new WashableItem(in);
        }

        @Override
        public WashableItem[] newArray(int size) {
            return new WashableItem[size];
        }
    };
    private String name;
    private String imageUrl;
    private List<ServiceType> serviceTypes;
    private Date dateCreated;

    public WashableItem(String name) {
        this.name = name;
        this.serviceTypes = new ArrayList<>();
        this.dateCreated = Calendar.getInstance().getTime();
    }

    protected WashableItem(Parcel in) {
        name = in.readString();
        imageUrl = in.readString();
        serviceTypes = in.createTypedArrayList(ServiceType.CREATOR);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(List<ServiceType> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
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
        parcel.writeString(imageUrl);
        parcel.writeTypedList(serviceTypes);
    }
}
