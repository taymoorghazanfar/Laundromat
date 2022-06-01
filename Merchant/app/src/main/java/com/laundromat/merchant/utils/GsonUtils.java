package com.laundromat.merchant.utils;

import com.google.gson.Gson;
import com.laundromat.merchant.model.Merchant;
import com.laundromat.merchant.model.Transaction;
import com.laundromat.merchant.model.Trip;
import com.laundromat.merchant.model.order.Order;
import com.laundromat.merchant.model.util.TripStatus;
import com.laundromat.merchant.model.util.TripType;

public class GsonUtils {

    public static String orderToGson(Order order) {

        Gson gson = new Gson();
        return gson.toJson(order);
    }

    public static String tripStatusToGson(TripStatus status) {

        Gson gson = new Gson();
        return gson.toJson(status);
    }

    public static Order gsonToOrder(String orderGson) {

        Gson gson = new Gson();

        return gson.fromJson(orderGson, Order.class);
    }

    public static TripStatus gsonToTripStatus(String tripStatusGson) {

        Gson gson = new Gson();

        return gson.fromJson(tripStatusGson, TripStatus.class);
    }

    public static Trip gsonToTrip(String tripGson) {

        Gson gson = new Gson();

        return gson.fromJson(tripGson, Trip.class);
    }

    public static TripType gsonToTripType(String tripTypeGson) {

        Gson gson = new Gson();

        return gson.fromJson(tripTypeGson, TripType.class);
    }

    public static Transaction gsonToTransaction(String transactionGson) {

        Gson gson = new Gson();

        return gson.fromJson(transactionGson, Transaction.class);
    }

    public static String merchantToGson(Merchant merchant) {

        Gson gson = new Gson();
        return gson.toJson(merchant);
    }

    public static Merchant gsonToMerchant(String merchantGson) {

        Gson gson = new Gson();

        return gson.fromJson(merchantGson, Merchant.class);
    }
}
