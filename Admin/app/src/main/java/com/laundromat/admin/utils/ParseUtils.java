package com.laundromat.admin.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.laundromat.admin.model.Admin;
import com.laundromat.admin.model.Customer;
import com.laundromat.admin.model.DeliveryBoy;
import com.laundromat.admin.model.Merchant;
import com.laundromat.admin.model.Trip;
import com.laundromat.admin.model.order.Order;
import com.laundromat.admin.model.util.FareData;
import com.laundromat.admin.model.util.TripStatus;
import com.laundromat.admin.model.washable.ServiceType;

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

        List<ServiceType> serviceTypes = new ArrayList<>();

        if(data == null){

            return serviceTypes;
        }

        @SuppressWarnings("unchecked")
        ArrayList<Object> result = (ArrayList<Object>) data;

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

    public static Admin parseAdmin(Object data) {

        Gson gson = new Gson();
        JsonElement jsonElement;

        jsonElement = gson.toJsonTree(data);

        return gson.fromJson(jsonElement, Admin.class);
    }

    public static List<Merchant> parseMerchants(Object data) {

        List<Merchant> merchants = new ArrayList<>();

        if(data == null){

            return merchants;
        }

        @SuppressWarnings("unchecked")
        ArrayList<Object> result = (ArrayList<Object>) data;

        Gson gson = new Gson();
        JsonElement jsonElement;

        for (Object obj : result) {

            jsonElement = gson.toJsonTree(obj);
            Merchant merchant = gson.fromJson(jsonElement, Merchant.class);
            Log.d("final_merchant", "getNewMerchantRequests: " + merchant.getId());
            merchants.add(merchant);
        }

        return merchants;
    }

    public static List<DeliveryBoy> parseDeliveryBoys(Object data) {

        List<DeliveryBoy> deliveryBoys = new ArrayList<>();

        if(data == null){

            return deliveryBoys;
        }

        ArrayList<Object> result = (ArrayList<Object>) data;

        Gson gson = new Gson();
        JsonElement jsonElement;

        for (Object obj : result) {

            jsonElement = gson.toJsonTree(obj);
            DeliveryBoy deliveryBoy = gson.fromJson(jsonElement, DeliveryBoy.class);
            Log.d("final_delivery_boy", "getNewDeliveryBoyRequests: " + deliveryBoy.getId());
            deliveryBoys.add(deliveryBoy);
        }

        return deliveryBoys;
    }

    public static List<Customer> parseCustomers(Object data) {

        List<Customer> customers = new ArrayList<>();

        if(data == null){

            return customers;
        }

        ArrayList<Object> result = (ArrayList<Object>) data;

        Gson gson = new Gson();
        JsonElement jsonElement;

        for (Object obj : result) {

            jsonElement = gson.toJsonTree(obj);
            Customer customer = gson.fromJson(jsonElement, Customer.class);
            Log.d("final_delivery_boy", "getNewDeliveryBoyRequests: " + customer.getId());
            customers.add(customer);
        }

        return customers;
    }

    public static List<Order> parseOrders(Object data) {

        List<Order> orders = new ArrayList<>();

        if(data == null){

            return orders;
        }

        ArrayList<Object> result = (ArrayList<Object>) data;

        Gson gson = new Gson();
        JsonElement jsonElement;

        for (Object obj : result) {

            jsonElement = gson.toJsonTree(obj);
            Order order = gson.fromJson(jsonElement, Order.class);
            Log.d("final_delivery_boy", "getNewDeliveryBoyRequests: " + order.getId());
            orders.add(order);
        }

        return orders;
    }
}
