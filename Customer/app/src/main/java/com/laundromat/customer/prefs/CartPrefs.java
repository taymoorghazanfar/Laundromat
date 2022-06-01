package com.laundromat.customer.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.laundromat.customer.model.util.Cart;

public class CartPrefs {

    private static final String PREFS_NAME = "cart";
    private static final String SAVED_CART = "saved_cart";

    public static void set(Context context, Cart cart) {

        Gson gson = new Gson();
        String cartString = gson.toJson(cart);

        SharedPreferences sharedPreferences = context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SAVED_CART, cartString);
        editor.apply();
    }

    public static Cart get(Context context) {

        SharedPreferences sharedPreferences = context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String cartGson = sharedPreferences.getString(SAVED_CART, null);

        if (cartGson == null) {

            return null;
        }

        return gson.fromJson(cartGson, Cart.class);
    }

    public static void delete(Context context) {

        SharedPreferences sharedPreferences = context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }
}
