package com.laundromat.admin.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.laundromat.admin.model.washable.WashableItem;

import org.json.JSONException;
import org.json.JSONObject;

public class SaleItem implements Parcelable {

    public static final Creator<SaleItem> CREATOR = new Creator<SaleItem>() {
        @Override
        public SaleItem createFromParcel(Parcel in) {
            return new SaleItem(in);
        }

        @Override
        public SaleItem[] newArray(int size) {
            return new SaleItem[size];
        }
    };

    private WashableItem washableItem;
    private int quantity;
    private double price;

    private SaleItem() {

    }

    public SaleItem(WashableItem washableItem, int quantity, double price) {
        this.washableItem = washableItem;
        this.quantity = quantity;
        this.price = price;
    }

    protected SaleItem(Parcel in) {
        washableItem = in.readParcelable(WashableItem.class.getClassLoader());
        quantity = in.readInt();
        price = in.readDouble();
    }

    public WashableItem getWashableItem() {
        return washableItem;
    }

    public void setWashableItem(WashableItem washableItem) {
        this.washableItem = washableItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
        parcel.writeParcelable(washableItem, i);
        parcel.writeInt(quantity);
        parcel.writeDouble(price);
    }
}
