package com.laundromat.admin.utils;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.laundromat.admin.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

public class StringUtils {

    private static String mapsStaticImageUrl = "https://maps.googleapis.com/maps/api/staticmap?center=LAT,LNG&zoom=15&size=600x300&maptype=roadmap&markers=color:red%7Clabel%7CLAT,LNG&key=API_KEY";

    public static String getMapsStaticImageUrl(Context context, LatLng location) {

        String url = mapsStaticImageUrl;

        String lat = String.valueOf(location.latitude);
        String lng = String.valueOf(location.longitude);
        String apiKey = context.getString(R.string.google_maps_api_key);

        url = url.replace("LAT", lat);
        url = url.replace("LNG", lng);
        url = url.replace("API_KEY", apiKey);

        return url;
    }

    public static String getCurrentDateTime() {

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return df.format(c.getTime());
    }
}
