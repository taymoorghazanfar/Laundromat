package com.laundromat.merchant.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.merchant.R;
import com.laundromat.merchant.helpers.direction_helpers.FetchURL;
import com.laundromat.merchant.helpers.direction_helpers.TaskLoadedCallback;
import com.laundromat.merchant.model.Merchant;
import com.laundromat.merchant.model.Trip;
import com.laundromat.merchant.model.order.Order;
import com.laundromat.merchant.model.order.OrderStatus;
import com.laundromat.merchant.model.util.FareData;
import com.laundromat.merchant.model.util.TripStatus;
import com.laundromat.merchant.model.util.TripType;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.views.ScrollableMapFragment;
import com.laundromat.merchant.utils.GsonUtils;
import com.laundromat.merchant.utils.LocationUtils;
import com.laundromat.merchant.utils.ParseUtils;
import com.laundromat.merchant.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PickupOrderActivity extends AppCompatActivity
        implements View.OnClickListener, OnMapReadyCallback, TaskLoadedCallback {

    // Variables
    private Order order;
    private Merchant merchant = Session.user;
    private FareData fareData;

    // Views
    private ScrollView scrollView;
    private ImageButton buttonBack;
    private TextView textViewRequestType;
    private TextView textViewLaundryName;

    private RelativeLayout layoutLoading;

    private TextView textViewOrderId;
    private TextView textViewDateCreated;
    private TextView textViewQuantity;
    private TextView textViewPrice;
    private TextView textViewDiscount;
    private TextView textViewPayable;
    private TextView textViewPaymentMethod;

    private TextView textViewAddressType;
    private ImageView imageViewLocation;
    private TextView textViewLocationDistance;
    private TextView textViewLocationAddress;

    private ScrollableMapFragment map;
    private TextView textViewCost;
    private TextView textViewBaseFare;
    private TextView textViewPerKm;

    private TextView textViewPaymentMessage;

    private TextView textViewSendRequest;
    private LinearLayout buttonSendPickupRequest;

    // Map
    private GoogleMap mMap;
    private MarkerOptions place1, place2;
    private Polyline currentPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_order);

        getIntentData();

        initViews();

        getFareData();
    }

    private void getIntentData() {

        String orderGson = getIntent().getStringExtra("order");
        order = GsonUtils.gsonToOrder(orderGson);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        scrollView = findViewById(R.id.layout_order_pickup);

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);
        textViewRequestType = findViewById(R.id.text_view_request_type);
        textViewLaundryName = findViewById(R.id.text_view_laundry_name);

        textViewOrderId = findViewById(R.id.text_view_order_id);
        textViewDateCreated = findViewById(R.id.text_view_date_created);
        textViewQuantity = findViewById(R.id.text_view_item_quantity);
        textViewPrice = findViewById(R.id.text_view_price);
        textViewDiscount = findViewById(R.id.text_view_discount);
        textViewPayable = findViewById(R.id.text_view_payable);
        textViewPaymentMethod = findViewById(R.id.text_view_payment_method);

        textViewAddressType = findViewById(R.id.text_view_address_type);
        imageViewLocation = findViewById(R.id.image_view_location);
        textViewLocationDistance = findViewById(R.id.text_view_location_distance);
        textViewLocationAddress = findViewById(R.id.text_view_location_address);

        map = ((ScrollableMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map));

        map.setListener(() -> scrollView.requestDisallowInterceptTouchEvent(true));

        Objects.requireNonNull(map).getMapAsync(this);

        textViewCost = findViewById(R.id.text_view_cost);
        textViewBaseFare = findViewById(R.id.text_view_base_fare);
        textViewPerKm = findViewById(R.id.text_view_per_km);

        textViewPaymentMessage = findViewById(R.id.text_view_payment_message);
        textViewSendRequest = findViewById(R.id.text_view_send_request);

        buttonSendPickupRequest = findViewById(R.id.button_send_pickup_request);
        buttonSendPickupRequest.setOnClickListener(this);
    }

    private void setupViews() {

        textViewRequestType.setText(order.getStatus() == OrderStatus.ACCEPTED ?
                "Pickup Request" : "Delivery Request");
        textViewLaundryName.setText(merchant.getLaundry().getName());

        textViewOrderId.setText(order.getId().substring(order.getId().length() - 10));
        textViewDateCreated.setText(order.getDateCreated());
        textViewQuantity.setText(String.valueOf(order.getItemsQuantity()));
        textViewPrice.setText(MessageFormat.format("PKR {0}", (order.getPrice() + order.getDiscount())));
        textViewDiscount.setText(MessageFormat.format("PKR {0}", order.getDiscount()));
        textViewPayable.setText(MessageFormat.format("PKR {0}", order.getPrice()));
        textViewPaymentMethod.setText(order.getPaymentMethod().toString());

        textViewAddressType.setText(order.getStatus()
                == OrderStatus.ACCEPTED ? "Pickup Address" : "Delivery Address");

        String locationUrl = StringUtils.getMapsStaticImageUrl(this,
                order.getDeliveryLocation());

        Picasso.get()
                .load(locationUrl)
                .into(imageViewLocation);

        double locationDistance = fareData.getDistance();
        String distance = locationDistance + " KM";
        textViewLocationDistance.setText(distance);

        String locationAddress = LocationUtils.getAddressFromLatLng(this,
                order.getDeliveryLocation().latitude,
                order.getDeliveryLocation().longitude);
        textViewLocationAddress.setText(locationAddress);

        textViewCost.setText(MessageFormat.format("PKR {0}", getRideCost()));
        textViewBaseFare.setText(MessageFormat.format("PKR {0}", fareData.getBaseFare()));
        textViewPerKm.setText(MessageFormat.format("PKR {0}", fareData.getPerKm()));

        textViewPaymentMessage.setText(order.getStatus() == OrderStatus.ACCEPTED
                ? "Note: Pickup fee will be payed by the customer"
                : "Note: Delivery fee will be payed by you");

        textViewSendRequest.setText(order.getStatus() == OrderStatus.ACCEPTED
                ? "Send Pickup Request" : "Send Delivery Request");

        setupMap();
    }

    private double getRideCost() {

        return fareData.getBaseFare() + (fareData.getDistance() * fareData.getPerKm());
    }

    private void getFareData() {

        showLoadingAnimation();

        Map<String, Object> data = new HashMap<>();
        data.put("customer_latitude", order.getDeliveryLocation().latitude);
        data.put("customer_longitude", order.getDeliveryLocation().longitude);
        data.put("laundry_latitude", merchant.getLaundry().getLocation().latitude);
        data.put("laundry_longitude", merchant.getLaundry().getLocation().longitude);

        //todo: get distance, base fare, per km
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("admin-getFareData")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        hideLoadingAnimation();

                        fareData = ParseUtils
                                .parseFareData(httpsCallableResult.getData());

                        Log.d("fare", "getFareData: base fare: " + fareData.getBaseFare());
                        Log.d("fare", "getFareData: per km: " + fareData.getPerKm());
                        Log.d("fare", "getFareData: distance: " + fareData.getDistance());

                        setupViews();
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Log.d("checkout", "placeOrder: " + e.getMessage());

                });
    }

    public void onClick(View view) {

        if (view.getId() == R.id.button_back) {

            Intent intent = new Intent(PickupOrderActivity.this,
                    OrderActivity.class);
            intent.putExtra("order", GsonUtils.orderToGson(order));
            startActivity(intent);
            finish();

        } else if (view.getId() == R.id.button_send_pickup_request) {

            androidx.appcompat.app.AlertDialog.Builder alert;
            alert = new androidx.appcompat.app.AlertDialog.Builder(this);
            String title = order.getStatus()
                    == OrderStatus.ACCEPTED ? "Send pickup request now ?" : "Send delivery request now ?";
            alert.setTitle(title);

            alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                dialog.dismiss();
                sendPickupRequest();
            });

            alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

            alert.show();
        }
    }

    private void sendPickupRequest() {

        // create trip object
        Trip trip = new Trip();

        trip.setType(order.getStatus() ==
                OrderStatus.ACCEPTED ? TripType.PICKUP : TripType.DELIVERY);

        trip.setStatus(TripStatus.REQUESTED);

        trip.setSource(order.getStatus() ==
                OrderStatus.ACCEPTED ? order.getDeliveryLocation()
                : Session.user.getLaundry().getLocation());

        trip.setDestination(order.getStatus() ==
                OrderStatus.ACCEPTED ? Session.user.getLaundry().getLocation()
                : order.getDeliveryLocation());

        trip.setOrder(order);
        trip.setCost(getRideCost());
        trip.setDistance(fareData.getDistance());

        Map<String, Object> data = new HashMap<>();
        data.put("trip", trip.toJson());

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("order_task-sendPickupRequest")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        hideLoadingAnimation();

                        boolean requestSent = (boolean) httpsCallableResult.getData();

                        if (requestSent) {

                            if (order.getStatus() == OrderStatus.ACCEPTED) {

                                order.setStatus(OrderStatus.PICKUP_REQUESTED);

                            } else if (order.getStatus() == OrderStatus.WASHED) {

                                order.setStatus(OrderStatus.DELIVERY_REQUESTED);
                            }

                            // change order status locally
                            for (int x = 0; x < Session.user.getLaundry().getOrders().size(); x++) {

                                if (Session.user
                                        .getLaundry().getOrders().get(x)
                                        .getId().equals(order.getId())) {

                                    Session.user
                                            .getLaundry().getOrders()
                                            .get(x).setStatus(order.getStatus());
                                    break;
                                }
                            }

                            // notify observers
                            Session.user.notifyObservers("RIDER_REQUEST",
                                    order.getId(), order.getStatus());

//                            // show dialog
//                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                            LayoutInflater layoutInflater = getLayoutInflater();
//
//                            final View dialogView = layoutInflater
//                                    .inflate(R.layout.dialog_pickup_request_sent, null);
//                            builder.setView(dialogView);
//                            final AlertDialog dialog = builder.create();
//
//                            Button buttonOk = dialogView.findViewById(R.id.button_ok);
//                            buttonOk.setOnClickListener(view -> {
//
//                                dialog.dismiss();
//
//                                Intent intent = new Intent(PickupOrderActivity.this,
//                                        OrderActivity.class);
//                                intent.putExtra("order", GsonUtils.orderToGson(order));
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//                                startActivity(intent);
//                            });
//
//                            dialog.show();
                            Intent intent = new Intent(PickupOrderActivity.this,
                                    OrderActivity.class);
                            intent.putExtra("order", GsonUtils.orderToGson(order));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            startActivity(intent);

                        } else {

                            // if request was failed to sent
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            LayoutInflater layoutInflater = getLayoutInflater();

                            final View dialogView = layoutInflater
                                    .inflate(R.layout.dialog_pickup_request_failed, null);
                            builder.setView(dialogView);
                            final AlertDialog dialog = builder.create();

                            Button buttonOk = dialogView.findViewById(R.id.button_ok);
                            buttonOk.setOnClickListener(view -> {

                                dialog.dismiss();

                            });

                            dialog.show();
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();
                    Log.d("pickup", "sendPickupRequest: " + e.getMessage());

                    // if request was failed to sent
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    LayoutInflater layoutInflater = getLayoutInflater();

                    final View dialogView = layoutInflater
                            .inflate(R.layout.dialog_pickup_request_failed, null);
                    builder.setView(dialogView);
                    final AlertDialog dialog = builder.create();

                    Button buttonOk = dialogView.findViewById(R.id.button_ok);
                    buttonOk.setOnClickListener(view -> {

                        dialog.dismiss();

                    });

                    dialog.show();
                });
    }

    private void showLoadingAnimation() {

        layoutLoading.setVisibility(View.VISIBLE);
        layoutLoading.bringToFront();
        layoutLoading.animate().translationY(0);
    }

    private void hideLoadingAnimation() {

        layoutLoading.setVisibility(View.GONE);
        layoutLoading.animate().translationY(layoutLoading.getHeight());
    }

    ////////////////////////////////////////////////////////////////////////
    // MAPS
    ////////////////////////////////////////////////////////////////////////

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMyLocationButtonClickListener(() -> {
            recenterMap();
            return true;
        });

        place1 = new MarkerOptions().position(order.getDeliveryLocation())
                .title("Pickup").icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED));

        place2 = new MarkerOptions().position(merchant.getLaundry().getLocation())
                .title("Drop off").icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        mMap.addMarker(place1);
        mMap.addMarker(place2);
    }

    private void setupMap() {

        new FetchURL(PickupOrderActivity.this)
                .execute(getUrl(place1.getPosition(), place2.getPosition(),
                        "driving"), "driving");
    }

    private void recenterMap() {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(place1.getPosition());
        builder.include(place2.getPosition());
        LatLngBounds bounds = builder.build();

        int padding = 100; // padding around start and end marker
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?"
                + parameters + "&key=" + getString(R.string.google_maps_api_key);

        return url;
    }

    @Override
    public void onTaskDone(Object... values) {

        if (currentPolyline != null) {

            currentPolyline.remove();
        }
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(place1.getPosition());
        builder.include(place2.getPosition());
        LatLngBounds bounds = builder.build();

        int padding = 100; // padding around start and end marker
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }
}