package com.laundromat.merchant.services;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.laundromat.merchant.activities.ConfirmCollectedActivity;
import com.laundromat.merchant.activities.ConfirmPickupActivity;
import com.laundromat.merchant.activities.OrderActivity;
import com.laundromat.merchant.activities.OrderRequestActivity;
import com.laundromat.merchant.model.Transaction;
import com.laundromat.merchant.model.order.Order;
import com.laundromat.merchant.model.order.OrderStatus;
import com.laundromat.merchant.model.util.TripType;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.utils.GsonUtils;
import com.laundromat.merchant.utils.ParseUtils;

import java.util.HashMap;
import java.util.Map;

public class LaundromatFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        if (Session.userExist(getApplicationContext())) {

            HashMap<String, Object> data = new HashMap<>();
            data.put("phone_number", Session.getPhoneNumber(getApplicationContext()));
            data.put("fcm_token", token);

            FirebaseFunctions
                    .getInstance()
                    .getHttpsCallable("merchant-setFcmToken")
                    .call(data);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
//        String merchantPhone = data.get("merchant_phone");

        // if user is logged in and phone number is distinct
        if (Session.userExist(getApplicationContext())) {

            String task = data.get("task");

            if (task != null) {

                switch (task) {

                    case "ORDER_REQUEST":

                        String customerPhone = data.get("customer_phone");
                        String orderId = data.get("order");

                        FirebaseFunctions
                                .getInstance()
                                .getHttpsCallable("order_task-getOrderById")
                                .call(orderId)
                                .addOnSuccessListener(httpsCallableResult -> {

                                    if (httpsCallableResult.getData() != null) {

                                        Order order = ParseUtils.parseOrder(httpsCallableResult.getData());
                                        order.setStatus(OrderStatus.REQUESTED);

                                        String title = "You have a new order request in " + order.getLaundryName();
                                        String message = "Quantity: " + order.getItemsQuantity() + " items\n" +
                                                "Price: PKR " + order.getPrice() + "\n" +
                                                "Payment Method: " + order.getPaymentMethod() + "\n" +
                                                "Customer Phone: +92" + customerPhone;

                                        Intent intent = new Intent(getApplicationContext(), OrderRequestActivity.class);
                                        intent.putExtra("order", GsonUtils.orderToGson(order));

                                        // send a notification
                                        MerchantNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            // save the order locally
                                            Session.user.getLaundry().getOrders().add(order);

                                            // notify observers
                                            Session.user.notifyObservers(task, order.getId(), OrderStatus.REQUESTED);
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.d("merchant_fcm", "fcm: " + e.getMessage()));
                        break;

                    case "ORDER_CANCEL":

                        orderId = data.get("order");

                        FirebaseFunctions
                                .getInstance()
                                .getHttpsCallable("order_task-getOrderById")
                                .call(orderId)
                                .addOnSuccessListener(httpsCallableResult -> {

                                    if (httpsCallableResult.getData() != null) {

                                        Order order = ParseUtils.parseOrder(httpsCallableResult.getData());
                                        order.setStatus(OrderStatus.CANCELLED);

                                        String title = "Your order in " + order.getLaundryName() + " has been cancelled";
                                        String message = "Order ID: " + order.getId().substring(order.getId().length() - 10) + "\n" +
                                                "Status: Cancelled";

                                        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                        intent.putExtra("order", GsonUtils.orderToGson(order));

                                        // send a notification
                                        MerchantNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            // update the order locally
                                            for (int x = 0; x < Session.user.getLaundry().getOrders().size(); x++) {

                                                if (Session.user
                                                        .getLaundry().getOrders().get(x).getId().equals(order.getId())) {

                                                    Session.user
                                                            .getLaundry().getOrders().get(x).setStatus(OrderStatus.CANCELLED);
                                                    break;
                                                }
                                            }

                                            // notify observers
                                            Session.user.notifyObservers(task, order.getId(), OrderStatus.CANCELLED);
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.d("merchant_fcm", "fcm: " + e.getMessage()));
                        break;

                    case "PICKUP_DECLINED":
                    case "DELIVERY_DECLINED":

                        orderId = data.get("order");

                        FirebaseFunctions
                                .getInstance()
                                .getHttpsCallable("order_task-getOrderById")
                                .call(orderId)
                                .addOnSuccessListener(httpsCallableResult -> {

                                    if (httpsCallableResult.getData() != null) {

                                        Order order = ParseUtils.parseOrder(httpsCallableResult.getData());

                                        OrderStatus status;
                                        String title;
                                        String message;

                                        if (task.equals("PICKUP_DECLINED")) {

                                            status = OrderStatus.ACCEPTED;
                                            title = "No rider was found to pickup your order with ID: "
                                                    + order.getId().substring(order.getId().length() - 10);
                                            message = "No worries, You can request for a pickup again";

                                        } else {

                                            status = OrderStatus.WASHED;
                                            title = "No rider was found to deliver your order with ID: "
                                                    + order.getId().substring(order.getId().length() - 10);
                                            message = "No worries, You can request for a delivery again";
                                        }

                                        order.setStatus(status);
                                        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                        intent.putExtra("order", GsonUtils.orderToGson(order));

                                        // send a notification
                                        MerchantNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            // change the order status back to accepted
                                            for (int x = 0; x < Session.user.getLaundry().getOrders().size(); x++) {

                                                if (Session.user
                                                        .getLaundry().getOrders().get(x).getId().equals(order.getId())) {

                                                    Session.user
                                                            .getLaundry().getOrders().get(x).setStatus(status);
                                                    break;
                                                }
                                            }

                                            // notify observers
                                            Session.user.notifyObservers(task, order.getId(), status);
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.d("merchant_fcm", "fcm: " + e.getMessage()));
                        break;

                    case "PICKUP_ACCEPTED":
                    case "DELIVERY_ACCEPTED":

                        orderId = data.get("order");

                        FirebaseFunctions
                                .getInstance()
                                .getHttpsCallable("order_task-getOrderById")
                                .call(orderId)
                                .addOnSuccessListener(httpsCallableResult -> {

                                    if (httpsCallableResult.getData() != null) {

                                        Order order = ParseUtils.parseOrder(httpsCallableResult.getData());

                                        OrderStatus status;
                                        String title;
                                        String message;

                                        if (task.equals("PICKUP_ACCEPTED")) {

                                            status = OrderStatus.PICKUP_ACCEPTED;
                                            title = "Your order with ID: " + order.getId().substring(order.getId().length() - 10) + " has been" +
                                                    " accepted for pickup";
                                        } else {

                                            status = OrderStatus.DELIVERY_ACCEPTED;
                                            title = "Your order with ID: " + order.getId().substring(order.getId().length() - 10) + " has been" +
                                                    " accepted for delivery";
                                        }

                                        message = "You will be notified once the rider starts the trip";

                                        order.setStatus(status);
                                        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                        intent.putExtra("order", GsonUtils.orderToGson(order));

                                        // send a notification
                                        MerchantNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            // change the order status
                                            for (int x = 0; x < Session.user.getLaundry().getOrders().size(); x++) {

                                                if (Session.user
                                                        .getLaundry().getOrders().get(x).getId().equals(order.getId())) {

                                                    Session.user
                                                            .getLaundry().getOrders().get(x)
                                                            .setStatus(status);
                                                    break;
                                                }
                                            }

                                            // notify observers
                                            Session.user.notifyObservers(task, order.getId(), status);
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.d("merchant_fcm", "fcm: " + e.getMessage()));
                        break;

                    case "TRIP_CANCEL":

                        orderId = data.get("order");

                        FirebaseFunctions
                                .getInstance()
                                .getHttpsCallable("order_task-getOrderById")
                                .call(orderId)
                                .addOnSuccessListener(httpsCallableResult -> {

                                    if (httpsCallableResult.getData() != null) {

                                        Order order = ParseUtils.parseOrder(httpsCallableResult.getData());

                                        OrderStatus status = order.getStatus();
                                        String title;
                                        String message;

                                        title = "Your scheduled collection for order was cancelled";
                                        message = "Your order with ID: " + order.getId().substring(order.getId().length() - 10) + " has been cancelled for the scheduled collection by the driver\n" +
                                                "We apologise for any inconvenience.";

                                        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                        intent.putExtra("order", GsonUtils.orderToGson(order));

                                        // send a notification
                                        MerchantNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            // change the order status to pickup
                                            for (int x = 0; x < Session.user.getLaundry()
                                                    .getOrders().size(); x++) {

                                                if (Session.user
                                                        .getLaundry().getOrders().get(x).getId()
                                                        .equals(order.getId())) {

                                                    Session.user
                                                            .getLaundry().getOrders().get(x)
                                                            .setStatus(status);
                                                    break;
                                                }
                                            }

                                            // notify observers
                                            Session.user.notifyObservers(task, order.getId(), status);
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.d("customer_fcm", "fcm: " + e.getMessage()));
                        break;

                    case "PICKUP_STARTED":
                    case "DELIVERY_STARTED":

                        orderId = data.get("order");

                        FirebaseFunctions
                                .getInstance()
                                .getHttpsCallable("order_task-getOrderById")
                                .call(orderId)
                                .addOnSuccessListener(httpsCallableResult -> {

                                    if (httpsCallableResult.getData() != null) {

                                        Order order = ParseUtils.parseOrder(httpsCallableResult.getData());

                                        OrderStatus status;
                                        String title;
                                        String message;

                                        if (task.equals("PICKUP_STARTED")) {

                                            status = OrderStatus.PICK_UP;
                                            title = "Rider is arriving to pickup order items";
                                            message = "Order with ID: " + order.getId().substring(order.getId().length() - 10) + " will be picked up soon\n" +
                                                    "You will be notified once the rider picks up the items";
                                        } else {

                                            status = OrderStatus.DELIVERING;
                                            title = "Get ready! Rider is arriving to pickup your items";
                                            message = "Your order with ID: " + order.getId().substring(order.getId().length() - 10) + " will be picked up soon\n" +
                                                    "You will be notified once the rider arrives at your location";
                                        }

                                        order.setStatus(status);
                                        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                        intent.putExtra("order", GsonUtils.orderToGson(order));

                                        // send a notification
                                        MerchantNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            // change the order status to pickup
                                            for (int x = 0; x < Session.user.getLaundry().getOrders().size(); x++) {

                                                if (Session.user
                                                        .getLaundry().getOrders().get(x).getId().equals(order.getId())) {

                                                    Session.user
                                                            .getLaundry().getOrders().get(x)
                                                            .setStatus(status);
                                                    break;
                                                }
                                            }

                                            // notify observers
                                            Session.user.notifyObservers(task, order.getId(), status);
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.d("merchant_fcm", "fcm: " + e.getMessage()));
                        break;

                    // when rider arrives to collect delivery items from merchant
                    case "ARRIVED_SOURCE":
                        orderId = data.get("order");

                        FirebaseFunctions
                                .getInstance()
                                .getHttpsCallable("order_task-getOrderById")
                                .call(orderId)
                                .addOnSuccessListener(httpsCallableResult -> {

                                    if (httpsCallableResult.getData() != null) {

                                        Order order = ParseUtils.parseOrder(httpsCallableResult.getData());

                                        String tripTypeJson = data.get("trip_type");
                                        TripType tripType = GsonUtils.gsonToTripType(tripTypeJson);

                                        String title;
                                        String message;

                                        if (tripType == TripType.DELIVERY) {

                                            title = "Rider has arrived for pickup";
                                            message = "Please meet the rider at the specified location to complete the pickup";

                                            Intent intent = new Intent(getApplicationContext(), ConfirmPickupActivity.class);
                                            intent.putExtra("order", GsonUtils.orderToGson(order));

                                            // send a notification
                                            MerchantNotificationManager
                                                    .getInstance(getApplicationContext())
                                                    .showNotification(getApplicationContext(), title, message,
                                                            intent
                                                    );

                                            if (Session.user != null) {

                                                // change the order status to pickup
                                                for (int x = 0; x < Session.user.getLaundry().getOrders().size(); x++) {

                                                    if (Session.user
                                                            .getLaundry()
                                                            .getOrders().get(x).getId().equals(order.getId())) {

                                                        Session.user
                                                                .getLaundry()
                                                                .getOrders().get(x)
                                                                .setStatus(OrderStatus.DELIVERING);
                                                        break;
                                                    }
                                                }

                                                // notify observers
                                                Session.user.notifyObservers(task, order.getId(), OrderStatus.DELIVERING);
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.d("merchant_fcm", "fcm: " + e.getMessage()));
                        break;

                    case "PICKED_UP":

                        orderId = data.get("order");

                        FirebaseFunctions
                                .getInstance()
                                .getHttpsCallable("order_task-getOrderById")
                                .call(orderId)
                                .addOnSuccessListener(httpsCallableResult -> {

                                    if (httpsCallableResult.getData() != null) {

                                        Order order = ParseUtils.parseOrder(httpsCallableResult.getData());

                                        String tripTypeJson = data.get("trip_type");
                                        TripType tripType = GsonUtils.gsonToTripType(tripTypeJson);

                                        String title;
                                        String message;

                                        String transactionJson = data.get("transaction");
                                        Transaction transaction = GsonUtils.gsonToTransaction(transactionJson);

                                        // when rider has started to arrive toward laundry for delivery
                                        if (tripType == TripType.PICKUP) {

                                            // order payment earning transaction

                                            title = "Get Ready! Your order with ID: " + order.getId().substring(order.getId().length() - 10) + " has been picked up";
                                            message = "Rider will arrive at your location soon to deliver the orders";

                                            order.setStatus(OrderStatus.PICK_UP);
                                            Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                            intent.putExtra("order", GsonUtils.orderToGson(order));

                                            // send a notification
                                            MerchantNotificationManager
                                                    .getInstance(getApplicationContext())
                                                    .showNotification(getApplicationContext(), title, message,
                                                            intent
                                                    );

                                            if (Session.user != null) {

                                                // save the transactions locally
                                                Session.user.getTransactions().add(transaction);

                                                // notify observers
                                                Session.user.notifyObservers(task, order.getId(), OrderStatus.PICK_UP);
                                            }
                                        }
                                        // when rider has started to arrive toward customer for delivery
                                        else {

                                            title = "Your order with ID: " + order.getId().substring(order.getId().length() - 10) + " has been picked up";
                                            message = "You will be notified once the order has been delivered to the customer";

                                            order.setStatus(OrderStatus.DELIVERING);
                                            Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                            intent.putExtra("order", GsonUtils.orderToGson(order));

                                            // send a notification
                                            MerchantNotificationManager
                                                    .getInstance(getApplicationContext())
                                                    .showNotification(getApplicationContext(), title, message,
                                                            intent
                                                    );

                                            if (Session.user != null) {

                                                // save the transactions locally
                                                Session.user.getTransactions().add(transaction);

                                                // notify observers
                                                Session.user.notifyObservers(task, order.getId(), OrderStatus.DELIVERING);
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.d("merchant_fcm", "fcm: " + e.getMessage()));
                        break;

                    case "ARRIVED_DESTINATION":

                        orderId = data.get("order");

                        FirebaseFunctions
                                .getInstance()
                                .getHttpsCallable("order_task-getOrderById")
                                .call(orderId)
                                .addOnSuccessListener(httpsCallableResult -> {

                                    if (httpsCallableResult.getData() != null) {

                                        Order order = ParseUtils.parseOrder(httpsCallableResult.getData());

                                        String tripTypeJson = data.get("trip_type");
                                        TripType tripType = GsonUtils.gsonToTripType(tripTypeJson);

                                        String title;
                                        String message;

                                        if (tripType == TripType.PICKUP) {

                                            title = "Rider has arrived to deliver";
                                            message = "Please meet the rider at the specified location to complete the delivery";

                                            Intent intent = new Intent(getApplicationContext(), ConfirmCollectedActivity.class);
                                            intent.putExtra("order", GsonUtils.orderToGson(order));

                                            // send a notification
                                            MerchantNotificationManager
                                                    .getInstance(getApplicationContext())
                                                    .showNotification(getApplicationContext(), title, message,
                                                            intent
                                                    );

                                            if (Session.user != null) {

                                                // notify observers
                                                Session.user.notifyObservers(task, order.getId(), OrderStatus.PICK_UP);
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.d("merchant_fcm", "fcm: " + e.getMessage()));
                        break;

                    case "ORDER_COLLECTED":

                        orderId = data.get("order");

                        FirebaseFunctions
                                .getInstance()
                                .getHttpsCallable("order_task-getOrderById")
                                .call(orderId)
                                .addOnSuccessListener(httpsCallableResult -> {

                                    if (httpsCallableResult.getData() != null) {

                                        Order order = ParseUtils.parseOrder(httpsCallableResult.getData());

                                        String title = "You have collected the order items";
                                        String message = "Order ID: " + order.getId().substring(order.getId().length() - 10) + "\n" +
                                                "Quantity: " + order.getItemsQuantity() + " Items\n" +
                                                "Price: PKR " + order.getPrice();

                                        order.setStatus(OrderStatus.COLLECTED);
                                        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                        intent.putExtra("order", GsonUtils.orderToGson(order));

                                        // send a notification
                                        MerchantNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            for (int x = 0; x < Session.user.getLaundry().getOrders().size(); x++) {

                                                if (Session.user
                                                        .getLaundry().getOrders().get(x).getId().equals(order.getId())) {

                                                    Session.user
                                                            .getLaundry().getOrders().get(x)
                                                            .setStatus(OrderStatus.COLLECTED);
                                                    break;
                                                }
                                            }

                                            // notify observers
                                            Session.user.notifyObservers(task, order.getId(), order.getStatus());
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.d("merchant_fcm", "fcm: " + e.getMessage()));
                        break;

                    case "ORDER_COMPLETED":

                        orderId = data.get("order");

                        FirebaseFunctions
                                .getInstance()
                                .getHttpsCallable("order_task-getOrderById")
                                .call(orderId)
                                .addOnSuccessListener(httpsCallableResult -> {

                                    if (httpsCallableResult.getData() != null) {

                                        Order order = ParseUtils.parseOrder(httpsCallableResult.getData());

                                        String title = "Your order has been completed";
                                        String message = "Order ID: " + order.getId().substring(order.getId().length() - 10) + "\n" +
                                                "Quantity: " + order.getItemsQuantity() + " Items\n" +
                                                "Price: PKR " + order.getPrice();

                                        order.setStatus(OrderStatus.COMPLETED);
                                        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                        intent.putExtra("order", GsonUtils.orderToGson(order));

                                        // send a notification
                                        MerchantNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            for (int x = 0; x < Session.user.getLaundry().getOrders().size(); x++) {

                                                if (Session.user.getLaundry()
                                                        .getOrders().get(x).getId().equals(order.getId())) {

                                                    Session.user.getLaundry()
                                                            .getOrders().get(x)
                                                            .setStatus(OrderStatus.COMPLETED);
                                                    break;
                                                }
                                            }

                                            // notify observers
                                            Session.user.notifyObservers(task, order.getId(), OrderStatus.COMPLETED);
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.d("merchant_fcm", "fcm: " + e.getMessage()));
                        break;
                }
            }
        }
    }

    @Override
    public void onMessageSent(@NonNull String s) {
        super.onMessageSent(s);
    }
}
