package com.laundromat.merchant.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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

    @SuppressLint("DefaultLocale")
    public static String getEstimatedTime(double distance, float speed) {

        if (distance == 0 || speed == 0) {

            return null;
        }

        double speedKm = speed * 1.60934;

        double hours = speedKm * distance;

        long millis = hoursToMillis(hours);

        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    private static long hoursToMillis(double hour) {
        return minutesToMillis(hour * 60);
    }

    private static long minutesToMillis(double minutes) {
        return (long) (minutes * 60 * 1000);
    }
}
