package com.laundromat.delivery.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.laundromat.delivery.model.Customer;
import com.laundromat.delivery.model.DeliveryBoy;
import com.laundromat.delivery.model.Merchant;
import com.laundromat.delivery.model.Trip;

public class ParseUtils {

    public static DeliveryBoy parseDeliveryBoy(Object data) {

        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, DeliveryBoy.class);
    }

    public static Customer parseCustomer(Object data) {

        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, Customer.class);
    }

    public static Merchant parseMerchant(Object data) {

        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, Merchant.class);
    }

    public static Trip parseTrip(Object data) {

        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, Trip.class);
    }
}
