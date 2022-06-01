package com.laundromat.merchant.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.laundromat.merchant.model.Customer;
import com.laundromat.merchant.model.DeliveryBoy;
import com.laundromat.merchant.model.Merchant;
import com.laundromat.merchant.model.Trip;
import com.laundromat.merchant.model.order.Order;
import com.laundromat.merchant.model.util.FareData;
import com.laundromat.merchant.model.util.LiveLocationData;
import com.laundromat.merchant.model.util.TripStatus;
import com.laundromat.merchant.model.washable.ServiceType;

import java.util.ArrayList;
import java.util.List;

public class ParseUtils {

    public static Merchant parseMerchant(Object data) {

        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, Merchant.class);
    }

    public static Customer parseCustomer(Object data) {

        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, Customer.class);
    }

    public static DeliveryBoy parseDriver(Object data) {

        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, DeliveryBoy.class);
    }

    public static List<ServiceType> parseServiceTypes(Object data) {

        @SuppressWarnings("unchecked")
        ArrayList<Object> result = (ArrayList<Object>) data;
        List<ServiceType> serviceTypes = new ArrayList<>();

        Gson gson = new Gson();
        JsonElement jsonElement;

        for (Object obj : result) {

            jsonElement = gson.toJsonTree(obj);
            ServiceType serviceType = gson.fromJson(jsonElement, ServiceType.class);
            serviceTypes.add(serviceType);
        }

        return serviceTypes;
    }

    public static Order parseOrder(Object data) {

        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, Order.class);
    }

    public static FareData parseFareData(Object data) {
        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, FareData.class);
    }

    public static TripStatus parseTripStatus(Object data) {

        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, TripStatus.class);
    }

    public static Trip parseTrip(Object data) {

        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, Trip.class);
    }

    public static LiveLocationData parseLiveLocation(Object data) {

        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, LiveLocationData.class);
    }
}
