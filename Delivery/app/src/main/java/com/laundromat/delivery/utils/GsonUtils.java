package com.laundromat.delivery.utils;

import com.google.gson.Gson;
import com.laundromat.delivery.model.Transaction;
import com.laundromat.delivery.model.Trip;
import com.laundromat.delivery.model.order.Order;

public class GsonUtils {

    public static String orderToGson(Order order) {

        Gson gson = new Gson();
        return gson.toJson(order);
    }

    public static Order gsonToOrder(String orderGson) {

        Gson gson = new Gson();

        return gson.fromJson(orderGson, Order.class);
    }

    public static String tripToGson(Trip trip) {

        Gson gson = new Gson();
        return gson.toJson(trip);
    }

    public static Trip gsonToTrip(String tripGson) {

        Gson gson = new Gson();

        return gson.fromJson(tripGson, Trip.class);
    }

    public static Transaction gsonToTransaction(String transactionGson) {

        Gson gson = new Gson();

        return gson.fromJson(transactionGson, Transaction.class);
    }
}
