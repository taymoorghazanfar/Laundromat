package com.laundromat.delivery.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {

    public static List<String> requestedPermissions = new ArrayList<>();

    public static List<String> getRequestedPermissions(Context context) {

        int camera = ContextCompat
                .checkSelfPermission(context, Manifest.permission.CAMERA);
        int storageWrite = ContextCompat
                .checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int storageRead = ContextCompat
                .checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);

        int fineLocation = ContextCompat
                .checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

        int coarseLocation = ContextCompat
                .checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (camera != PackageManager.PERMISSION_GRANTED) {
            requestedPermissions.add(Manifest.permission.CAMERA);
        }
        if (storageWrite != PackageManager.PERMISSION_GRANTED) {
            requestedPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (storageRead != PackageManager.PERMISSION_GRANTED) {
            requestedPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (fineLocation != PackageManager.PERMISSION_GRANTED) {
            requestedPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (coarseLocation != PackageManager.PERMISSION_GRANTED) {
            requestedPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

            int backgroundLocation = ContextCompat
                    .checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION);

            if (backgroundLocation != PackageManager.PERMISSION_GRANTED) {
                requestedPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        }

        return requestedPermissions;
    }
}
