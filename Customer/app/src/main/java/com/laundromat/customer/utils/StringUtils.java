package com.laundromat.customer.utils;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.laundromat.customer.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

public class StringUtils {

    private static String mapsStaticImageUrl = "https://maps.googleapis.com/maps/api/staticmap?center=LAT,LNG&zoom=15&size=600x300&maptype=roadmap&markers=color:red%7Clabel%7CLAT,LNG&key=API_KEY";

    public static String getMapsStaticImageUrl(Context context, LatLng location) {

        String url = mapsStaticImageUrl;

        String lat = String.valueOf(location.latitude);
        String lng = String.valueOf(location.longitude);
        String apiKey = context.getString(R.string.google_api_key);

        url = url.replace("LAT", lat);
        url = url.replace("LNG", lng);
        url = url.replace("API_KEY", apiKey);

        return url;
    }

    public static String getCurrentDateTime() {

        Calendar c = Calendar.getInstance();

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
