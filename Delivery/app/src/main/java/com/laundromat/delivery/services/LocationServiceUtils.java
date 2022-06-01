package com.laundromat.delivery.services;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.laundromat.delivery.utils.Constants;

public class LocationServiceUtils {

    public static boolean isLocationServiceRunning(Context context) {

        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager != null) {

            for (ActivityManager.RunningServiceInfo serviceInfo
                    : activityManager.getRunningServices(Integer.MAX_VALUE)) {

                if (LocationService.class.getName().equals(serviceInfo.service.getClassName())) {

                    if (serviceInfo.foreground) {

                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    public static void startLocationService(Context context) {

        if (!isLocationServiceRunning(context)) {

            Intent intent = new Intent(context.getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            context.startService(intent);

            Log.d("location_service", "startLocationService: location service started");
        }
    }

    public static void stopLocationService(Context context) {

        if (isLocationServiceRunning(context)) {

            Intent intent = new Intent(context.getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            context.startService(intent);

            Log.d("location_service", "startLocationService: location service stopped");
        }
    }
}
