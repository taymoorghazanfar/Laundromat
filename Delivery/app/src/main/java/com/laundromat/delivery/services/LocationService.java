package com.laundromat.delivery.services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.delivery.R;
import com.laundromat.delivery.prefs.Session;
import com.laundromat.delivery.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class LocationService extends Service {

    private LocationCallback locationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            if (locationResult != null && locationResult.getLastLocation() != null) {

                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                float speed = locationResult.getLastLocation().getSpeed();

                Log.d("location_service", "onLocationResult: latitude: "
                        + latitude + ", longitude: " + longitude);

                String phoneNumber = Session.getPhoneNumber(getApplicationContext());

                if (phoneNumber != null) {

                    updateCurrentLocation(phoneNumber, latitude, longitude, speed);
                }
            }
        }
    };

    private void updateCurrentLocation(String phoneNumber, double latitude, double longitude, float speed) {

        // update location locally
        if (Session.user != null) {

            Session.user.setCurrentLocation(new LatLng(latitude, longitude));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("phone_number", phoneNumber);
        data.put("latitude", latitude);
        data.put("longitude", longitude);
        data.put("speed", speed);

        // update current location in cloud
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("delivery_boy-updateCurrentLocation")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    Log.d("location_service",
                            "onLocationResult: driver location updated");
                })
                .addOnFailureListener(e -> {

                    Log.d("location_service",
                            "onLocationResult: failed to update driver location");
                    Log.d("location_service",
                            "onLocationResult: " + e.getMessage());
                });
    }

    private void startLocationService() {

        String channelId = "location_notification_channel";

        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(

                getApplicationContext(),
                channelId
        );

        builder.setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Laundromat Delivery")
                .setContentText("Tracking your live location")
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(false)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {

                NotificationChannel notificationChannel = new NotificationChannel(

                        channelId,
                        "Location Service",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("Location service notification channel");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        startForeground(Constants.LOCATION_SERVICE_ID, builder.build());
    }

    private void stopLocationService() {

        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {

            String action = intent.getAction();

            if (action != null) {

                if (action.equals(Constants.ACTION_START_LOCATION_SERVICE)) {

                    startLocationService();

                } else if (action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)) {

                    stopLocationService();
                }
            }
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}