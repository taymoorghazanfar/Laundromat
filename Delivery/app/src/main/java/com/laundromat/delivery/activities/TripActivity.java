package com.laundromat.delivery.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
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
import com.laundromat.delivery.utils.Globals;
import com.laundromat.delivery.utils.GsonUtils;
import com.laundromat.delivery.utils.LocationUtils;
import com.laundromat.delivery.utils.PackageUtils;
import com.laundromat.delivery.utils.ParseUtils;
import com.laundromat.delivery.utils.StringUtils;
import com.laundromat.delivery.utils.TimeUtils;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TripActivity extends AppCompatActivity
        implements View.OnClickListener, ITripObserver,
        OnMapReadyCallback, TaskLoadedCallback {

    // Constants
    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;

    // Time handler
    Handler timeHandler = new Handler();
    Runnable timeRunnable;
    int timeDelay = 1000;

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
    private CardView cardViewStatus;
    private CardView cardViewCancelled;
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
    private TextView textViewMerchantName;
    private TextView textViewMerchantPhone;
    private AppCompatButton buttonContactCustomer;
    private AppCompatButton buttonContactMerchant;
    private LinearLayout buttonStartTrip;
    private AppCompatButton buttonCancelTrip;

    // footers
    private RelativeLayout layoutStartTrip;
    private RelativeLayout layoutOnGoingTrip;
    private LinearLayout layoutStatus;
    private TextView textViewTripStatus;
    private TextView textViewTimeElapsed;

    private LinearLayout buttonGetDirections;
    private LinearLayout buttonConfirmStatus;
    private TextView textViewGetDirections;
    private TextView textViewConfirmStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        Globals.tripInView = true;

        getIntentData();

        initViews();

        if (driver == null) {

            getDriver();

        } else {

            Session.user.registerObserver(this);

            getOrderCustomer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Globals.tripInView = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (driver != null && trip != null && trip.getStatus() != TripStatus.ACCEPTED) {

            timeHandler.postDelayed(timeRunnable = new Runnable() {
                public void run() {

                    if (trip != null &&
                            trip.getStatus() != TripStatus.ACCEPTED &&
                            trip.getStatus() != TripStatus.COMPLETED
                            && textViewTimeElapsed != null) {

                        getTimeElapsed();

                        timeHandler.postDelayed(timeRunnable, timeDelay);
                    }
                }
            }, timeDelay);
        }
    }

    @Override
    protected void onPause() {

        timeHandler.removeCallbacks(timeRunnable);
        super.onPause();
    }

    private void getTimeElapsed() {

        String timeStarted = trip.getDateStarted();
        String currentTime = StringUtils.getCurrentDateTime();
        String timeElapsed = TimeUtils.getTimeElapsed(timeStarted, currentTime);

        textViewTimeElapsed.setText(timeElapsed);
    }

    private void getIntentData() {

        String tripGson = getIntent().getStringExtra("trip");
        trip = GsonUtils.gsonToTrip(tripGson);

        order = trip.getOrder();
    }

    private void initViews() {

        scrollView = findViewById(R.id.layout_trip);
        cardViewStatus = findViewById(R.id.card_view_status);
        cardViewCancelled = findViewById(R.id.card_view_cancelled);

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

        buttonContactCustomer = findViewById(R.id.button_contact_customer);
        buttonContactCustomer.setOnClickListener(this);

        buttonContactMerchant = findViewById(R.id.button_contact_merchant);
        buttonContactMerchant.setOnClickListener(this);

        buttonStartTrip = findViewById(R.id.button_start_trip);
        buttonStartTrip.setOnClickListener(this);

        buttonCancelTrip = findViewById(R.id.button_cancel_trip);
        buttonCancelTrip.setOnClickListener(this);

        // footers
        layoutStartTrip = findViewById(R.id.layout_start_trip);

        initOnGoingLayout();
    }

    private void initOnGoingLayout() {

        layoutOnGoingTrip = findViewById(R.id.layout_on_going_footer);
        layoutStatus = findViewById(R.id.layout_status);
        textViewTripStatus = findViewById(R.id.text_view_trip_status);
        textViewTimeElapsed = findViewById(R.id.text_view_time_elapsed);

        buttonGetDirections = findViewById(R.id.button_get_directions);
        buttonGetDirections.setOnClickListener(this);

        buttonConfirmStatus = findViewById(R.id.button_confirm_status);
        buttonConfirmStatus.setOnClickListener(this);

        textViewGetDirections = findViewById(R.id.text_view_get_directions);
        textViewConfirmStatus = findViewById(R.id.text_view_confirm_status);
    }

    private void setupOnGoingLayout() {

        layoutOnGoingTrip.setVisibility(View.VISIBLE);

        textViewTripStatus.setText(MessageFormat.format("({0})",
                trip.getStatus().toString().replace("_", " ")));

        if (trip.getStatus() == TripStatus.STARTED) {

            textViewConfirmStatus.setText("Confirm Arrival To Source");
            textViewGetDirections.setText("Get Directions For Source");

        } else {

            textViewConfirmStatus.setText("Confirm Arrival To Destination");
            textViewGetDirections.setText("Get Directions For Destination");
        }


        // animate the status text
        Animation myFadeInAnimation = AnimationUtils
                .loadAnimation(TripActivity.this, R.anim.blink_anim);
        layoutStatus.startAnimation(myFadeInAnimation);

        // time handler
        timeHandler.postDelayed(timeRunnable = new Runnable() {
            public void run() {

                if (trip != null &&
                        trip.getStatus() != TripStatus.ACCEPTED &&
                        trip.getStatus() != TripStatus.COMPLETED
                        && textViewTimeElapsed != null) {

                    getTimeElapsed();

                    timeHandler.postDelayed(timeRunnable, timeDelay);
                }
            }
        }, timeDelay);
    }

    private void setupViews() {

        cardViewStatus.setVisibility(trip.getStatus() == TripStatus.COMPLETED ?
                View.VISIBLE : View.GONE);

        cardViewCancelled.setVisibility(trip.getStatus() == TripStatus.CANCELLED ?
                View.VISIBLE : View.GONE);

        buttonCancelTrip.setVisibility(View.GONE);

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

        // footers
        layoutStartTrip.setVisibility(View.GONE);
        layoutOnGoingTrip.setVisibility(View.GONE);

        switch (trip.getStatus()) {

            case ACCEPTED:
                buttonCancelTrip.setVisibility(View.VISIBLE);
                layoutStartTrip.setVisibility(View.VISIBLE);
                break;

            case STARTED:
            case PICKED_UP:
                setupOnGoingLayout();
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

        // get logged in user by phone and password from server
        Map<String, Object> data = new HashMap<>();
        data.put("phone_number", Session.getPhoneNumber(TripActivity.this));
        data.put("password", Session.getPassword(TripActivity.this));

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

                        Session.user.registerObserver(this);

                        driver = Session.user;
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

                        if (trip.getStatus() == TripStatus.ARRIVED_SOURCE) {

                            // open confirm pickup activity
                            Intent intent = new Intent(TripActivity.this,
                                    ConfirmPickupActivity.class);
                            intent.putExtra("trip", GsonUtils.tripToGson(trip));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            startActivity(intent);

                        } else if (trip.getStatus() == TripStatus.ARRIVED_DESTINATION) {

                            // open confirm delivery activity
                            Intent intent = new Intent(TripActivity.this,
                                    ConfirmDeliveryActivity.class);
                            intent.putExtra("trip", GsonUtils.tripToGson(trip));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            startActivity(intent);

                        } else {

                            setupViews();
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Log.d("checkout", "placeOrder: " + e.getMessage());

                });
    }

    private void startTrip() {

        trip.setDateStarted(StringUtils.getCurrentDateTime());

        AlertDialog.Builder alert;
        alert = new AlertDialog.Builder(this);
        alert.setTitle("Start Trip ?");

        alert.setPositiveButton("Yes", (dialog, whichButton) -> {

            dialog.dismiss();

            // set query data
            Map<String, Object> data = new HashMap<>();
            data.put("trip_id", trip.getId());
            data.put("trip_type", trip.getType().toString());
            data.put("trip_start_date", trip.getDateStarted());
            data.put("driver_id", driver.getId());
            data.put("customer_id", customer.getId());
            data.put("merchant_id", merchant.getId());
            data.put("order", trip.getOrder().toJson());

            showLoadingAnimation();

            // change order status to started then update view
            FirebaseFunctions
                    .getInstance()
                    .getHttpsCallable("trip_task-startTrip")
                    .call(data)
                    .addOnSuccessListener(httpsCallableResult -> {

                        hideLoadingAnimation();

                        // change trip status locally
                        for (int x = 0; x < Session.user.getTrips().size(); x++) {

                            if (Session.user.getTrips().get(x).getId().equals(trip.getId())) {

                                Session.user.getTrips().get(x).setStatus(TripStatus.STARTED);
                                Session.user.getTrips().get(x).setDateStarted(trip.getDateStarted());

                                break;
                            }
                        }

                        // notify observers
                        Session.user.notifyObservers("TRIP_STARTED",
                                trip.getId(), TripStatus.STARTED);
                    })
                    .addOnFailureListener(e -> {

                        hideLoadingAnimation();

                        Toast.makeText(TripActivity.this,
                                e.getMessage(), Toast.LENGTH_SHORT).show();

                        Log.d("trip", "acceptTripRequest: " + e.getMessage());
                    });
        });

        alert.setNegativeButton("No",
                (dialogInterface, i) -> dialogInterface.dismiss());

        alert.show();
    }

    private void getCurrentRouteDirections() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            turnOnGps();

        } else {

            getLocation();
        }

        String directionUrl = "";
        // if trip is going toward source
        if (trip.getStatus() == TripStatus.STARTED) {

            directionUrl = "http://maps.google.com/maps?daddr="
                    + trip.getSource().latitude + "," + trip.getSource().longitude;
        }
        // if trip is going toward destination
        else {

            directionUrl = "http://maps.google.com/maps?daddr="
                    + trip.getDestination().latitude + "," + trip.getDestination().longitude;
        }

        if (PackageUtils.getAppState(this, "com.google.android.apps.maps")) {

            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse(directionUrl));

            startActivity(intent);

        } else {

            androidx.appcompat.app.AlertDialog.Builder alert;
            alert = new androidx.appcompat.app.AlertDialog.Builder(this);
            alert.setTitle("Please install and enable google maps to get directions");
            alert.setCancelable(false);

            alert.setPositiveButton("OK", (dialog, whichButton) -> {

                dialog.dismiss();
            });

            alert.show();
        }
    }

    private void changeTripStatus() {

        String dialogTitle = "";

        if (trip.getStatus() == TripStatus.STARTED) {

            dialogTitle = "Confirm Arrival To Source ?";

        } else {

            dialogTitle = "Confirm Arrival To Destination ?";
        }

        AlertDialog.Builder alert;
        alert = new AlertDialog.Builder(this);
        alert.setTitle(dialogTitle);

        alert.setPositiveButton("Yes", (dialog, whichButton) -> {

            dialog.dismiss();

            String functionName = "";

            // change the trip status
            if (trip.getStatus() == TripStatus.STARTED) {

                trip.setStatus(TripStatus.ARRIVED_SOURCE);

                functionName = "trip_task-confirmArrivalToSource";

            } else {

                trip.setStatus(TripStatus.ARRIVED_DESTINATION);

                functionName = "trip_task-confirmArrivalToDestination";
            }

            // set query data
            Map<String, Object> data = new HashMap<>();
            data.put("trip_id", trip.getId());
            data.put("trip_type", trip.getType().toString());
            data.put("merchant_id", merchant.getId());
            data.put("customer_id", customer.getId());
            data.put("order", trip.getOrder().toJson());

            showLoadingAnimation();

            // update in cloud
            FirebaseFunctions
                    .getInstance()
                    .getHttpsCallable(functionName)
                    .call(data)
                    .addOnSuccessListener(httpsCallableResult -> {

                        hideLoadingAnimation();

                        // change trip status locally
                        for (int x = 0; x < Session.user.getTrips().size(); x++) {

                            if (Session.user.getTrips().get(x).getId().equals(trip.getId())) {

                                Session.user.getTrips().get(x).setStatus(trip.getStatus());
                                break;
                            }
                        }

                        // notify observers
                        Session.user.notifyObservers("STATUS_CHANGED",
                                trip.getId(), trip.getStatus());
                    })
                    .addOnFailureListener(e -> {

                        hideLoadingAnimation();

                        Toast.makeText(TripActivity.this,
                                e.getMessage(), Toast.LENGTH_SHORT).show();

                        Log.d("trip", "acceptTripRequest: " + e.getMessage());
                    });
        });

        alert.setNegativeButton("No",
                (dialogInterface, i) -> dialogInterface.dismiss());

        alert.show();
    }

    private void cancelTrip() {

        // set query data
        Map<String, Object> data = new HashMap<>();
        data.put("trip_id", trip.getId());
        data.put("trip_type", trip.getType().toString());
        data.put("customer_id", customer.getId());
        data.put("merchant_id", merchant.getId());
        data.put("driver_id", driver.getId());
        data.put("order_id", trip.getOrder().getId());

        showLoadingAnimation();

        // update in cloud
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("trip_task-cancelTrip")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    hideLoadingAnimation();

                    // change trip status locally
                    for (int x = 0; x < Session.user.getTrips().size(); x++) {

                        if (Session.user.getTrips().get(x).getId().equals(trip.getId())) {

                            Session.user.getTrips().get(x).setStatus(TripStatus.CANCELLED);
                            break;
                        }
                    }

                    scrollView.smoothScrollTo(0, 0);

                    // notify observers
                    Session.user.notifyObservers("STATUS_CHANGED",
                            trip.getId(), TripStatus.CANCELLED);
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Toast.makeText(TripActivity.this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();

                    Log.d("trip", "acceptTripRequest: " + e.getMessage());
                });
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_back) {

            goBack();

        } else if (view.getId() == R.id.button_contact_customer) {

            contactCustomer();

        } else if (view.getId() == R.id.button_contact_merchant) {

            contactMerchant();

        } else if (view.getId() == R.id.button_start_trip) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            String currentTime = dateFormat.format(new Date());

            String openingTime = merchant.getLaundry().getTimings().getOpeningTime();
            String closingTime = merchant.getLaundry().getTimings().getClosingTime();

            boolean laundryOpen;
            try {

                laundryOpen = TimeUtils.isTimeBetweenTwoTime(openingTime, closingTime, currentTime);

                if (!laundryOpen) {

                    androidx.appcompat.app.AlertDialog.Builder alert;
                    alert = new androidx.appcompat.app.AlertDialog.Builder(this);
                    alert.setTitle("The laundry is currently closed." +
                            " Try again later");
                    alert.setCancelable(false);

                    alert.setPositiveButton("OK", (dialog, whichButton) -> {

                        dialog.dismiss();
                    });

                    alert.show();

                } else {

                    startTrip();
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else if (view.getId() == R.id.button_get_directions) {

            getCurrentRouteDirections();

        } else if (view.getId() == R.id.button_confirm_status) {

            changeTripStatus();

        } else if (view.getId() == R.id.button_cancel_trip) {

            androidx.appcompat.app.AlertDialog.Builder alert;
            alert = new androidx.appcompat.app.AlertDialog.Builder(this);
            alert.setTitle("Cancel trip for sure ?");

            alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                dialog.dismiss();
                cancelTrip();
            });

            alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

            alert.show();
        }
    }

    private void goBack() {

        // if there are no activities in the background
        if (isTaskRoot()) {

            Intent intent = new Intent(TripActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();

        } else {

            Intent intent = new Intent(TripActivity.this, TripsActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        goBack();
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


    private void showLoadingAnimation() {

        layoutLoading.setVisibility(View.VISIBLE);
        layoutLoading.bringToFront();
        layoutLoading.animate().translationY(0);
    }

    private void hideLoadingAnimation() {

        layoutLoading.setVisibility(View.GONE);
        layoutLoading.animate().translationY(layoutLoading.getHeight());
    }

    @Override
    public void updateView(String task, String tripId, TripStatus status) {

        if (Globals.tripInView) {

            if (tripId.equals(trip.getId())) {

                trip.setStatus(status);

                if (status == TripStatus.ARRIVED_SOURCE) {

                    // open confirm pickup activity
                    Intent intent = new Intent(TripActivity.this,
                            ConfirmPickupActivity.class);
                    intent.putExtra("trip", GsonUtils.tripToGson(trip));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);

                } else if (status == TripStatus.ARRIVED_DESTINATION) {

                    // open confirm delivery activity
                    Intent intent = new Intent(TripActivity.this,
                            ConfirmDeliveryActivity.class);
                    intent.putExtra("trip", GsonUtils.tripToGson(trip));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);
                } else {

                    runOnUiThread(this::setupViews);
                }
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

    private void turnOnGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) ->
                        startActivity(new Intent(Settings.
                                ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("No",
                        (dialog, which) -> dialog.cancel());

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {

            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (locationGPS != null) {
                double latitude = locationGPS.getLatitude();
                double longitude = locationGPS.getLongitude();

                Session.user.setCurrentLocation(new LatLng(latitude, longitude));
                driver.setCurrentLocation(new LatLng(latitude, longitude));
            }
        }
    }
}