package com.laundromat.customer.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.laundromat.customer.R;
import com.laundromat.customer.model.Laundry;
import com.squareup.picasso.Picasso;

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

    public static void showLocation(Context context, String laundryName, LatLng laundryLocation) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();

        final View dialogView = layoutInflater
                .inflate(R.layout.dialog_show_location, null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(true);

        // init views
        ImageView imageViewLocation = dialogView.findViewById(R.id.image_view_location);
        TextView textViewLocationName = dialogView.findViewById(R.id.text_view_laundry_name);
        TextView textViewLocationAddress = dialogView.findViewById(R.id.text_view_location_address);

        String locationUrl = StringUtils.getMapsStaticImageUrl(context,
                laundryLocation);

        Picasso.get()
                .load(locationUrl)
                .into(imageViewLocation);

        textViewLocationName.setText(laundryName);

        String locationAddress = LocationUtils.getAddressFromLatLng(context,
                laundryLocation.latitude,
                laundryLocation.longitude);
        textViewLocationAddress.setText(locationAddress);

        dialog.show();
    }
}
