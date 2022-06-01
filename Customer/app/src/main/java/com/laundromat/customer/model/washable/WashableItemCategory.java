package com.laundromat.customer.model.washable;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WashableItemCategory implements Parcelable {

    public static final Creator<WashableItemCategory> CREATOR = new Creator<WashableItemCategory>() {
        @Override
        public WashableItemCategory createFromParcel(Parcel in) {
            return new WashableItemCategory(in);
        }

        @Override
        public WashableItemCategory[] newArray(int size) {
            return new WashableItemCategory[size];
        }
    };
    private String title;
    private Date dateCreated;
    private List<WashableItem> washableItems;

    public WashableItemCategory(String title) {
        this.title = title;
        this.dateCreated = Calendar.getInstance().getTime();
        this.washableItems = new ArrayList<>();
    }

    protected WashableItemCategory(Parcel in) {
        title = in.readString();
        washableItems = in.createTypedArrayList(WashableItem.CREATOR);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public List<WashableItem> getWashableItems() {
        return washableItems;
    }

    public void setWashableItems(List<WashableItem> washableItems) {
        this.washableItems = washableItems;
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
        parcel.writeString(title);
        parcel.writeTypedList(washableItems);
    }
}
