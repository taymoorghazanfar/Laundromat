package com.laundromat.merchant.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.laundromat.merchant.model.order.Order;
import com.laundromat.merchant.model.util.Timings;
import com.laundromat.merchant.model.washable.WashableItemCategory;
import com.laundromat.merchant.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Laundry implements Parcelable {

    public static final Creator<Laundry> CREATOR = new Creator<Laundry>() {
        @Override
        public Laundry createFromParcel(Parcel in) {
            return new Laundry(in);
        }

        @Override
        public Laundry[] newArray(int size) {
            return new Laundry[size];
        }
    };

    private String id;
    private String name;
    private String logoUrl;
    private LatLng location;
    private boolean isHomeBased;
    private boolean isActive;
    private String dateCreated;
    private double discount;
    private Timings timings;
    private List<WashableItemCategory> menu;
    private List<Order> orders;

    public Laundry(String name, String logoUrl,
                   LatLng location, boolean isHomeBased, Timings timings) {
        this.name = name;
        this.logoUrl = logoUrl;
        this.location = location;
        this.isHomeBased = isHomeBased;
        this.timings = timings;
        this.dateCreated = StringUtils.getCurrentDateTime();
        this.discount = 0;
        this.isActive = true;
        this.menu = new ArrayList<>();
        this.orders = new ArrayList<>();
    }

    protected Laundry(Parcel in) {
        id = in.readString();
        name = in.readString();
        logoUrl = in.readString();
        location = in.readParcelable(LatLng.class.getClassLoader());
        isHomeBased = in.readByte() != 0;
        isActive = in.readByte() != 0;
        dateCreated = in.readString();
        discount = in.readDouble();
        timings = in.readParcelable(Timings.class.getClassLoader());
        menu = in.createTypedArrayList(WashableItemCategory.CREATOR);
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

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public boolean isHomeBased() {
        return isHomeBased;
    }

    public void setHomeBased(boolean homeBased) {
        isHomeBased = homeBased;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Timings getTimings() {
        return timings;
    }

    public void setTimings(Timings timings) {
        this.timings = timings;
    }

    public List<WashableItemCategory> getMenu() {
        return menu;
    }

    public void setMenu(List<WashableItemCategory> menu) {
        this.menu = menu;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
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
        parcel.writeString(logoUrl);
        parcel.writeParcelable(location, i);
        parcel.writeByte((byte) (isHomeBased ? 1 : 0));
        parcel.writeByte((byte) (isActive ? 1 : 0));
        parcel.writeString(dateCreated);
        parcel.writeDouble(discount);
        parcel.writeParcelable(timings, i);
        parcel.writeTypedList(menu);
    }
}
