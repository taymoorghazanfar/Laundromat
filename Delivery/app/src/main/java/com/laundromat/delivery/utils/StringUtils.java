package com.laundromat.delivery.utils;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.laundromat.delivery.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

public class StringUtils {

    private static String mapsStaticImageUrl = "https://maps.googleapis.com/maps/api/staticmap?center=LAT,LNG&zoom=15&size=600x300&maptype=roadmap&markers=color:red%7Clabel%7CLAT,LNG&key=API_KEY";
    private static String directionsUrl = "https://maps.googleapis.com/maps/api/directions/json?origin=LAT1,LNG1&destination=LAT2,LNG2&mode=driving&key=API_KEY";

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

    public static String getDirectionsUrl(Context context, LatLng location1, LatLng location2) {

        String url = directionsUrl;

        String lat1 = String.valueOf(location1.latitude);
        String lng1 = String.valueOf(location1.longitude);
        String lat2 = String.valueOf(location2.latitude);
        String lng2 = String.valueOf(location2.longitude);
        String apiKey = context.getString(R.string.google_maps_api_key);

        url = url.replace("LAT1", lat1);
        url = url.replace("LNG1", lng1);
        url = url.replace("LAT2", lat2);
        url = url.replace("LNG2", lng2);
        url = url.replace("API_KEY", apiKey);

        return url;
    }

    public static String getCurrentDateTime() {

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return df.format(c.getTime());
    }

    public static String getRandomCode(int n) {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }
}
