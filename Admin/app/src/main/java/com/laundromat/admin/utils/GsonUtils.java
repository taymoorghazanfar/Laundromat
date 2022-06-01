package com.laundromat.admin.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.laundromat.admin.model.Customer;
import com.laundromat.admin.model.DeliveryBoy;
import com.laundromat.admin.model.Laundry;
import com.laundromat.admin.model.Merchant;
import com.laundromat.admin.model.Transaction;
import com.laundromat.admin.model.Trip;
import com.laundromat.admin.model.Vehicle;
import com.laundromat.admin.model.order.Order;
import com.laundromat.admin.model.util.TripStatus;
import com.laundromat.admin.model.util.TripType;
import com.laundromat.admin.model.washable.ServiceType;

import java.util.List;

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

    public static String laundryToGson(Laundry laundry) {

        Gson gson = new Gson();
        return gson.toJson(laundry);
    }

    public static Laundry gsonToLaundry(String laundryGson) {

        Gson gson = new Gson();

        return gson.fromJson(laundryGson, Laundry.class);
    }

    public static String driverToGson(DeliveryBoy driver) {

        Gson gson = new Gson();
        return gson.toJson(driver);
    }

    public static DeliveryBoy gsonToDriver(String driverGson) {

        Gson gson = new Gson();
        return gson.fromJson(driverGson, DeliveryBoy.class);
    }

    public static String vehicleToGson(Vehicle vehicle) {

        Gson gson = new Gson();
        return gson.toJson(vehicle);
    }

    public static Vehicle gsonToVehicle(String vehicleGson) {

        Gson gson = new Gson();
        return gson.fromJson(vehicleGson, Vehicle.class);
    }

    public static String transactionsToGson(List<Transaction> transactions) {

        Gson gson = new Gson();
        return gson.toJson(transactions);
    }

    public static List<Transaction> gsonToTransactions(String transactionsGson) {

        Gson gson = new Gson();
        return gson.fromJson(transactionsGson,
                new TypeToken<List<Transaction>>() {
                }.getType());
    }

    public static String customerToGson(Customer customer) {

        Gson gson = new Gson();
        return gson.toJson(customer);
    }

    public static Customer gsonToCustomer(String customerGson) {

        Gson gson = new Gson();
        return gson.fromJson(customerGson, Customer.class);
    }

    public static String tripToGson(Trip trip) {

        Gson gson = new Gson();
        return gson.toJson(trip);
    }

    public static String serviceToGson(ServiceType serviceType) {

        Gson gson = new Gson();
        return gson.toJson(serviceType);
    }

    public static ServiceType gsonToService(String serviceGson) {

        Gson gson = new Gson();
        return gson.fromJson(serviceGson, ServiceType.class);
    }
}
