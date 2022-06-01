package com.laundromat.customer.services;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.laundromat.customer.activities.ConfirmCollectedActivity;
import com.laundromat.customer.activities.ConfirmPickupActivity;
import com.laundromat.customer.activities.OrderActivity;
import com.laundromat.customer.model.Transaction;
import com.laundromat.customer.model.Trip;
import com.laundromat.customer.model.order.Order;
import com.laundromat.customer.model.order.OrderStatus;
import com.laundromat.customer.model.util.TripType;
import com.laundromat.customer.prefs.Session;
import com.laundromat.customer.utils.GsonUtils;
import com.laundromat.customer.utils.ParseUtils;

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
                    .getHttpsCallable("customer-setFcmToken")
                    .call(data);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // if user is logged in
        if (Session.userExist(getApplicationContext())) {

            Map<String, String> data = remoteMessage.getData();

            String task = data.get("task");

            if (task != null) {

                switch (task) {

                    case "ORDER_DECLINE":
                    case "ORDER_ACCEPT":

                        String orderId = data.get("order");

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

                                        if (task.equals("ORDER_ACCEPT")) {

                                            status = OrderStatus.ACCEPTED;
                                            title = "Your order in " + order.getLaundryName() + " has been accepted";
                                            message = "Order ID: " + order.getId().substring(order.getId().length() - 10) + "\n" +
                                                    "Status: ACCEPTED";

                                        } else {

                                            status = OrderStatus.DECLINED;
                                            title = "Your order in " + order.getLaundryName() + " has been declined";
                                            message = "Order ID: " + order.getId().substring(order.getId().length() - 10) + "\n" +
                                                    "Status: DECLINED";
                                        }

                                        order.setStatus(status);
                                        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                        intent.putExtra("order", GsonUtils.orderToGson(order));

                                        CustomerNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title,
                                                        message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            // update order locally
                                            for (int x = 0; x < Session.user.getOrders().size(); x++) {

                                                if (Session.user.getOrders().get(x).getId().equals(order.getId())) {

                                                    Session.user.getOrders().get(x).setStatus(status);
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
                                        CustomerNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            // update the order locally
                                            for (int x = 0; x < Session.user.getOrders().size(); x++) {

                                                if (Session.user
                                                        .getOrders().get(x).getId().equals(order.getId())) {

                                                    Session.user
                                                            .getOrders().get(x).setStatus(OrderStatus.CANCELLED);
                                                    break;
                                                }
                                            }

                                            // notify observers
                                            Session.user.notifyObservers(task, order.getId(), OrderStatus.CANCELLED);
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.d("customer_fcm", "fcm: " + e.getMessage()));
                        break;

                    case "PICKUP_REQUEST":
                    case "DELIVERY_REQUEST":

                        String tripId = data.get("trip");

                        FirebaseFunctions
                                .getInstance()
                                .getHttpsCallable("trip_task-getTripById")
                                .call(tripId)
                                .addOnSuccessListener(httpsCallableResult -> {

                                    if (httpsCallableResult.getData() != null) {

                                        Trip trip = ParseUtils.parseTrip(httpsCallableResult.getData());

                                        Order order = trip.getOrder();

                                        OrderStatus status;
                                        String title;
                                        String message;

                                        if (task.equals("PICKUP_REQUEST")) {

                                            status = OrderStatus.PICKUP_REQUESTED;
                                            title = "Your order in " + order.getLaundryName() + " has been requested for pickup";
                                            message = "Order ID: " + order.getId().substring(order.getId().length() - 10) + "\n" +
                                                    "Status: PICKUP REQUESTED";

                                        } else {

                                            status = OrderStatus.DELIVERY_REQUESTED;
                                            title = "Your order in " + order.getLaundryName() + " has been requested for delivery";
                                            message = "Order ID: " + order.getId().substring(order.getId().length() - 10) + "\n" +
                                                    "Status: DELIVERY REQUESTED";
                                        }

                                        order.setStatus(status);
                                        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                        intent.putExtra("order", GsonUtils.orderToGson(order));

                                        // send a notification
                                        CustomerNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            // update the order locally
                                            for (int x = 0; x < Session.user.getOrders().size(); x++) {

                                                if (Session.user
                                                        .getOrders().get(x).getId().equals(order.getId())) {

                                                    Session.user
                                                            .getOrders().get(x).setStatus(status);
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
                                            title = "No rider was found to pickup your order with ID: " + order.getId();
                                            message = "Laundry will schedule your items pickup at another time";

                                        } else {

                                            status = OrderStatus.WASHED;
                                            title = "No rider was found to deliver your order with ID: " + order.getId();
                                            message = "Laundry will schedule your items delivery at another time";
                                        }

                                        order.setStatus(status);
                                        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                        intent.putExtra("order", GsonUtils.orderToGson(order));

                                        // send a notification
                                        CustomerNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            // change the order status back to accepted
                                            for (int x = 0; x < Session.user.getOrders().size(); x++) {

                                                if (Session.user
                                                        .getOrders().get(x).getId().equals(order.getId())) {

                                                    Session.user
                                                            .getOrders().get(x).setStatus(status);
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
                                            title = "Your order with ID: " + order.getId().substring(order.getId().length() - 10) + " has been accepted for pickup";

                                        } else {

                                            status = OrderStatus.DELIVERY_ACCEPTED;
                                            title = "Your order with ID: " + order.getId().substring(order.getId().length() - 10) + " has been accepted for delivery";
                                        }

                                        message = "You will be notified once the rider starts the trip";

                                        order.setStatus(status);
                                        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                        intent.putExtra("order", GsonUtils.orderToGson(order));

                                        // send a notification
                                        CustomerNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            // change the order status
                                            for (int x = 0; x < Session.user.getOrders().size(); x++) {

                                                if (Session.user
                                                        .getOrders().get(x).getId().equals(order.getId())) {

                                                    Session.user
                                                            .getOrders().get(x)
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
                                        CustomerNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            // change the order status to pickup
                                            for (int x = 0; x < Session.user.getOrders().size(); x++) {

                                                if (Session.user
                                                        .getOrders().get(x).getId().equals(order.getId())) {

                                                    Session.user
                                                            .getOrders().get(x)
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
                                            title = "Get ready! Rider is arriving to pickup your items";
                                            message = "Your order with ID: " + order.getId().substring(order.getId().length() - 10) + " will be picked up soon\n" +
                                                    "You will be notified once the rider arrives at your location";

                                        } else {

                                            status = OrderStatus.DELIVERING;
                                            title = "Rider has started to deliver your items";
                                            message = "Your order with ID: " + order.getId().substring(order.getId().length() - 10) + " will be delivered soon\n" +
                                                    "You will be notified once the rider arrives at your location";
                                        }

                                        order.setStatus(status);
                                        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                        intent.putExtra("order", GsonUtils.orderToGson(order));

                                        // send a notification
                                        CustomerNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            // change the order status to pickup
                                            for (int x = 0; x < Session.user.getOrders().size(); x++) {

                                                if (Session.user
                                                        .getOrders().get(x).getId().equals(order.getId())) {

                                                    Session.user
                                                            .getOrders().get(x)
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

                    // when rider arrives to pickup items from customer
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

                                        if (tripType == TripType.PICKUP) {

                                            title = "Rider has arrived for pickup";
                                            message = "Please meet the rider at the specified location to complete the pickup";

                                            Intent intent = new Intent(getApplicationContext(), ConfirmPickupActivity.class);
                                            intent.putExtra("order", GsonUtils.orderToGson(order));

                                            // send a notification
                                            CustomerNotificationManager
                                                    .getInstance(getApplicationContext())
                                                    .showNotification(getApplicationContext(), title, message,
                                                            intent
                                                    );

                                            if (Session.user != null) {

                                                // change the order status to pickup
                                                for (int x = 0; x < Session.user.getOrders().size(); x++) {

                                                    if (Session.user
                                                            .getOrders().get(x).getId().equals(order.getId())) {

                                                        Session.user
                                                                .getOrders().get(x)
                                                                .setStatus(OrderStatus.PICK_UP);
                                                        break;
                                                    }
                                                }

                                                // notify observers
                                                Session.user.notifyObservers(task, order.getId(), OrderStatus.PICK_UP);

                                                // notify observers
                                                Session.user.notifyObservers(task, order.getId(), OrderStatus.PICK_UP);
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.d("customer_fcm", "fcm: " + e.getMessage()));
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

                                        // if rider has picked up items from customer
                                        if (tripType == TripType.PICKUP) {

                                            // order fee transaction
                                            String transactionJson1 = data.get("transaction1");
                                            Transaction transaction1 = GsonUtils.gsonToTransaction(transactionJson1);

                                            // pickup fee transaction
                                            String transactionJson2 = data.get("transaction2");
                                            Transaction transaction2 = GsonUtils.gsonToTransaction(transactionJson2);

                                            title = "Your order with ID: " + order.getId().substring(order.getId().length() - 10) + " has been picked up";
                                            message = "You will be notified once the order has been collected by the laundry";

                                            order.setStatus(OrderStatus.PICK_UP);
                                            Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                            intent.putExtra("order", GsonUtils.orderToGson(order));

                                            // send a notification
                                            CustomerNotificationManager
                                                    .getInstance(getApplicationContext())
                                                    .showNotification(getApplicationContext(), title, message,
                                                            intent
                                                    );

                                            if (Session.user != null) {

                                                // save the transactions locally
                                                Session.user.getTransactions().add(transaction1);
                                                Session.user.getTransactions().add(transaction2);

                                                // change the order status to pickup
                                                for (int x = 0; x < Session.user.getOrders().size(); x++) {

                                                    if (Session.user
                                                            .getOrders().get(x).getId().equals(order.getId())) {

                                                        Session.user
                                                                .getOrders().get(x)
                                                                .setStatus(OrderStatus.PICK_UP);
                                                        break;
                                                    }
                                                }

                                                // notify observers
                                                Session.user.notifyObservers(task, order.getId(), OrderStatus.PICK_UP);
                                            }
                                        }
                                        // when rider is arriving toward customer for delivery
                                        else {

                                            title = "Get Ready! Your order with ID: " + order.getId().substring(order.getId().length() - 10) + " has been picked up";
                                            message = "Rider will arrive at your location soon to deliver the orders";

                                            order.setStatus(OrderStatus.DELIVERING);
                                            Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                            intent.putExtra("order", GsonUtils.orderToGson(order));

                                            // send a notification
                                            CustomerNotificationManager
                                                    .getInstance(getApplicationContext())
                                                    .showNotification(getApplicationContext(), title, message,
                                                            intent
                                                    );

                                            if (Session.user != null) {

                                                // notify observers
                                                Session.user.notifyObservers(task, order.getId(), OrderStatus.DELIVERING);
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.d("customer_fcm", "fcm: " + e.getMessage()));
                        break;

                    // when rider has arrived to deliver washed items
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

                                        if (tripType == TripType.DELIVERY) {

                                            title = "Rider has arrived to deliver";
                                            message = "Please meet the rider at the specified location to complete the delivery";

                                            order.setStatus(OrderStatus.DELIVERING);
                                            Intent intent = new Intent(getApplicationContext(), ConfirmCollectedActivity.class);
                                            intent.putExtra("order", GsonUtils.orderToGson(order));

                                            // send a notification
                                            CustomerNotificationManager
                                                    .getInstance(getApplicationContext())
                                                    .showNotification(getApplicationContext(), title, message,
                                                            intent
                                                    );

                                            if (Session.user != null) {

                                                // notify observers
                                                Session.user.notifyObservers(task, order.getId(), OrderStatus.DELIVERING);
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.d("customer_fcm", "fcm: " + e.getMessage()));
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

                                        String title = "Your order items have been collected";
                                        String message = "Order ID: " + order.getId().substring(order.getId().length() - 10) + "\n" +
                                                "Quantity: " + order.getItemsQuantity() + " Items\n" +
                                                "Price: PKR " + order.getPrice();

                                        order.setStatus(OrderStatus.COLLECTED);
                                        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                        intent.putExtra("order", GsonUtils.orderToGson(order));

                                        // send a notification
                                        CustomerNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            for (int x = 0; x < Session.user.getOrders().size(); x++) {

                                                if (Session.user
                                                        .getOrders().get(x).getId().equals(order.getId())) {

                                                    Session.user
                                                            .getOrders().get(x)
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
                                        Log.d("customer_fcm", "fcm: " + e.getMessage()));
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
                                        CustomerNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            for (int x = 0; x < Session.user.getOrders().size(); x++) {

                                                if (Session.user
                                                        .getOrders().get(x).getId().equals(order.getId())) {

                                                    Session.user
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
                                        Log.d("customer_fcm", "fcm: " + e.getMessage()));
                        break;

                    case "STATUS_CHANGED":

                        orderId = data.get("order");

                        FirebaseFunctions
                                .getInstance()
                                .getHttpsCallable("order_task-getOrderById")
                                .call(orderId)
                                .addOnSuccessListener(httpsCallableResult -> {

                                    if (httpsCallableResult.getData() != null) {

                                        Order order = ParseUtils.parseOrder(httpsCallableResult.getData());

                                        String title = "";

                                        if (order.getStatus() == OrderStatus.IN_SERVICE) {

                                            title = "Your order items are now being washed";

                                        } else if (order.getStatus() == OrderStatus.WASHED) {

                                            title = "Your order items have been washed";
                                        }

                                        String message = "Order ID: " + order.getId().substring(order.getId().length() - 10) + "\n" +
                                                "Quantity: " + order.getItemsQuantity() + " Items\n" +
                                                "Price: PKR " + order.getPrice();

                                        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                        intent.putExtra("order", GsonUtils.orderToGson(order));

                                        // send a notification
                                        CustomerNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            for (int x = 0; x < Session.user.getOrders().size(); x++) {

                                                if (Session.user
                                                        .getOrders().get(x).getId().equals(order.getId())) {

                                                    Session.user
                                                            .getOrders().get(x)
                                                            .setStatus(order.getStatus());
                                                    break;
                                                }
                                            }

                                            // notify observers
                                            Session.user.notifyObservers(task, order.getId(), order.getStatus());
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.d("customer_fcm", "fcm: " + e.getMessage()));
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
