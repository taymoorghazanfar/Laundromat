package com.laundromat.merchant.model.util;

import android.os.Parcel;
import android.os.Parcelable;

public class Timings implements Parcelable {

    public static final Creator<Timings> CREATOR = new Creator<Timings>() {
        @Override
        public Timings createFromParcel(Parcel in) {
            return new Timings(in);
        }

        @Override
        public Timings[] newArray(int size) {
            return new Timings[size];
        }
    };
    private String openingTime;
    private String closingTime;

    public Timings(String openingTime, String closingTime) {
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    protected Timings(Parcel in) {
        openingTime = in.readString();
        closingTime = in.readString();
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(openingTime);
        parcel.writeString(closingTime);
    }
}
