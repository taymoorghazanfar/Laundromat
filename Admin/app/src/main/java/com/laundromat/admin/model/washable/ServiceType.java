package com.laundromat.admin.model.washable;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

public class ServiceType implements Parcelable {

    public static final Creator<ServiceType> CREATOR = new Creator<ServiceType>() {
        @Override
        public ServiceType createFromParcel(Parcel in) {
            return new ServiceType(in);
        }

        @Override
        public ServiceType[] newArray(int size) {
            return new ServiceType[size];
        }
    };
    private String id;
    private String name;
    private double price;
    private int quantity;
    private boolean isActive;
    private Date dateCreated;

    public ServiceType(String name) {
        this.name = name;
        this.price = 0;
        this.isActive = true;
        this.dateCreated = Calendar.getInstance().getTime();
    }

    // copy constructor
    public ServiceType(ServiceType serviceType) {

        this.id = serviceType.id;
        this.name = serviceType.name;
        this.price = serviceType.price;
        this.quantity = serviceType.quantity;
        this.isActive = serviceType.isActive;
        this.dateCreated = serviceType.dateCreated;
    }

    protected ServiceType(Parcel in) {
        id = in.readString();
        name = in.readString();
        price = in.readDouble();
        quantity = in.readInt();
        isActive = in.readByte() != 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeDouble(price);
        parcel.writeInt(quantity);
        parcel.writeByte((byte) (isActive ? 1 : 0));
    }
}
