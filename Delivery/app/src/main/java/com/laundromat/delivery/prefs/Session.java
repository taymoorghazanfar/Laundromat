package com.laundromat.delivery.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.laundromat.delivery.model.DeliveryBoy;

public class Session {

    private static final String PREFS_NAME = "user_credentials";
    private static final String SAVED_PHONE_NUMBER = "saved_phone_number";
    private static final String SAVED_PASSWORD = "saved_password";

    public static DeliveryBoy user;

    public static void setPhoneNumber(Context context, String phoneNumber) {

        SharedPreferences sharedPreferences = context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SAVED_PHONE_NUMBER, phoneNumber);
        editor.apply();
    }

    public static String getPhoneNumber(Context context) {

        SharedPreferences sharedPreferences = context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        return sharedPreferences.getString(SAVED_PHONE_NUMBER, null);
    }

    public static void setPassword(Context context, String password) {

        SharedPreferences sharedPreferences = context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SAVED_PASSWORD, password);
        editor.apply();
    }

    public static String getPassword(Context context) {

        SharedPreferences sharedPreferences = context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        return sharedPreferences.getString(SAVED_PASSWORD, null);
    }

    public static boolean userExist(Context context) {

        FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser == null) {

            return false;
        }

        return getPhoneNumber(context) != null && getPassword(context) != null;
    }

    public static void destroy(Context context) {

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signOut();

        SharedPreferences sharedPreferences = context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }
}
