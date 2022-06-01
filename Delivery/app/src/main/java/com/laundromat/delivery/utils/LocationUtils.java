package com.laundromat.delivery.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

;

public class LocationUtils {

    public static String getAddressFromLatLng(Context context, double latitude, double longitude) {

        Geocoder myLocation = new Geocoder(context, Locale.getDefault());
        List<Address> myList;
        try {
            myList = myLocation.getFromLocation(latitude, longitude, 1);
            Address address = myList.get(0);
            String addressStr = "";

            addressStr += address.getAddressLine(0) + ", ";

            return addressStr;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static double getDistanceBetweenTwoPoints(LatLng source, LatLng destination) {

        double distanceInMeters = SphericalUtil.computeDistanceBetween(source, destination);
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(distanceInMeters / 1000));
    }
}
