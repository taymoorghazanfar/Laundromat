package com.laundromat.merchant.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.gson.Gson;
import com.laundromat.merchant.R;
import com.laundromat.merchant.dialogs.ChangeOrderStatusDialog;
import com.laundromat.merchant.helpers.direction_helpers.FetchURL;
import com.laundromat.merchant.helpers.direction_helpers.TaskLoadedCallback;
import com.laundromat.merchant.model.Customer;
import com.laundromat.merchant.model.DeliveryBoy;
import com.laundromat.merchant.model.Merchant;
import com.laundromat.merchant.model.Trip;
import com.laundromat.merchant.model.observers.IMerchantObserver;
import com.laundromat.merchant.model.order.Order;
import com.laundromat.merchant.model.order.OrderItem;
import com.laundromat.merchant.model.order.OrderStatus;
import com.laundromat.merchant.model.order.SaleItem;
import com.laundromat.merchant.model.util.LiveLocationData;
import com.laundromat.merchant.model.util.TripStatus;
import com.laundromat.merchant.model.util.TripType;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.interfaces.IOrderStatusUpdatedListener;
import com.laundromat.merchant.ui.viewholders.OrderSummarySection;
import com.laundromat.merchant.ui.views.ScrollableMapFragment;
import com.laundromat.merchant.utils.Globals;
import com.laundromat.merchant.utils.GsonUtils;
import com.laundromat.merchant.utils.LocationUtils;
import com.laundromat.merchant.utils.ParseUtils;
import com.laundromat.merchant.utils.StringUtils;
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
        implements View.OnClickListener, IMerchantObserver, IOrderStatusUpdatedListener,
        OnMapReadyCallback, TaskLoadedCallback {

    private static final int REQUEST_CODE_PICKUP = 111;

    // Handler
    Handler timeHandler = new Handler();
    Runnable timeRunnable;
    int timeDelay = 5000;
    // Variables
    private Order order;
    private Customer customer;
    private DeliveryBoy driver;
    private Merchant merchant;
    private Trip trip;
    // Views
    private ScrollView scrollView;
    private ImageButton buttonBack;
    private TextView textViewOrderIdHeader;
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
    private TextView textViewLocationDistance;
    private TextView textViewLocationAddress;
    private TextView textViewCustomerName;
    private TextView textViewCustomerPhone;
    private AppCompatButton buttonContact;
    private TextView textViewOrderStatus;
    private AppCompatButton buttonChangeStatus;
    private AppCompatButton buttonCancelOrder;

    // footers
    private RelativeLayout buttonPickupOrder;
    private TextView textViewPickupRequest;
    private RelativeLayout layoutStatus;
    private RelativeLayout layoutOrderStatus;
    private LottieAnimationView animationViewStatus;
    private TextView textViewStatus;
    private TextView textViewStatusMessage;
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
    private MarkerOptions driverMarker, merchantMarker;
    private Polyline currentPolyline;
    private LiveLocationData liveLocationData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        Globals.orderInView = true;

        getIntentData();

        initViews();

        if (Session.user == null) {

            getMerchant();

        } else {

            Session.user.registerObserver(this);

            getOrderCustomer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Globals.orderInView = false;
        Log.d("order_activity", "onDestroy: destroyed");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (driver != null && trip != null) {

            if (trip.getType() == TripType.PICKUP) {

                if (trip.getStatus() == TripStatus.PICKED_UP) {

                    if (mMap != null) {

                        timeHandler.postDelayed(timeRunnable = () -> {

                            getLiveLocationUpdate();

                            timeHandler.postDelayed(timeRunnable, timeDelay);

                        }, timeDelay);
                    }
                }

            } else if (trip.getType() == TripType.DELIVERY) {

                if (trip.getStatus() == TripStatus.STARTED) {

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

        textViewOrderIdHeader = findViewById(R.id.text_view_order_id_header);
        textViewOrderId = findViewById(R.id.text_view_order_id);
        textViewDateCreated = findViewById(R.id.text_view_date_created);
        textViewQuantity = findViewById(R.id.text_view_item_quantity);
        textViewPrice = findViewById(R.id.text_view_price);
        textViewDiscount = findViewById(R.id.text_view_discount);
        textViewPayable = findViewById(R.id.text_view_payable);
        textViewPaymentMethod = findViewById(R.id.text_view_payment_method);
        recyclerViewOrderSummary = findViewById(R.id.recycler_view_order_summary);
        imageViewLocation = findViewById(R.id.image_view_location);
        textViewLocationDistance = findViewById(R.id.text_view_location_distance);
        textViewLocationAddress = findViewById(R.id.text_view_location_address);
        textViewCustomerName = findViewById(R.id.text_view_customer_name);
        textViewCustomerPhone = findViewById(R.id.text_view_customer_phone);
        textViewOrderStatus = findViewById(R.id.text_view_status);

        buttonChangeStatus = findViewById(R.id.button_change_status);
        buttonChangeStatus.setOnClickListener(this);

        buttonContact = findViewById(R.id.button_contact);
        buttonContact.setOnClickListener(this);

        // footers
        buttonPickupOrder = findViewById(R.id.button_pickup_order);
        textViewPickupRequest = findViewById(R.id.text_view_request_pickup);
        buttonPickupOrder.setOnClickListener(this);

        layoutStatus = findViewById(R.id.layout_status);

        layoutOrderStatus = findViewById(R.id.footer_order_status);
        animationViewStatus = findViewById(R.id.animation_status);
        textViewStatus = findViewById(R.id.text_view_order_status);
        textViewStatusMessage = findViewById(R.id.text_view_message);

        buttonCancelOrder = findViewById(R.id.button_cancel_order);
        buttonCancelOrder.setOnClickListener(this);


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

            merchantMarker = new MarkerOptions().position(trip.getSource())
                    .title("Your Location").icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else {

            driverMarker = new MarkerOptions().position(driver.getCurrentLocation())
                    .title("Rider")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin));

            merchantMarker = new MarkerOptions().position(trip.getDestination())
                    .title("Your Location").icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }

        Objects.requireNonNull(mapLiveLocation).getMapAsync(this);
    }

    private void setupViews() {

        if (trip == null) {

            removeLiveLocation();
        }

        textViewOrderIdHeader.setText(order.getId().substring(order.getId().length() - 10));
        textViewOrderId.setText(order.getId().substring(order.getId().length() - 10));
        textViewDateCreated.setText(order.getDateCreated());
        textViewQuantity.setText(String.valueOf(order.getItemsQuantity()));
        textViewPrice.setText(MessageFormat.format("PKR {0}", (order.getPrice() + order.getDiscount())));
        textViewDiscount.setText(MessageFormat.format("PKR {0}", order.getDiscount()));
        textViewPayable.setText(MessageFormat.format("PKR {0}", order.getPrice()));
        textViewPaymentMethod.setText(order.getPaymentMethod().toString().replace("_", " "));

        setupRecyclerView();

        String locationUrl = StringUtils.getMapsStaticImageUrl(this,
                order.getDeliveryLocation());

        Picasso.get()
                .load(locationUrl)
                .into(imageViewLocation);

        double locationDistance = LocationUtils.getDistanceBetweenTwoPoints(
                order.getDeliveryLocation(),
                Session.user.getLaundry().getLocation());
        String distance = locationDistance + " KM";
        textViewLocationDistance.setText(distance);

        String locationAddress = LocationUtils.getAddressFromLatLng(this,
                order.getDeliveryLocation().latitude,
                order.getDeliveryLocation().longitude);
        textViewLocationAddress.setText(locationAddress);

        textViewCustomerName.setText(customer.getFullName());
        textViewCustomerPhone.setText(MessageFormat.format("+92{0}",
                customer.getPhoneNumber()));

        // footers
        layoutStatus.setVisibility(View.GONE);
        buttonPickupOrder.setVisibility(View.GONE);
        layoutOrderStatus.setVisibility(View.GONE);
        layoutDriver.setVisibility(View.GONE);

        switch (order.getStatus()) {

            case CANCELLED:
                layoutOrderStatus.setVisibility(View.VISIBLE);
                textViewStatus.setText("Cancelled");
                textViewStatusMessage.setText("This order has been cancelled");
                animationViewStatus.setAnimation(R.raw.cancelled);
                animationViewStatus.playAnimation();
                break;

            case DECLINED:
                layoutOrderStatus.setVisibility(View.VISIBLE);
                textViewStatus.setText("Declined");
                textViewStatusMessage.setText("This order request has been declined");
                animationViewStatus.setAnimation(R.raw.declined);
                animationViewStatus.playAnimation();
                break;

            case ACCEPTED:
                buttonPickupOrder
                        .setVisibility(View.VISIBLE);
                textViewPickupRequest.setText("Request For Pickup");
                buttonCancelOrder.setVisibility(View.VISIBLE);
                break;

            case PICKUP_REQUESTED:
                layoutOrderStatus.setVisibility(View.VISIBLE);
                textViewStatus.setText("Pickup Requested");
                textViewStatusMessage.setText("You will be notified once any rider accepts the request");
                animationViewStatus.setAnimation(R.raw.requested);
                animationViewStatus.playAnimation();
                break;

            case PICKUP_ACCEPTED:
                layoutOrderStatus.setVisibility(View.VISIBLE);
                textViewStatus.setText("Pickup Accepted");
                textViewStatusMessage.setText("You will be notified once the rider starts the trip");
                animationViewStatus.setAnimation(R.raw.accepted);
                animationViewStatus.playAnimation();
                layoutDriver.setVisibility(View.VISIBLE);
                getDriver();
                break;

            case PICK_UP:
                layoutDriver.setVisibility(View.VISIBLE);
                getDriver();

                if (trip != null) {

                    if (trip.getStatus() == TripStatus.STARTED) {

                        layoutOrderStatus.setVisibility(View.VISIBLE);
                        textViewStatus.setText("Pickup Started");
                        textViewStatusMessage.setText("You will be notified once the rider reaches your location for delivery");
                        animationViewStatus.setAnimation(R.raw.delivering);
                        animationViewStatus.playAnimation();

                    } else if (trip.getStatus() == TripStatus.ARRIVED_SOURCE) {

                        layoutOrderStatus.setVisibility(View.VISIBLE);
                        textViewStatus.setText("Pickup Started");
                        textViewStatusMessage.setText("You will be notified once the rider reaches your location for delivery");
                        animationViewStatus.setAnimation(R.raw.delivering);
                        animationViewStatus.playAnimation();

                    } else if (trip.getStatus() == TripStatus.PICKED_UP) {

                        layoutOrderStatus.setVisibility(View.VISIBLE);
                        textViewStatus.setText("Picked Up");
                        textViewStatusMessage.setText("Get ready! The rider will soon arrive at your location for delivery");
                        animationViewStatus.setAnimation(R.raw.delivering);
                        animationViewStatus.playAnimation();
                    }
                }
                break;

            case WASHED:
                buttonPickupOrder
                        .setVisibility(View.VISIBLE);
                textViewPickupRequest.setText("Request For Delivery");
                break;

            case DELIVERY_REQUESTED:
                layoutOrderStatus.setVisibility(View.VISIBLE);
                textViewStatus.setText("Delivery Requested");
                textViewStatusMessage.setText("You will be notified once any rider accepts the request");
                animationViewStatus.setAnimation(R.raw.requested);
                animationViewStatus.playAnimation();
                break;

            case DELIVERY_ACCEPTED:
                layoutOrderStatus.setVisibility(View.VISIBLE);
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

                    if (trip.getStatus() == TripStatus.STARTED) {

                        layoutOrderStatus.setVisibility(View.VISIBLE);
                        textViewStatus.setText("Pickup Started");
                        textViewStatusMessage.setText(R.string.pickup_note);
                        animationViewStatus.setAnimation(R.raw.delivering);
                        animationViewStatus.playAnimation();
                        layoutDriver.setVisibility(View.VISIBLE);

                    } else if (trip.getStatus() == TripStatus.PICKED_UP
                            || trip.getStatus() == TripStatus.ARRIVED_DESTINATION) {

                        layoutOrderStatus.setVisibility(View.VISIBLE);
                        textViewStatus.setText("Delivering");
                        textViewStatusMessage.setText("Rider is on his way to deliver");
                        animationViewStatus.setAnimation(R.raw.delivering);
                        animationViewStatus.playAnimation();
                        layoutDriver.setVisibility(View.VISIBLE);
                    }
                }
                break;

            case COMPLETED:
                removeLiveLocation();
                layoutOrderStatus.setVisibility(View.VISIBLE);
                textViewStatus.setText("Order Completed");
                textViewStatusMessage.setText("This order has been completed");
                animationViewStatus.setAnimation(R.raw.completed);
                animationViewStatus.playAnimation();
                break;

            default:
                layoutStatus.setVisibility(View.VISIBLE);
                textViewOrderStatus.setText(order.getStatus().toString().replace("_", " "));
        }
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

                if (trip.getStatus() == TripStatus.PICKED_UP) {

                    setupLiveLocation();
                }

            } else if (trip.getType() == TripType.DELIVERY) {

                if (trip.getStatus() == TripStatus.STARTED) {

                    setupLiveLocation();
                }
            }
        }
    }

    private void setupRecyclerView() {

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

    private void getMerchant() {

        showLoadingAnimation();

        // get logged in user by phone and password from server
        Map<String, Object> data = new HashMap<>();
        data.put("phone_number", Session.getPhoneNumber(OrderActivity.this));
        data.put("password", Session.getPassword(OrderActivity.this));

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("merchant-verifyLogin")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        hideLoadingAnimation();

                        // saving new logged in user to session
                        Session.user = ParseUtils
                                .parseMerchant(httpsCallableResult.getData());

                        merchant = Session.user;

                        Session.user.registerObserver(this);

                        getOrderCustomer();
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Toast.makeText(OrderActivity.this,
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

                        hideLoadingAnimation();

                        customer = ParseUtils.parseCustomer(httpsCallableResult.getData());

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
                                && trip.getStatus() == TripStatus.ARRIVED_DESTINATION) {

                            Intent intent = new Intent(OrderActivity.this,
                                    ConfirmCollectedActivity.class);
                            intent.putExtra("order", GsonUtils.orderToGson(order));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            startActivity(intent);

                        } else if (order.getStatus() == OrderStatus.DELIVERING
                                && trip.getStatus() == TripStatus.ARRIVED_SOURCE) {

                            Intent intent = new Intent(OrderActivity.this,
                                    ConfirmPickupActivity.class);
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

    private void contactCustomer() {

        String phone = "0092" + customer.getPhoneNumber();
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

    private void showOrderStatusPopup() {

        Gson gson = new Gson();

        Bundle bundle = new Bundle();
        bundle.putString("current_status", gson.toJson(order.getStatus()));

        ChangeOrderStatusDialog dialog = new ChangeOrderStatusDialog();
        dialog.setArguments(bundle);
        dialog.setCancelable(true);
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "dialog_update_status");
    }

    @Override
    public void onOrderStatusUpdated(OrderStatus status) {

        order.setStatus(status);

        Map<String, Object> data = new HashMap<>();
        data.put("customer_id", order.getCustomerId());
        data.put("order", order.toJson());

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("order_task-changeOrderStatus")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    hideLoadingAnimation();

                    // change order status locally
                    for (int x = 0; x < Session.user.getLaundry().getOrders().size(); x++) {

                        if (Session.user
                                .getLaundry().getOrders().get(x)
                                .getId().equals(order.getId())) {

                            Session.user
                                    .getLaundry().getOrders()
                                    .get(x).setStatus(status);
                            break;
                        }
                    }

                    // notify observers
                    Session.user.notifyObservers("STATUS_CHANGED", order.getId(), status);

                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Toast.makeText(OrderActivity.this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_back) {

            goBack();

        } else if (view.getId() == R.id.button_contact) {

            contactCustomer();

        } else if (view.getId() == R.id.button_contact_driver) {

            contactDriver();

        } else if (view.getId() == R.id.button_change_status) {

            showOrderStatusPopup();

        } else if (view.getId() == R.id.button_pickup_order) {

            showPickupOrderActivity();

        } else if (view.getId() == R.id.button_cancel_order) {

            AlertDialog.Builder alert;
            alert = new AlertDialog.Builder(this);
            alert.setTitle("Cancel order for sure ?");

            alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                dialog.dismiss();
                cancelOrder();
            });

            alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

            alert.show();
        }
    }

    private void cancelOrder() {

        order.setStatus(OrderStatus.CANCELLED);

        Map<String, String> data = new HashMap<>();
        data.put("laundry_id", order.getLaundryId());
        data.put("customer_id", order.getCustomerId());
        data.put("order_id", order.getId());

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("order_task-cancelOrderByMerchant")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    hideLoadingAnimation();

                    // change order status locally
                    for (int x = 0; x < Session.user.getLaundry().getOrders().size(); x++) {

                        if (Session.user
                                .getLaundry().getOrders().get(x)
                                .getId().equals(order.getId())) {

                            Session.user
                                    .getLaundry().getOrders()
                                    .get(x).setStatus(OrderStatus.CANCELLED);
                            break;
                        }
                    }

                    buttonCancelOrder.setVisibility(View.GONE);

                    // notify observers
                    Session.user.notifyObservers("ORDER_CANCEL",
                            order.getId(), OrderStatus.CANCELLED);

                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Toast.makeText(OrderActivity.this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void showPickupOrderActivity() {

        Intent intent = new Intent(OrderActivity.this, PickupOrderActivity.class);
        intent.putExtra("order", GsonUtils.orderToGson(order));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

        goBack();
    }

    private void goBack() {

        // if there are no activities in the background
        if (isTaskRoot()) {

            Intent intent = new Intent(OrderActivity.this, DashboardActivity.class);
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

            Log.d("order_activity", "updateView: called");
            Log.d("order_activity", "updateView: " + Globals.orderInView);

            if (orderId.equals(order.getId())) {

                order.setStatus(status);

                if (task.equals("RIDER_REQUEST")) {

                    return;
                }

                if (status == OrderStatus.PICK_UP) {

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

                } else if (status == OrderStatus.DELIVERING) {

                    if (task.equals("ARRIVED_SOURCE")) {

                        runOnUiThread(() -> {

                            Intent intent = new Intent(OrderActivity.this, ConfirmPickupActivity.class);
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
        mMap.addMarker(merchantMarker);
    }

    private void setupMaps() {
        new FetchURL(OrderActivity.this)
                .execute(getUrl(driverMarker.getPosition(), merchantMarker.getPosition(),
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
            mMap.addMarker(merchantMarker);
            recenterMap();
        }
    }

    private void recenterMap() {

        if (mMap != null) {

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(driverMarker.getPosition());
            builder.include(merchantMarker.getPosition());
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