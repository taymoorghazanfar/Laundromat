package com.laundromat.delivery.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.annotation.NonNull;

import java.util.List;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER;

public class PackageUtils {

    public static boolean getAppState(@NonNull Context context, @NonNull String packageName) {
        final PackageManager packageManager = context.getPackageManager();

        // Check if the App is installed or not first
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent == null) {
            return false;
        }
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.isEmpty()) {
            // App is not installed
            return false;
        } else {

            // Check if the App is enabled/disabled
            int appEnabledSetting = packageManager.getApplicationEnabledSetting(packageName);
            return appEnabledSetting != COMPONENT_ENABLED_STATE_DISABLED &&
                    appEnabledSetting != COMPONENT_ENABLED_STATE_DISABLED_USER;
        }
    }
}
