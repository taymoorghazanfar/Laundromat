package com.laundromat.delivery.services;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.laundromat.delivery.activities.TripRequestActivity;
import com.laundromat.delivery.model.Trip;
import com.laundromat.delivery.model.order.Order;
import com.laundromat.delivery.model.util.TripStatus;
import com.laundromat.delivery.prefs.Session;
import com.laundromat.delivery.utils.GsonUtils;
import com.laundromat.delivery.utils.ParseUtils;

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
                    .getHttpsCallable("delivery_boy-setFcmToken")
                    .call(data);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
//        String deliveryBoyPhone = data.get("delivery_boy_phone");

        // if user is logged in
        if (Session.userExist(getApplicationContext())) {

            String task = data.get("task");

            if (task != null) {

                switch (task) {

                    case "PICKUP_REQUEST":
                    case "DELIVERY_REQUEST":

                        String tripId = data.get("trip");

                        // check if this trip is already handled
                        boolean exists = false;

                        if (Session.user != null) {

                            for (int x = 0; x < Session.user.getTrips().size(); x++) {

                                if (Session.user.getTrips().get(x).getId().equals(tripId)) {

                                    exists = true;
                                    break;
                                }
                            }
                        }

                        if (exists) {

                            return;
                        }

                        FirebaseFunctions
                                .getInstance()
                                .getHttpsCallable("trip_task-getTripById")
                                .call(tripId)
                                .addOnSuccessListener(httpsCallableResult -> {

                                    if (httpsCallableResult.getData() != null) {

                                        Trip trip = ParseUtils.parseTrip(httpsCallableResult.getData());
                                        trip.setStatus(TripStatus.REQUESTED);

                                        Order order = trip.getOrder();

                                        String title = "You have a new trip request from " + order.getLaundryName();
                                        String message = "Distance: " + trip.getDistance() + " KM\n" +
                                                "Fare: PKR " + trip.getCost() + "\n" +
                                                "Payment Method: " + order.getPaymentMethod() + "\n" +
                                                "Status: REQUESTED";

                                        Intent intent = new Intent(getApplicationContext(), TripRequestActivity.class);
                                        intent.putExtra("trip", GsonUtils.tripToGson(trip));

                                        // send a notification
                                        DeliveryBoyNotificationManager
                                                .getInstance(getApplicationContext())
                                                .showNotification(getApplicationContext(), title, message,
                                                        intent
                                                );

                                        if (Session.user != null) {

                                            // save the trip locally
                                            Session.user.getTrips().add(trip);

                                            // notify observers
                                            Session.user.notifyObservers(task, trip.getId(), trip.getStatus());
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.d("customer_fcm", "fcm: " + e.getMessage()));
                        break;

                    case "TRIP_DELETE":

                        tripId = data.get("trip_id");

                        if (Session.user != null) {

                            // delete the trip locally
                            int index = 0;
                            for (int x = 0; x < Session.user.getTrips().size(); x++) {

                                if (Session.user.getTrips().get(x).getId().equals(tripId)) {

                                    index = x;
                                    break;
                                }
                            }

                            // check if this trip was accepted by this driver
                            if (Session.user.getTrips().get(index)
                                    .getStatus() == TripStatus.ACCEPTED) {

                                return;
                            }

                            Session.user.getTrips().remove(index);

                            // notify observers
                            Session.user.notifyObservers(task, tripId, TripStatus.DECLINED);
                        }
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
