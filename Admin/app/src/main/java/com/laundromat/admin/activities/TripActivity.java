package com.laundromat.admin.activities;

import android.Manifest;
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
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.admin.R;
import com.laundromat.admin.helpers.direction_helpers.FetchURL;
import com.laundromat.admin.helpers.direction_helpers.TaskLoadedCallback;
import com.laundromat.admin.model.Customer;
import com.laundromat.admin.model.DeliveryBoy;
import com.laundromat.admin.model.Merchant;
import com.laundromat.admin.model.Trip;
import com.laundromat.admin.model.order.Order;
import com.laundromat.admin.model.util.TripStatus;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.views.ScrollableMapFragment;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.LocationUtils;
import com.laundromat.admin.utils.ParseUtils;

import java.text.MessageFormat;
import java.util.Objects;

public class TripActivity extends AppCompatActivity
        implements View.OnClickListener,
        OnMapReadyCallback, TaskLoadedCallback, SwipeRefreshLayout.OnRefreshListener {

    // Variables
    private Trip trip;
    private Order order;
    private Customer customer;
    private Merchant merchant;
    private DeliveryBoy driver;
    private GoogleMap mMap;
    private MarkerOptions place1, place2;
    private Polyline currentPolyline;

    // Views
    private SwipeRefreshLayout swipeRefreshLayout;
    private NestedScrollView scrollView;
    private ImageButton buttonBack;

    private TextView textViewLaundryName;
    private RelativeLayout layoutLoading;

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
    private AppCompatButton buttonContactCustomer;

    private TextView textViewMerchantName;
    private TextView textViewMerchantPhone;
    private AppCompatButton buttonContactMerchant;

    private CardView cardViewDriver;
    private TextView textViewDriverName;
    private TextView textViewDriverPhone;
    private AppCompatButton buttonContactDriver;

    // footer
    private LottieAnimationView animationView;
    private TextView textViewStatus;
    private TextView textViewMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        getIntentData();

        initViews();

        if (trip.getStatus() != TripStatus.DECLINED) {

            getDriver();

        } else {

            getOrderCustomer();
        }
    }

    private void getIntentData() {

        String tripGson = getIntent().getStringExtra("trip");
        trip = GsonUtils.gsonToTrip(tripGson);

        order = trip.getOrder();
    }

    private void initViews() {

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        scrollView = findViewById(R.id.layout_trip);

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

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

        map.setListener(() -> scrollView.requestDisallowInterceptTouchEvent(true));

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

        cardViewDriver = findViewById(R.id.card_view_driver);

        textViewDriverName = findViewById(R.id.text_view_driver_name);
        textViewDriverPhone = findViewById(R.id.text_view_driver_phone);

        buttonContactCustomer = findViewById(R.id.button_contact_customer);
        buttonContactCustomer.setOnClickListener(this);

        buttonContactMerchant = findViewById(R.id.button_contact_merchant);
        buttonContactMerchant.setOnClickListener(this);

        buttonContactDriver = findViewById(R.id.button_contact_driver);
        buttonContactDriver.setOnClickListener(this);

        animationView = findViewById(R.id.animation_status);
        textViewStatus = findViewById(R.id.text_view_status);
        textViewMessage = findViewById(R.id.text_view_message);
    }

    private void setupViews() {

        textViewLaundryName.setText(order.getLaundryName());

        textViewTripId.setText(trip.getId().substring(trip.getId().length() - 10));
        textViewDateCreated.setText(trip.getDateCreated());
        textViewTripType.setText(trip.getType().toString());
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

        if (trip.getStatus() != TripStatus.DECLINED) {

            textViewDriverName.setText(driver.getFullName());
            textViewDriverPhone.setText(MessageFormat.format("+92{0}",
                    driver.getPhoneNumber()));

        } else {

            cardViewDriver.setVisibility(View.GONE);
        }

        switch (trip.getStatus()) {

            case REQUESTED:
                animationView.setAnimation(R.raw.requested);
                animationView.playAnimation();
                textViewStatus.setText("Requested");
                textViewMessage.setText("This trip has been requested for approval by driver");
                break;

            case ACCEPTED:
                animationView.setAnimation(R.raw.accepted);
                animationView.playAnimation();
                textViewStatus.setText("Accepted");
                textViewMessage.setText("This trip has been accepted by the driver");
                break;

            case STARTED:
                animationView.setAnimation(R.raw.delivering);
                animationView.playAnimation();
                textViewStatus.setText("Started");
                textViewMessage.setText("This trip has been started for pickup");
                break;

            case ARRIVED_SOURCE:
                animationView.setAnimation(R.raw.delivering);
                animationView.playAnimation();
                textViewStatus.setText("Arrived At Source");
                textViewMessage.setText("Driver has arrived at source for pickup");
                break;

            case PICKED_UP:
                animationView.setAnimation(R.raw.delivering);
                animationView.playAnimation();
                textViewStatus.setText("Picked Up");
                textViewMessage.setText("Driver has picked up the order items");
                break;

            case ARRIVED_DESTINATION:
                animationView.setAnimation(R.raw.delivering);
                animationView.playAnimation();
                textViewStatus.setText("Arrived At Destination");
                textViewMessage.setText("Driver has arrived at destination for delivery");
                break;

            case DELIVERED:
                animationView.setAnimation(R.raw.delivered);
                animationView.playAnimation();
                textViewStatus.setText("Delivered");
                textViewMessage.setText("Driver has delivered the order items");
                break;

            case COMPLETED:
                animationView.setAnimation(R.raw.completed);
                animationView.playAnimation();
                textViewStatus.setText("Completed");
                textViewMessage.setText("This trip has been completed");
                break;

            case DECLINED:
                animationView.setAnimation(R.raw.declined);
                animationView.playAnimation();
                textViewStatus.setText("Declined");
                textViewMessage.setText("This trip request has been declined by all drivers");
                break;
        }

        setupMaps();
    }

    private void setupMaps() {
        new FetchURL(TripActivity.this)
                .execute(getUrl(place1.getPosition(), place2.getPosition(),
                        "driving"), "driving");
    }

    private void getDriver() {

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("delivery_boy-getById")
                .call(trip.getDriverId())
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        hideLoadingAnimation();

                        driver = ParseUtils
                                .parseDriver(httpsCallableResult.getData());

                        getOrderCustomer();
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Toast.makeText(TripActivity.this,
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

    private void getTrip() {

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("trip_task-getTripById")
                .call(trip.getId())
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        trip = ParseUtils.parseTrip(httpsCallableResult.getData());
                        order = trip.getOrder();

                        // save locally
                        for (int x = 0; x < Session.user.getDeliveryBoys().size(); x++) {

                            for (int y = 0; y < Session.user.getDeliveryBoys()
                                    .get(x).getTrips().size(); y++) {

                                if (Session.user.getDeliveryBoys().get(x).getTrips().get(y)
                                        .getId().equals(trip.getId())) {

                                    Session.user.getDeliveryBoys().get(x).getTrips().set(y, trip);
                                    break;
                                }
                            }
                        }

                        getDriver();
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();
                    Log.d("customer_fcm", "fcm: " + e.getMessage());
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_back) {

            finish();

        } else if (view.getId() == R.id.button_contact_customer) {

            contactCustomer();

        } else if (view.getId() == R.id.button_contact_merchant) {

            contactMerchant();

        } else if (view.getId() == R.id.button_contact_driver) {

            contactDriver();
        }
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

    private void contactDriver() {

        String phone = "0092" + driver.getPhoneNumber();
        Intent intent = new Intent(Intent.ACTION_DIAL,
                Uri.fromParts("tel", phone, null));
        startActivity(intent);
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
                .title("Pickup");

        place2 = new MarkerOptions().position(trip.getDestination())
                .title("Delivery");

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

    @Override
    public void onRefresh() {

        getTrip();
        swipeRefreshLayout.setRefreshing(false);
    }
}