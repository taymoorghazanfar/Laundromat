package com.laundromat.customer.utils;

import com.google.gson.Gson;
import com.laundromat.customer.model.Laundry;
import com.laundromat.customer.model.Transaction;
import com.laundromat.customer.model.Trip;
import com.laundromat.customer.model.order.Order;
import com.laundromat.customer.model.util.TripStatus;
import com.laundromat.customer.model.util.TripType;

public class GsonUtils {

    public static String orderToGson(Order order) {

        Gson gson = new Gson();
        return gson.toJson(order);
    }

    public static Order gsonToOrder(String orderGson) {

        Gson gson = new Gson();

        return gson.fromJson(orderGson, Order.class);
    }

    public static Trip gsonToTrip(String tripGson) {

        Gson gson = new Gson();

        return gson.fromJson(tripGson, Trip.class);
    }

    public static String tripStatusToGson(TripStatus status) {

        Gson gson = new Gson();
        return gson.toJson(status);
    }

    public static TripStatus gsonToTripStatus(String tripStatusGson) {

        Gson gson = new Gson();

        return gson.fromJson(tripStatusGson, TripStatus.class);
    }

    public static TripType gsonToTripType(String tripTypeGson) {

        Gson gson = new Gson();

        return gson.fromJson(tripTypeGson, TripType.class);
    }

    public static Transaction gsonToTransaction(String transactionGson) {

        Gson gson = new Gson();

        return gson.fromJson(transactionGson, Transaction.class);
    }

    public static String laundryToGson(Laundry laundry) {

        Gson gson = new Gson();
        return gson.toJson(laundry);
    }


    public static Laundry gsonToLaundry(String laundryGson) {

        Gson gson = new Gson();

        return gson.fromJson(laundryGson, Laundry.class);
    }
}
