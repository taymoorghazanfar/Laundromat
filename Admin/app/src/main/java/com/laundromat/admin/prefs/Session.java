package com.laundromat.admin.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.laundromat.admin.model.Admin;

public class Session {

    private static final String PREFS_NAME = "user_credentials";
    private static final String SAVED_USER_NAME = "saved_username";
    private static final String SAVED_PASSWORD = "saved_password";

    public static Admin user;

    public static void setUsername(Context context, String username) {

        SharedPreferences sharedPreferences = context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SAVED_USER_NAME, username);
        editor.apply();
    }

    public static String getUsername(Context context) {

        SharedPreferences sharedPreferences = context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        return sharedPreferences.getString(SAVED_USER_NAME, null);
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

        return getUsername(context) != null && getPassword(context) != null;
    }

    public static void destroy(Context context) {

        SharedPreferences sharedPreferences = context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }
}
