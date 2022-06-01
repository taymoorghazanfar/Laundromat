package com.laundromat.delivery.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.delivery.R;
import com.laundromat.delivery.helpers.direction_helpers.FetchURL;
import com.laundromat.delivery.helpers.direction_helpers.TaskLoadedCallback;
import com.laundromat.delivery.model.Customer;
import com.laundromat.delivery.model.DeliveryBoy;
import com.laundromat.delivery.model.Merchant;
import com.laundromat.delivery.model.Trip;
import com.laundromat.delivery.model.observers.ITripObserver;
import com.laundromat.delivery.model.order.Order;
import com.laundromat.delivery.model.util.TripStatus;
import com.laundromat.delivery.prefs.Session;
import com.laundromat.delivery.ui.views.ScrollableMapFragment;
import com.laundromat.delivery.utils.GsonUtils;
import com.laundromat.delivery.utils.LocationUtils;
import com.laundromat.delivery.utils.ParseUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class TripRequestActivity extends AppCompatActivity
        implements View.OnClickListener, ITripObserver,
        OnMapReadyCallback, TaskLoadedCallback {

    // Variables
    private DeliveryBoy driver = Session.user;
    private Trip trip;
    private Order order;
    private Customer customer;
    private Merchant merchant;
    private GoogleMap mMap;
    private MarkerOptions place1, place2;
    private Polyline currentPolyline;

    // Views
    private ScrollView scrollView;
    private ImageButton buttonBack;
    private TextView textViewLaundryName;
    private RelativeLayout layoutLoading;
    private RelativeLayout layoutMissed;
    private RelativeLayout layoutDecline;
    private TextView textViewTripId;
    private TextView textViewDateCreated;
    private TextView textViewTripType;
    private TextView textViewTripDistance;
    private TextView textViewTripFare;
    private TextView textViewTripPaymentMethod;
    private ScrollableMapFragment map;
    private TextView textViewPickupAddress;
    private TextView textViewDeliveryAddress;

    private TextView textViewOrderId;
    private TextView textViewItemQuantity;
    private TextView textViewOrderPrice;
    private TextView textViewOrderPaymentMethod;

    private TextView textViewCustomerName;
    private TextView textViewCustomerPhone;
    private TextView textViewMerchantName;
    private TextView textViewMerchantPhone;

    private AppCompatButton buttonContactCustomer;
    private AppCompatButton buttonContactMerchant;

    private AppCompatButton buttonAccept;
    private AppCompatButton buttonDecline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_request);

        Session.user.registerObserver(this);

        getIntentData();

        initViews();

        if (driver == null) {

            getDriver();

        } else {

            if (checkTripRequestExist()) {

                if (checkTripRequestHandled()) {

                    Intent intent = new Intent(TripRequestActivity.this,
                            TripActivity.class);
                    intent.putExtra("trip", GsonUtils.tripToGson(trip));

                    startActivity(intent);
                    finish();

                } else {

                    getOrderCustomer();
                }

            } else {

                showMissedLayout();
            }
        }
    }

    private void getIntentData() {

        String tripGson = getIntent().getStringExtra("trip");
        trip = GsonUtils.gsonToTrip(tripGson);

        order = trip.getOrder();

    }

    private void initViews() {

        scrollView = findViewById(R.id.layout_trip_request);

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        layoutMissed = findViewById(R.id.layout_missed);
        layoutMissed.setVisibility(View.GONE);

        layoutDecline = findViewById(R.id.layout_request_declined);
        layoutDecline.setVisibility(View.GONE);

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        textViewLaundryName = findViewById(R.id.text_view_laundry_name);
        textViewTripId = findViewById(R.id.text_view_trip_id);
        textViewDateCreated = findViewById(R.id.text_view_date_created);
        textViewTripType = findViewById(R.id.text_view_type);
        textViewTripDistance = findViewById(R.id.text_view_distance);
        textViewTripFare = findViewById(R.id.text_view_fare);
        textViewTripPaymentMethod = findViewById(R.id.text_view_payment_method);

        map = ((ScrollableMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map));

        map.setListener(new ScrollableMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {

                scrollView.requestDisallowInterceptTouchEvent(true);
            }
        });

        Objects.requireNonNull(map).getMapAsync(this);

        textViewPickupAddress = findViewById(R.id.text_view_pickup_address);
        textViewDeliveryAddress = findViewById(R.id.text_view_delivery_address);

        textViewOrderId = findViewById(R.id.text_view_order_id);
        textViewItemQuantity = findViewById(R.id.text_view_item_quantity);
        textViewOrderPrice = findViewById(R.id.text_view_price);
        textViewOrderPaymentMethod = findViewById(R.id.text_view_order_payment_method);

        textViewCustomerName = findViewById(R.id.text_view_customer_name);
        textViewCustomerPhone = findViewById(R.id.text_view_customer_phone);

        textViewMerchantName = findViewById(R.id.text_view_merchant_name);
        textViewMerchantPhone = findViewById(R.id.text_view_merchant_phone);

        buttonContactCustomer = findViewById(R.id.button_contact_customer);
        buttonContactCustomer.setOnClickListener(this);

        buttonContactMerchant = findViewById(R.id.button_contact_merchant);
        buttonContactMerchant.setOnClickListener(this);

        buttonAccept = findViewById(R.id.button_accept);
        buttonAccept.setOnClickListener(this);

        buttonDecline = findViewById(R.id.button_decline);
        buttonDecline.setOnClickListener(this);
    }

    private void setupViews() {

        textViewLaundryName.setText(order.getLaundryName());

        textViewTripId.setText(trip.getId().substring(trip.getId().length() - 10));
        textViewDateCreated.setText(trip.getDateCreated());
        textViewTripType.setText(trip.getType().toString().replace("_", " "));
        textViewTripDistance.setText(MessageFormat.format("{0} KM", trip.getDistance()));
        textViewTripFare.setText(MessageFormat.format("PKR {0}", trip.getCost()));
        textViewTripPaymentMethod.setText(order.getPaymentMethod().toString().replace("_", " "));

        String pickupAddress = LocationUtils.getAddressFromLatLng(this,
                trip.getSource().latitude, trip.getSource().longitude);
        textViewPickupAddress.setText(pickupAddress);

        String deliveryAddress = LocationUtils.getAddressFromLatLng(this,
                trip.getDestination().latitude, trip.getDestination().longitude);
        textViewDeliveryAddress.setText(deliveryAddress);

        textViewOrderId.setText(order.getId().substring(order.getId().length() - 10));
        textViewItemQuantity.setText(String.valueOf(order.getItemsQuantity()));
        textViewOrderPrice.setText(MessageFormat.format("PKR {0}", order.getPrice()));
        textViewOrderPaymentMethod.setText(order.getPaymentMethod().toString().replace("_", " "));

        textViewCustomerName.setText(customer.getFullName());
        textViewCustomerPhone.setText(MessageFormat.format("+92{0}",
                customer.getPhoneNumber()));

        textViewMerchantName.setText(merchant.getFullName());
        textViewMerchantPhone.setText(MessageFormat.format("+92{0}",
                merchant.getPhoneNumber()));

        setupMaps();
    }

    private void setupMaps() {
        new FetchURL(TripRequestActivity.this)
                .execute(getUrl(place1.getPosition(), place2.getPosition(),
                        "driving"), "driving");
    }

    // checking if this trip has been already accepted by other driver first
    // if return true, this means trip is still in the trips list
    private boolean checkTripRequestExist() {

        for (Trip searchTrip : driver.getTrips()) {

            if (searchTrip.getId().equals(trip.getId())) {

                return true;
            }
        }

        return false;
    }

    // checking if this trip has been already accepted by the driver
    private boolean checkTripRequestHandled() {

        for (Trip searchTrip : driver.getTrips()) {

            if (searchTrip.getId().equals(trip.getId())) {

                if (searchTrip.getStatus() != TripStatus.REQUESTED) {

                    return true;
                }
            }
        }

        return false;
    }

    private void getDriver() {

        showLoadingAnimation();

        // get logged in user by phone and password from server
        Map<String, Object> data = new HashMap<>();
        data.put("phone_number", Session.getPhoneNumber(TripRequestActivity.this));
        data.put("password", Session.getPassword(TripRequestActivity.this));

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("delivery_boy-verifyLogin")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        hideLoadingAnimation();

                        // saving new logged in user to session
                        Session.user = ParseUtils
                                .parseDeliveryBoy(httpsCallableResult.getData());

                        driver = Session.user;

                        if (checkTripRequestHandled()) {

                            Intent intent = new Intent(TripRequestActivity.this,
                                    TripActivity.class);
                            intent.putExtra("trip", GsonUtils.tripToGson(trip));

                            startActivity(intent);
                            finish();

                        } else {

                            getOrderCustomer();
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Toast.makeText(TripRequestActivity.this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void getOrderCustomer() {

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("customer-getCustomerById")
                .call(order.getCustomerId())
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        customer = ParseUtils.parseCustomer(httpsCallableResult.getData());

                        getOrderMerchant();
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Log.d("checkout", "placeOrder: " + e.getMessage());

                });
    }

    private void getOrderMerchant() {

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("merchant-getMerchantByLaundryId")
                .call(order.getLaundryId())
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        hideLoadingAnimation();

                        merchant = ParseUtils.parseMerchant(httpsCallableResult.getData());

                        setupViews();
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Log.d("checkout", "placeOrder: " + e.getMessage());

                });
    }

    private void declineTripRequest() {

        AlertDialog.Builder alert;
        alert = new AlertDialog.Builder(this);
        alert.setTitle("Decline trip request ?");

        alert.setPositiveButton("Yes", (dialog, whichButton) -> {

            dialog.dismiss();

            // set query data
            Map<String, Object> data = new HashMap<>();
            data.put("trip_id", trip.getId());
            data.put("driver_id", driver.getId());
            data.put("customer_id", customer.getId());
            data.put("merchant_id", merchant.getId());
            data.put("order", trip.getOrder().toJson());

            showLoadingAnimation();

            FirebaseFunctions
                    .getInstance()
                    .getHttpsCallable("trip_task-declineTripRequest")
                    .call(data)
                    .addOnSuccessListener(httpsCallableResult -> {

                        hideLoadingAnimation();

                        showDeclineLayout();

                        int index = 0;
                        for (int x = 0; x < Session.user.getTrips().size(); x++) {

                            if (Session.user.getTrips().get(x).getId().equals(trip.getId())) {

                                index = x;
                                break;
                            }
                        }

                        // remove trip locally
                        Session.user.getTrips().remove(index);

                        // notify observers
                        Session.user.notifyObservers("TRIP_DECLINED",
                                trip.getId(), TripStatus.DECLINED);

                    })
                    .addOnFailureListener(e -> {

                        hideLoadingAnimation();

                        Toast.makeText(TripRequestActivity.this,
                                e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
        });

        alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

        alert.show();
    }

    private void acceptTripRequest() {

        AlertDialog.Builder alert;
        alert = new AlertDialog.Builder(this);
        alert.setTitle("Accept trip request ?");

        alert.setPositiveButton("Yes", (dialog, whichButton) -> {

            dialog.dismiss();

            showLoadingAnimation();

            //get delivery radius
            FirebaseFunctions
                    .getInstance()
                    .getHttpsCallable("admin-getDeliveryRadius")
                    .call()
                    .addOnSuccessListener(httpsCallableResult -> {

                        if (httpsCallableResult.getData() != null) {

                            double deliveryRadius = Double.parseDouble(httpsCallableResult.getData().toString());

                            // check if driver is within the delivery radius
                            double distance = LocationUtils
                                    .getDistanceBetweenTwoPoints(
                                            Session.user.getCurrentLocation(),
                                            trip.getSource());

                            // if driver is within delivery radius
                            if (distance <= deliveryRadius) {

                                // change trip status locally
                                for (int x = 0; x < Session.user.getTrips().size(); x++) {

                                    if (Session.user.getTrips().get(x).getId().equals(trip.getId())) {

                                        Session.user.getTrips().get(x).setStatus(TripStatus.ACCEPTED);
                                        break;
                                    }
                                }

                                // set query data
                                Map<String, Object> data = new HashMap<>();
                                data.put("trip_id", trip.getId());
                                data.put("driver_id", driver.getId());
                                data.put("customer_id", customer.getId());
                                data.put("merchant_id", merchant.getId());
                                data.put("order", trip.getOrder().toJson());

                                // change order status to accepted and notify customer, then update view
                                FirebaseFunctions
                                        .getInstance()
                                        .getHttpsCallable("trip_task-acceptTripRequest")
                                        .call(data)
                                        .addOnSuccessListener(httpsCallableResult2 -> {

                                            hideLoadingAnimation();

                                            // notify observers
                                            Session.user.notifyObservers("PICKUP_ACCEPTED",
                                                    trip.getId(), TripStatus.ACCEPTED);
                                        })
                                        .addOnFailureListener(e -> {

                                            hideLoadingAnimation();

                                            Toast.makeText(TripRequestActivity.this,
                                                    e.getMessage(), Toast.LENGTH_SHORT).show();

                                            Log.d("trip", "acceptTripRequest: " + e.getMessage());
                                        });
                            }

                            // if driver is outside the delivery radius
                            else {

                                hideLoadingAnimation();

                                androidx.appcompat.app.AlertDialog.Builder alert2;
                                alert2 = new androidx.appcompat.app.AlertDialog.Builder(this);
                                alert2.setTitle("You are out of range to accept the trip.\n" +
                                        " Try again later");
                                alert2.setCancelable(false);

                                alert2.setPositiveButton("OK", (dialog2, whichButton2) -> {

                                    dialog2.dismiss();
                                });

                                alert2.show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {

                        hideLoadingAnimation();

                        Toast.makeText(TripRequestActivity.this,
                                e.getMessage(), Toast.LENGTH_SHORT).show();

                        Log.d("trip", "acceptTripRequest: " + e.getMessage());
                    });
        });

        alert.setNegativeButton("No",
                (dialogInterface, i) -> dialogInterface.dismiss());

        alert.show();
    }

    private void contactCustomer() {

        String phone = "0092" + customer.getPhoneNumber();
        Intent intent = new Intent(Intent.ACTION_DIAL,
                Uri.fromParts("tel", phone, null));
        startActivity(intent);
    }

    private void contactMerchant() {

        String phone = "0092" + merchant.getPhoneNumber();
        Intent intent = new Intent(Intent.ACTION_DIAL,
                Uri.fromParts("tel", phone, null));
        startActivity(intent);
    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_back) {

            goBack();

        } else if (view.getId() == R.id.button_contact_customer) {

            contactCustomer();

        } else if (view.getId() == R.id.button_contact_merchant) {

            contactMerchant();

        } else if (view.getId() == R.id.button_accept) {

            acceptTripRequest();

        } else if (view.getId() == R.id.button_decline) {

            declineTripRequest();
        }
    }

    private void goBack() {

        // if there are no activities in the background
        if (isTaskRoot()) {

            startActivity(new Intent(TripRequestActivity.this, DashboardActivity.class));
        }

        finish();
    }

    @Override
    public void onBackPressed() {
        goBack();
    }


    private void showLoadingAnimation() {

        layoutLoading.setVisibility(View.VISIBLE);
        layoutLoading.bringToFront();
        layoutLoading.animate().translationY(0);
    }

    private void showDeclineLayout() {

        layoutDecline.setVisibility(View.VISIBLE);
        layoutDecline.bringToFront();
        layoutDecline.animate().translationY(0);
    }

    private void showMissedLayout() {

        layoutMissed.setVisibility(View.VISIBLE);
        layoutMissed.bringToFront();
        layoutMissed.animate().translationY(0);
    }

    private void hideLoadingAnimation() {

        layoutLoading.setVisibility(View.GONE);
        layoutLoading.animate().translationY(layoutLoading.getHeight());
    }

    @Override
    public void updateView(String task, String tripId, TripStatus status) {

        if (tripId.equals(trip.getId())) {

            if (task.equals("TRIP_DELETE")) {

                runOnUiThread(this::showMissedLayout);

            } else if (task.equals("PICKUP_ACCEPTED")) {

                trip.setStatus(TripStatus.ACCEPTED);

                runOnUiThread(() -> {

                    Intent intent = new Intent(TripRequestActivity.this, TripActivity.class);
                    intent.putExtra("trip", GsonUtils.tripToGson(trip));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);
                });
            }
        }
    }

    //////////////////////////////////////////////////////
    // MAP
    //////////////////////////////////////////////////////

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

        place1 = new MarkerOptions().position(trip.getSource())
                .title("Pickup").icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED));

        place2 = new MarkerOptions().position(trip.getDestination())
                .title("Delivery").icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        mMap.addMarker(place1);
        mMap.addMarker(place2);
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
        recenterMap();
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
}