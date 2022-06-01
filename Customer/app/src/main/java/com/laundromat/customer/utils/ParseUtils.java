package com.laundromat.customer.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.laundromat.customer.model.Customer;
import com.laundromat.customer.model.DeliveryBoy;
import com.laundromat.customer.model.Laundry;
import com.laundromat.customer.model.Merchant;
import com.laundromat.customer.model.Trip;
import com.laundromat.customer.model.order.Order;
import com.laundromat.customer.model.util.FareData;
import com.laundromat.customer.model.util.LiveLocationData;
import com.laundromat.customer.model.util.TripStatus;
import com.laundromat.customer.model.washable.ServiceType;

import java.util.ArrayList;
import java.util.List;

public class ParseUtils {

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

    public static DeliveryBoy parseDriver(Object data) {

        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, DeliveryBoy.class);
    }

    public static Laundry parseLaundry(Object data) {

        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, Laundry.class);
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

    public static List<Laundry> parseLaundries(Object data) {

        @SuppressWarnings("unchecked")
        ArrayList<Object> result = (ArrayList<Object>) data;

        List<Laundry> laundries = new ArrayList<>();

        Gson gson = new Gson();
        JsonElement jsonElement;

        for (Object obj : result) {

            jsonElement = gson.toJsonTree(obj);
            Laundry laundry = gson.fromJson(jsonElement, Laundry.class);
            laundries.add(laundry);
        }

        return laundries;
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

    public static Order parseOrder(Object data) {

        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, Order.class);
    }

    public static LiveLocationData parseLiveLocation(Object data) {

        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, LiveLocationData.class);
    }

    public static FareData parseFareData(Object data) {
        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, FareData.class);
    }
}
