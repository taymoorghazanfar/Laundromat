package com.laundromat.admin.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OrderItem implements Parcelable {

    public static final Creator<OrderItem> CREATOR = new Creator<OrderItem>() {
        @Override
        public OrderItem createFromParcel(Parcel in) {
            return new OrderItem(in);
        }

        @Override
        public OrderItem[] newArray(int size) {
            return new OrderItem[size];
        }
    };
    private Map<String, SaleItem> saleItems;
    private double price;

    public OrderItem() {

        saleItems = new HashMap<>();
    }

    public OrderItem(Map<String, SaleItem> saleItems, double price) {
        this.saleItems = saleItems;
        this.price = price;
    }

    protected OrderItem(Parcel in) {
        price = in.readDouble();
    }

    public Map<String, SaleItem> getSaleItems() {
        return saleItems;
    }

    public void setSaleItems(Map<String, SaleItem> saleItems) {
        this.saleItems = saleItems;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
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
        parcel.writeDouble(price);
    }
}
