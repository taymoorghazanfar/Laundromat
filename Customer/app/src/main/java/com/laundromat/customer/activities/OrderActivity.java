package com.laundromat.customer.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
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
import com.laundromat.customer.R;
import com.laundromat.customer.helpers.direction_helpers.FetchURL;
import com.laundromat.customer.helpers.direction_helpers.TaskLoadedCallback;
import com.laundromat.customer.model.Customer;
import com.laundromat.customer.model.DeliveryBoy;
import com.laundromat.customer.model.Merchant;
import com.laundromat.customer.model.Trip;
import com.laundromat.customer.model.observers.IOrderObserver;
import com.laundromat.customer.model.order.OrderItem;
import com.laundromat.customer.model.order.OrderStatus;
import com.laundromat.customer.model.order.SaleItem;
import com.laundromat.customer.model.util.LiveLocationData;
import com.laundromat.customer.model.util.Location;
import com.laundromat.customer.model.util.TripStatus;
import com.laundromat.customer.model.util.TripType;
import com.laundromat.customer.prefs.CartPrefs;
import com.laundromat.customer.prefs.Session;
import com.laundromat.customer.ui.viewholders.OrderSummarySection;
import com.laundromat.customer.ui.views.ScrollableMapFragment;
import com.laundromat.customer.utils.Globals;
import com.laundromat.customer.utils.GsonUtils;
import com.laundromat.customer.utils.ImageUtils;
import com.laundromat.customer.utils.LocationUtils;
import com.laundromat.customer.utils.ParseUtils;
import com.laundromat.customer.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class OrderActivity extends AppCompatActivity
        implements View.OnClickListener, IOrderObserver, OnMapReadyCallback, TaskLoadedCallback {

    // Variables
    Customer customer = Session.user;
    com.laundromat.customer.model.order.Order order;
    Merchant merchant;

    // Handler
    Handler timeHandler = new Handler();
    Runnable timeRunnable;
    int timeDelay = 5000;
    private DeliveryBoy driver;
    private Trip trip;

    // Views
    private ScrollView scrollView;
    private ImageButton buttonBack;
    private TextView textViewLaundryName;
    private RelativeLayout layoutLoading;
    private TextView textViewOrderId;
    private TextView textViewDateCreated;
    private TextView textViewQuantity;
    private TextView textViewPrice;
    private TextView textViewDiscount;
    private TextView textViewPayable;
    private TextView textViewPaymentMethod;
    private RecyclerView recyclerViewOrderSummary;
    private SectionedRecyclerViewAdapter adapter;
    private ImageView imageViewLocation;
    private TextView textViewLocationName;
    private TextView textViewLocationAddress;
    private TextView textViewMerchantName;
    private TextView textViewMerchantPhone;
    private AppCompatButton buttonContact;
    private AppCompatButton buttonCancelOrder;
    // footer
    private TextView textViewStatus;
    private TextView textViewStatusMessage;
    private LottieAnimationView animationViewStatus;

    // Driver views
    private CardView layoutDriver;
    private CircleImageView imageViewDriver;
    private TextView textViewDriverName;
    private TextView textViewDriverPhone;
    private TextView textViewPlateNumber;
    private TextView textViewVehicle;
    private TextView textViewVehicleColor;
    private AppCompatButton buttonContactDriver;

    // Live location view
    private LinearLayout layoutLiveLocation;
    private ScrollableMapFragment mapLiveLocation;
    private TextView textViewLiveLocation;

    // Live location variables
    private GoogleMap mMap;
    private MarkerOptions driverMarker, customerMarker;
    private Polyline currentPolyline;
    private LiveLocationData liveLocationData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        Globals.orderInView = true;

        getIntentData();

        initViews();

        if (customer == null) {

            getCustomer();

        } else {

            Session.user.registerObserver(this);

            getOrderMerchant();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Globals.orderInView = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (driver != null && trip != null) {

            if (trip.getType() == TripType.PICKUP) {

                if (trip.getStatus() == TripStatus.STARTED) {

                    if (mMap != null) {

                        timeHandler.postDelayed(timeRunnable = () -> {

                            getLiveLocationUpdate();

                            timeHandler.postDelayed(timeRunnable, timeDelay);

                        }, timeDelay);
                    }
                }

            } else if (trip.getType() == TripType.DELIVERY) {

                if (trip.getStatus() == TripStatus.PICKED_UP) {

                    if (mMap != null) {

                        timeHandler.postDelayed(timeRunnable = () -> {

                            getLiveLocationUpdate();

                            timeHandler.postDelayed(timeRunnable, timeDelay);

                        }, timeDelay);
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {

        timeHandler.removeCallbacks(timeRunnable);
        super.onPause();
    }

    private void getIntentData() {

        String orderGson = getIntent().getStringExtra("order");
        order = GsonUtils.gsonToOrder(orderGson);
    }

    private void initViews() {

        scrollView = findViewById(R.id.layout_order);

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        textViewLaundryName = findViewById(R.id.text_view_laundry_name);
        textViewOrderId = findViewById(R.id.text_view_order_id);
        textViewDateCreated = findViewById(R.id.text_view_date_created);
        textViewQuantity = findViewById(R.id.text_view_item_quantity);
        textViewPrice = findViewById(R.id.text_view_price);
        textViewDiscount = findViewById(R.id.text_view_discount);
        textViewPayable = findViewById(R.id.text_view_payable);
        textViewPaymentMethod = findViewById(R.id.text_view_payment_method);
        recyclerViewOrderSummary = findViewById(R.id.recycler_view_order_summary);
        recyclerViewOrderSummary.setNestedScrollingEnabled(false);
        imageViewLocation = findViewById(R.id.image_view_location);
        textViewLocationName = findViewById(R.id.text_view_location_name);
        textViewLocationAddress = findViewById(R.id.text_view_location_address);
        textViewMerchantName = findViewById(R.id.text_view_merchant_name);
        textViewMerchantPhone = findViewById(R.id.text_view_merchant_phone);

        buttonContact = findViewById(R.id.button_contact);
        buttonContact.setOnClickListener(this);

        buttonCancelOrder = findViewById(R.id.button_cancel_order);
        buttonCancelOrder.setOnClickListener(this);
        buttonCancelOrder.setVisibility(View.GONE);

        // footer
        textViewStatus = findViewById(R.id.text_view_status);
        textViewStatusMessage = findViewById(R.id.text_view_message);
        animationViewStatus = findViewById(R.id.animation_status);

        // driver views
        layoutDriver = findViewById(R.id.layout_driver_info);
        imageViewDriver = findViewById(R.id.image_view_driver);
        textViewDriverName = findViewById(R.id.text_view_driver_name);
        textViewDriverPhone = findViewById(R.id.text_view_driver_phone);
        textViewPlateNumber = findViewById(R.id.text_view_plate_number);
        textViewVehicle = findViewById(R.id.text_view_vehicle);
        textViewVehicleColor = findViewById(R.id.text_view_vehicle_color);

        buttonContactDriver = findViewById(R.id.button_contact_driver);
        buttonContactDriver.setOnClickListener(this);

        // Live location views
        layoutLiveLocation = findViewById(R.id.layout_live_location);
        layoutLiveLocation.setVisibility(View.GONE);

        mapLiveLocation = ((ScrollableMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_live_location));

        mapLiveLocation.setListener(() -> scrollView.requestDisallowInterceptTouchEvent(true));

        textViewLiveLocation = findViewById(R.id.text_view_live_location);
    }

    private void setupLiveLocation() {

        layoutLiveLocation.setVisibility(View.VISIBLE);

        // setup markers
        if (trip.getType() == TripType.PICKUP) {

            driverMarker = new MarkerOptions().position(driver.getCurrentLocation())
                    .title("Rider")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin));

            customerMarker = new MarkerOptions().position(trip.getSource())
                    .title("Your Location").icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else {

            driverMarker = new MarkerOptions().position(driver.getCurrentLocation())
                    .title("Rider")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin));

            customerMarker = new MarkerOptions().position(trip.getDestination())
                    .title("Your Location").icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }

        Objects.requireNonNull(mapLiveLocation).getMapAsync(this);
    }

    private void getCustomer() {

        showLoadingAnimation();

        // get logged in user by phone and password from server
        Map<String, Object> data = new HashMap<>();
        data.put("phone_number", Session.getPhoneNumber(OrderActivity.this));
        data.put("password", Session.getPassword(OrderActivity.this));

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("customer-verifyLogin")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        // saving new logged in user to session
                        Session.user = ParseUtils
                                .parseCustomer(httpsCallableResult.getData());

                        Session.user.registerObserver(OrderActivity.this);

                        // get saved cart
                        if (CartPrefs.get(OrderActivity.this) != null) {

                            Session.user.setCart(CartPrefs.get(OrderActivity.this));
                        }

                        getOrderMerchant();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(OrderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("splash", "startSplash: " + e.getMessage());
                });
    }

    private void getOrderMerchant() {

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("merchant-getMerchantByLaundryId")
                .call(order.getLaundryId())
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        hideLoadingAnimation();

                        merchant = ParseUtils.parseMerchant(httpsCallableResult.getData());

                        if (order.getStatus() == OrderStatus.PICK_UP
                                || order.getStatus() == OrderStatus.DELIVERING) {

                            getTrip();

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

    private void getTrip() {

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("trip_task-getTripByOrderId")
                .call(order.getId())
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        hideLoadingAnimation();

                        trip = ParseUtils.parseTrip(httpsCallableResult.getData());

                        if (order.getStatus() == OrderStatus.PICK_UP
                                && trip.getStatus() == TripStatus.ARRIVED_SOURCE) {

                            Intent intent = new Intent(OrderActivity.this,
                                    ConfirmPickupActivity.class);
                            intent.putExtra("order", GsonUtils.orderToGson(order));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            startActivity(intent);

                        } else if (order.getStatus() == OrderStatus.DELIVERING
                                && trip.getStatus() == TripStatus.ARRIVED_DESTINATION) {

                            Intent intent = new Intent(OrderActivity.this,
                                    ConfirmCollectedActivity.class);
                            intent.putExtra("order", GsonUtils.orderToGson(order));
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

    private void setupDriverViews() {

        Picasso.get()
                .load(driver.getAvatarUrl())
                .into(imageViewDriver);

        textViewDriverName.setText(driver.getFullName());

        textViewDriverPhone.setText(MessageFormat
                .format("+92{0}", driver.getPhoneNumber()));

        textViewPlateNumber.setText(driver.getVehicle().getPlateNumber());

        textViewVehicle.setText(MessageFormat.format("{0} {1}",
                driver.getVehicle().getName(), driver.getVehicle().getModel()));

        textViewVehicleColor.setText(driver.getVehicle().getColor());

        if (trip != null && order != null && driver != null) {

            if (trip.getType() == TripType.PICKUP) {

                if (trip.getStatus() == TripStatus.STARTED) {

                    setupLiveLocation();
                }

            } else if (trip.getType() == TripType.DELIVERY) {

                if (trip.getStatus() == TripStatus.PICKED_UP) {

                    setupLiveLocation();
                }
            }
        }
    }

    private void getDriver() {

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("delivery_boy-getByOrderId")
                .call(order.getId())
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        hideLoadingAnimation();

                        driver = ParseUtils.parseDriver(httpsCallableResult.getData());

                        setupDriverViews();
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Log.d("checkout", "placeOrder: " + e.getMessage());

                });
    }

    private void setupViews() {

        Log.d("order_status", "setupViews: status: " + order.getStatus());

        if (trip == null) {

            removeLiveLocation();
        }

        textViewLaundryName.setText(order.getLaundryName());
        textViewOrderId.setText(order.getId().substring(order.getId().length() - 10));
        textViewDateCreated.setText(order.getDateCreated());
        textViewQuantity.setText(String.valueOf(order.getItemsQuantity()));
        textViewPrice.setText(MessageFormat.format("PKR {0}", (order.getPrice() + order.getDiscount())));
        textViewDiscount.setText(MessageFormat.format("PKR {0}", order.getDiscount()));
        textViewPayable.setText(MessageFormat.format("PKR {0}", order.getPrice()));
        textViewPaymentMethod.setText(order.getPaymentMethod().toString().replace("_", " "));

        layoutDriver.setVisibility(View.GONE);

        // only show cancel button till the order is in accepted or requested state
        if (order.getStatus() == OrderStatus.REQUESTED
                || order.getStatus() == OrderStatus.ACCEPTED) {

            buttonCancelOrder.setVisibility(View.VISIBLE);

        } else {

            buttonCancelOrder.setVisibility(View.GONE);
        }

        setupRecyclerView();

        String locationUrl = StringUtils.getMapsStaticImageUrl(this,
                order.getDeliveryLocation());
        Picasso.get()
                .load(locationUrl)
                .into(imageViewLocation);

        String locationName = "Unknown";

        for (Location location : Session.user.getLocations()) {

            if (location.getLatLng().latitude == order.getDeliveryLocation().latitude
                    && location.getLatLng().longitude == order.getDeliveryLocation().longitude) {

                locationName = location.getName();
            }
        }
        textViewLocationName.setText(locationName);

        String locationAddress = LocationUtils.getAddressFromLatLng(this,
                order.getDeliveryLocation().latitude,
                order.getDeliveryLocation().longitude);
        textViewLocationAddress.setText(locationAddress);

        textViewMerchantName.setText(merchant.getFullName());
        textViewMerchantPhone.setText(MessageFormat.format("+92{0}",
                merchant.getPhoneNumber()));

        switch (order.getStatus()) {

            case CANCELLED:
                textViewStatus.setText("Cancelled");
                textViewStatusMessage.setText("This order has been cancelled");
                animationViewStatus.setAnimation(R.raw.cancelled);
                animationViewStatus.playAnimation();
                break;

            case DECLINED:
                textViewStatus.setText("Declined");
                textViewStatusMessage.setText("This order request has been declined by laundry");
                animationViewStatus.setAnimation(R.raw.declined);
                animationViewStatus.playAnimation();
                break;

            case REQUESTED:
                textViewStatus.setText("Order Requested");
                textViewStatusMessage.setText("You will be notified once the laundry accepts your order request");
                animationViewStatus.setAnimation(R.raw.requested);
                animationViewStatus.playAnimation();
                break;

            case ACCEPTED:
                textViewStatus.setText("Order Request Accepted");
                textViewStatusMessage.setText("Laundry will book a rider for pickup soon");
                animationViewStatus.setAnimation(R.raw.accepted);
                animationViewStatus.playAnimation();
                break;

            case PICKUP_REQUESTED:
                textViewStatus.setText("Pickup Requested");
                textViewStatusMessage.setText("Laundry has requested for pickup of items");
                animationViewStatus.setAnimation(R.raw.requested);
                animationViewStatus.playAnimation();
                break;

            case PICKUP_ACCEPTED:
                textViewStatus.setText("Pickup Accepted");
                textViewStatusMessage.setText("You will be notified once the rider starts the trip");
                animationViewStatus.setAnimation(R.raw.accepted);
                animationViewStatus.playAnimation();
                layoutDriver.setVisibility(View.VISIBLE);
                getDriver();
                break;

            case PICK_UP:
                Log.d("order_status", "setupViews: inside pickup");
                layoutDriver.setVisibility(View.VISIBLE);
                getDriver();

                if (trip != null) {

                    Log.d("order_status", "setupViews: trip != null");
                    Log.d("order_status", "setupViews: trip id: " + trip.getId());
                    Log.d("order_status", "setupViews: trip status: " + trip.getStatus());

                    if (trip.getStatus() == TripStatus.STARTED) {

                        Log.d("order_status", "setupViews: inside trip started");

                        textViewStatus.setText("Pickup Started");
                        textViewStatusMessage.setText(R.string.pickup_note);
                        animationViewStatus.setAnimation(R.raw.delivering);
                        animationViewStatus.playAnimation();

                    } else if (trip.getStatus() == TripStatus.PICKED_UP
                            || trip.getStatus() == TripStatus.ARRIVED_DESTINATION) {

                        textViewStatus.setText("Delivering To Laundry");
                        textViewStatusMessage.setText("Your items are currently being delivered to the laundry");
                        animationViewStatus.setAnimation(R.raw.delivering);
                        animationViewStatus.playAnimation();
                    }
                }
                break;

            case COLLECTED:
                removeLiveLocation();
                textViewStatus.setText("Items Collected By Laundry");
                textViewStatusMessage.setText("You will be notified once the laundry starts washing your items");
                animationViewStatus.setAnimation(R.raw.delivered);
                animationViewStatus.playAnimation();
                break;

            case IN_SERVICE:
                textViewStatus.setText("Washing");
                textViewStatusMessage.setText("Your items are currently being washed");
                animationViewStatus.setAnimation(R.raw.washing);
                animationViewStatus.playAnimation();
                break;

            case WASHED:
                textViewStatus.setText("Items Washed");
                textViewStatusMessage.setText("You will be notified once the laundry books a rider for delivery");
                animationViewStatus.setAnimation(R.raw.washed);
                animationViewStatus.playAnimation();
                break;

            case DELIVERY_REQUESTED:
                textViewStatus.setText("Delivery Requested");
                textViewStatusMessage.setText("Laundry has requested for pickup of items");
                animationViewStatus.setAnimation(R.raw.requested);
                animationViewStatus.playAnimation();
                break;

            case DELIVERY_ACCEPTED:
                textViewStatus.setText("Delivery Request Accepted");
                textViewStatusMessage.setText("You will be notified once the rider starts the trip");
                animationViewStatus.setAnimation(R.raw.accepted);
                animationViewStatus.playAnimation();
                layoutDriver.setVisibility(View.VISIBLE);
                getDriver();
                break;

            case DELIVERING:
                layoutDriver.setVisibility(View.VISIBLE);
                getDriver();

                if (trip != null) {

                    if (trip.getStatus() == TripStatus.STARTED
                            || trip.getStatus() == TripStatus.ARRIVED_SOURCE) {

                        textViewStatus.setText("Delivering");
                        textViewStatusMessage.setText("You will be informed once the rider reaches your location");
                        animationViewStatus.setAnimation(R.raw.delivering);
                        animationViewStatus.playAnimation();

                    } else if (trip.getStatus() == TripStatus.PICKED_UP) {

                        textViewStatus.setText("Delivering");
                        textViewStatusMessage.setText("Get ready! Rider will arrive soon at your location");
                        animationViewStatus.setAnimation(R.raw.delivering);
                        animationViewStatus.playAnimation();
                    }
                }
                break;

            case COMPLETED:
                removeLiveLocation();
                textViewStatus.setText("Order Completed");
                textViewStatusMessage.setText("This order has been completed");
                animationViewStatus.setAnimation(R.raw.completed);
                animationViewStatus.playAnimation();
                break;
        }
    }

    private void setupRecyclerView() {

        Log.d("recycler", "size: " + order.getItems().size());

        recyclerViewOrderSummary.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SectionedRecyclerViewAdapter();

        for (final Map.Entry<String, OrderItem> entry : order.getItems().entrySet()) {

            if (entry.getValue().getSaleItems().size() > 0) {

                List<SaleItem> saleItems = new ArrayList<>(entry.getValue().getSaleItems().values());

                adapter.addSection(
                        new OrderSummarySection(entry.getKey(), saleItems));
            }
        }

        recyclerViewOrderSummary.setAdapter(adapter);
    }

    private void contactMerchant() {

        String phone = "0092" + merchant.getPhoneNumber();
        Intent intent = new Intent(Intent.ACTION_DIAL,
                Uri.fromParts("tel", phone, null));
        startActivity(intent);
    }

    private void contactDriver() {

        if (driver != null) {

            String phone = "0092" + driver.getPhoneNumber();
            Intent intent = new Intent(Intent.ACTION_DIAL,
                    Uri.fromParts("tel", phone, null));
            startActivity(intent);
        }
    }

    private void cancelOrder() {

        AlertDialog.Builder alert;
        alert = new AlertDialog.Builder(this);
        alert.setTitle("Cancel Order for sure?");

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

                dialog.dismiss();

                // set query data
                Map<String, String> data = new HashMap<>();
                data.put("laundry_id", order.getLaundryId());
                data.put("customer_id", order.getCustomerId());
                data.put("order_id", order.getId());

                showLoadingAnimation();

                FirebaseFunctions
                        .getInstance()
                        .getHttpsCallable("order_task-cancelOrderByCustomer")
                        .call(data)
                        .addOnSuccessListener(httpsCallableResult -> {

                            hideLoadingAnimation();

                            // change order status locally
                            for (int x = 0; x < Session.user.getOrders().size(); x++) {

                                if (Session.user
                                        .getOrders().get(x).getId().equals(order.getId())) {

                                    Session.user
                                            .getOrders().get(x).setStatus(OrderStatus.CANCELLED);
                                    break;
                                }
                            }

                            // notify observers
                            Session.user.notifyObservers("ORDER_CANCEL", order.getId(), OrderStatus.CANCELLED);

                        })
                        .addOnFailureListener(e -> {

                            hideLoadingAnimation();

                            Toast.makeText(OrderActivity.this,
                                    e.getMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        });
            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });

        alert.show();
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_back) {

            goBack();
        } else if (view.getId() == R.id.button_contact) {

            contactMerchant();

        } else if (view.getId() == R.id.button_contact_driver) {

            contactDriver();

        } else if (view.getId() == R.id.button_cancel_order) {

            cancelOrder();
        }
    }

    @Override
    public void onBackPressed() {

        goBack();
    }

    private void goBack() {

        // if there are no activities in the background
        if (isTaskRoot()) {

            Intent intent = new Intent(OrderActivity.this, LaundriesActivity.class);
            startActivity(intent);
            finish();

        } else {

            Intent intent = new Intent(OrderActivity.this, OrdersActivity.class);
            startActivity(intent);
            finish();
        }
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
    public void updateView(String task, String orderId, OrderStatus status) {

        if (Globals.orderInView) {

            if (orderId.equals(order.getId())) {

                order.setStatus(status);

                if (status == OrderStatus.PICK_UP) {

                    if (task.equals("ARRIVED_SOURCE")) {

                        runOnUiThread(() -> {

                            Intent intent = new Intent(OrderActivity.this,
                                    ConfirmPickupActivity.class);
                            intent.putExtra("order", GsonUtils.orderToGson(order));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            startActivity(intent);
                        });

                    } else {

                        runOnUiThread(this::getTrip);
                    }

                } else if (status == OrderStatus.DELIVERING) {

                    if (task.equals("ARRIVED_DESTINATION")) {

                        runOnUiThread(() -> {

                            Intent intent = new Intent(OrderActivity.this,
                                    ConfirmCollectedActivity.class);
                            intent.putExtra("order", GsonUtils.orderToGson(order));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            startActivity(intent);
                        });

                    } else {

                        runOnUiThread(this::getTrip);
                    }

                } else {

                    runOnUiThread(this::setupViews);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    // MAPS
    ///////////////////////////////////////////////////////////////////////////////
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                if (mMap != null) {

                    setupMaps();

                    // start timer to fetch periodic location
                    timeHandler.postDelayed(timeRunnable = new Runnable() {
                        public void run() {

                            if (trip != null &&
                                    trip.getStatus() != TripStatus.ACCEPTED &&
                                    trip.getStatus() != TripStatus.COMPLETED
                                    && textViewLiveLocation != null
                                    && mapLiveLocation != null) {

                                getLiveLocationUpdate();

                                timeHandler.postDelayed(timeRunnable, timeDelay);
                            }
                        }
                    }, timeDelay);
                }
            }
        });
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

        mMap.addMarker(driverMarker);
        mMap.addMarker(customerMarker);
    }

    private void setupMaps() {
        new FetchURL(OrderActivity.this)
                .execute(getUrl(driverMarker.getPosition(), customerMarker.getPosition(),
                        "driving"), "driving");
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

        if (mMap != null) {

            if (currentPolyline != null) {

                currentPolyline.remove();
            }

            mMap.clear();

            currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
            mMap.addMarker(driverMarker);
            mMap.addMarker(customerMarker);
            recenterMap();
        }
    }

    private void recenterMap() {

        if (mMap != null) {

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(driverMarker.getPosition());
            builder.include(customerMarker.getPosition());
            LatLngBounds bounds = builder.build();

            int padding = 100; // padding around start and end marker
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);
        }
    }

    private void getLiveLocationUpdate() {

        if (driver != null && trip != null &&
                mapLiveLocation != null && textViewLiveLocation != null) {

            Map<String, Object> data = new HashMap<>();
            data.put("driver_id", driver.getId());

            FirebaseFunctions
                    .getInstance()
                    .getHttpsCallable("delivery_boy-getLiveLocation")
                    .call(data)
                    .addOnSuccessListener(httpsCallableResult -> {

                        if (httpsCallableResult.getData() != null) {

                            liveLocationData = ParseUtils
                                    .parseLiveLocation(httpsCallableResult.getData());

                            //todo: update map
                            driverMarker = new MarkerOptions().position(new
                                    LatLng(liveLocationData.getLatitude(), liveLocationData.getLongitude()))
                                    .title("Rider").icon(BitmapDescriptorFactory
                                            .fromResource(R.drawable.ic_pin));

                            setupMaps();

                            //todo: update estimate time
                            getEstimateTime(
                                    new LatLng(liveLocationData.getLatitude(),
                                            liveLocationData.getLongitude()),
                                    liveLocationData.getSpeed());
                        }
                    })
                    .addOnFailureListener(e -> {

                        Log.d("checkout", "placeOrder: " + e.getMessage());
                    });
        }
    }

    private void getEstimateTime(LatLng driverLocation, float speed) {

        if (textViewLiveLocation != null) {

            double distance;

            if (trip.getType() == TripType.PICKUP) {

                distance = LocationUtils.getDistanceBetweenTwoPoints(driverLocation, trip.getSource());

            } else {

                distance = LocationUtils.getDistanceBetweenTwoPoints(driverLocation, trip.getDestination());
            }

            String estimatedTime = LocationUtils.getEstimatedTime(distance, speed);

            if (estimatedTime != null) {

                textViewLiveLocation.setText(estimatedTime);
            }
        }
    }

    private void removeLiveLocation() {

        layoutLiveLocation.setVisibility(View.GONE);

        timeHandler.removeCallbacks(timeRunnable);
    }
}