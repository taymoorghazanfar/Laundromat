package com.laundromat.merchant.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.laundromat.merchant.R;

public class MerchantNotificationManager {

    private static final int NOTIFICATION_REQUEST = 1;
    private static final String CHANNEL_FCM = "fcm_notification";
    private static MerchantNotificationManager instance;
    private Context context;

    private MerchantNotificationManager(Context context) {

        this.context = context;
    }

    public static MerchantNotificationManager getInstance(Context context) {
        if (instance == null) {

            instance = new MerchantNotificationManager(context);
        }

        return instance;
    }

    public void showNotification(Context context, String title, String message, Intent intent) {

        PendingIntent pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_REQUEST, intent, PendingIntent.FLAG_ONE_SHOT);
        String CHANNEL_ID = "fcm_notifications";// The id of the channel.

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(NOTIFICATION_REQUEST, notificationBuilder.build()); // 0 is the request code, it should be unique id
    }
}
