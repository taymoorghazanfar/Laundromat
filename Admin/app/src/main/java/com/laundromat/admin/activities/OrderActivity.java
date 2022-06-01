package com.laundromat.admin.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.admin.R;
import com.laundromat.admin.model.Customer;
import com.laundromat.admin.model.DeliveryBoy;
import com.laundromat.admin.model.Merchant;
import com.laundromat.admin.model.Trip;
import com.laundromat.admin.model.order.Order;
import com.laundromat.admin.model.order.OrderItem;
import com.laundromat.admin.model.order.OrderStatus;
import com.laundromat.admin.model.order.SaleItem;
import com.laundromat.admin.model.util.TripStatus;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.viewholders.OrderSummarySection;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.LocationUtils;
import com.laundromat.admin.utils.ParseUtils;
import com.laundromat.admin.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class OrderActivity extends AppCompatActivity
        implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    // Variables
    private Order order;
    private Customer customer;
    private DeliveryBoy driver;
    private Merchant merchant;
    private Trip trip;

    // Views
    private SwipeRefreshLayout swipeRefreshLayout;
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
    private AppCompatButton buttonContactCustomer;

    private TextView textViewMerchantName;
    private TextView textViewMerchantPhone;
    private AppCompatButton buttonContactMerchant;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        getIntentData();

        initViews();

        getMerchant();
    }

    private void getIntentData() {

        String orderGson = getIntent().getStringExtra("order");
        order = GsonUtils.gsonToOrder(orderGson);
    }

    private void initViews() {

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

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
        buttonContactCustomer = findViewById(R.id.button_contact_customer);
        buttonContactCustomer.setOnClickListener(this);

        textViewMerchantName = findViewById(R.id.text_view_merchant_name);
        textViewMerchantPhone = findViewById(R.id.text_view_merchant_phone);
        buttonContactMerchant = findViewById(R.id.button_contact_merchant);
        buttonContactMerchant.setOnClickListener(this);

        animationViewStatus = findViewById(R.id.animation_status);
        textViewStatus = findViewById(R.id.text_view_order_status);
        textViewStatusMessage = findViewById(R.id.text_view_message);

        layoutDriver = findViewById(R.id.layout_driver_info);
        imageViewDriver = findViewById(R.id.image_view_driver);
        textViewDriverName = findViewById(R.id.text_view_driver_name);
        textViewDriverPhone = findViewById(R.id.text_view_driver_phone);
        textViewPlateNumber = findViewById(R.id.text_view_plate_number);
        textViewVehicle = findViewById(R.id.text_view_vehicle);
        textViewVehicleColor = findViewById(R.id.text_view_vehicle_color);

        buttonContactDriver = findViewById(R.id.button_contact_driver);
        buttonContactDriver.setOnClickListener(this);
    }

    private void setupViews() {

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
                merchant.getLaundry().getLocation());
        String distance = locationDistance + " KM";
        textViewLocationDistance.setText(distance);

        String locationAddress = LocationUtils.getAddressFromLatLng(this,
                order.getDeliveryLocation().latitude,
                order.getDeliveryLocation().longitude);
        textViewLocationAddress.setText(locationAddress);

        textViewCustomerName.setText(customer.getFullName());
        textViewCustomerPhone.setText(MessageFormat.format("+92{0}",
                customer.getPhoneNumber()));

        textViewMerchantName.setText(merchant.getFullName());
        textViewMerchantPhone.setText(MessageFormat.format("+92{0}",
                merchant.getPhoneNumber()));

        // footers
        layoutDriver.setVisibility(View.GONE);

        switch (order.getStatus()) {

            case REQUESTED:
                textViewStatus.setText("Requested");
                textViewStatusMessage.setText("This order has been requested by customer");
                animationViewStatus.setAnimation(R.raw.requested);
                animationViewStatus.playAnimation();
                break;

            case CANCELLED:
                textViewStatus.setText("Cancelled");
                textViewStatusMessage.setText("This order has been cancelled");
                animationViewStatus.setAnimation(R.raw.cancelled);
                animationViewStatus.playAnimation();
                break;

            case DECLINED:
                textViewStatus.setText("Declined");
                textViewStatusMessage.setText("This order request has been declined");
                animationViewStatus.setAnimation(R.raw.declined);
                animationViewStatus.playAnimation();
                break;

            case ACCEPTED:
                textViewStatus.setText("Accepted");
                textViewStatusMessage.setText("This order request has been accepted");
                animationViewStatus.setAnimation(R.raw.accepted);
                animationViewStatus.playAnimation();
                break;

            case PICKUP_REQUESTED:

                textViewStatus.setText("Pickup Requested");
                textViewStatusMessage.setText("This order request has been requested for pickup");
                animationViewStatus.setAnimation(R.raw.requested);
                animationViewStatus.playAnimation();
                break;

            case PICKUP_ACCEPTED:
                textViewStatus.setText("Pickup Accepted");
                textViewStatusMessage.setText("This order has been accepted for pickup");
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

                        textViewStatus.setText("Pickup Started");
                        textViewStatusMessage.setText("Rider is approaching for pickup");
                        animationViewStatus.setAnimation(R.raw.delivering);
                        animationViewStatus.playAnimation();

                    } else if (trip.getStatus() == TripStatus.ARRIVED_SOURCE) {

                        textViewStatus.setText("Arrived For Pickup");
                        textViewStatusMessage.setText("Rider has arrived for pickup");
                        animationViewStatus.setAnimation(R.raw.delivering);
                        animationViewStatus.playAnimation();

                    } else if (trip.getStatus() == TripStatus.PICKED_UP) {

                        textViewStatus.setText("Picked Up");
                        textViewStatusMessage.setText("Rider has picked up the order");
                        animationViewStatus.setAnimation(R.raw.delivering);
                        animationViewStatus.playAnimation();
                    }
                }
                break;

            case COLLECTED:
                textViewStatus.setText("Collected");
                textViewStatusMessage.setText("This order has been collected");
                animationViewStatus.setAnimation(R.raw.delivering);
                animationViewStatus.playAnimation();
                break;

            case IN_SERVICE:
                textViewStatus.setText("Washing");
                textViewStatusMessage.setText("This order is currently in service");
                animationViewStatus.setAnimation(R.raw.delivering);
                animationViewStatus.playAnimation();
                break;

            case WASHED:
                textViewStatus.setText("Washed");
                textViewStatusMessage.setText("Order items have been washed");
                animationViewStatus.setAnimation(R.raw.delivering);
                animationViewStatus.playAnimation();
                break;

            case DELIVERY_REQUESTED:
                textViewStatus.setText("Delivery Requested");
                textViewStatusMessage.setText("This order has been requested for delivery");
                animationViewStatus.setAnimation(R.raw.requested);
                animationViewStatus.playAnimation();
                break;

            case DELIVERY_ACCEPTED:
                textViewStatus.setText("Delivery Request Accepted");
                textViewStatusMessage.setText("This order has been accepted for delivery");
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
                        textViewStatus.setText("Delivering");
                        textViewStatusMessage.setText("This order is currently being delivered");
                        animationViewStatus.setAnimation(R.raw.delivering);
                        animationViewStatus.playAnimation();
                        layoutDriver.setVisibility(View.VISIBLE);

                    } else if (trip.getStatus() == TripStatus.PICKED_UP
                            || trip.getStatus() == TripStatus.ARRIVED_DESTINATION) {

                        textViewStatus.setText("Delivering");
                        textViewStatusMessage.setText("This order is currently being delivered");
                        animationViewStatus.setAnimation(R.raw.delivering);
                        animationViewStatus.playAnimation();
                        layoutDriver.setVisibility(View.VISIBLE);
                    }
                }
                break;

            case COMPLETED:
                textViewStatus.setText("Order Completed");
                textViewStatusMessage.setText("This order has been completed");
                animationViewStatus.setAnimation(R.raw.completed);
                animationViewStatus.playAnimation();
                break;
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

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("merchant-getMerchantByLaundryId")
                .call(order.getLaundryId())
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        hideLoadingAnimation();

                        merchant = ParseUtils
                                .parseMerchant(httpsCallableResult.getData());

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

                        setupViews();
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

    private void getOrder() {

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("order_task-getOrderById")
                .call(order.getId())
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        order = ParseUtils.parseOrder(httpsCallableResult.getData());

                        // save locally
                        for (int x = 0; x < Session.user.getOrders().size(); x++) {

                            if (Session.user.getOrders().get(x).getId().equals(order.getId())) {

                                Session.user.getOrders().set(x, order);
                                break;
                            }
                        }

                        getMerchant();
                    }
                })
                .addOnFailureListener(e -> {

                    Log.d("customer_fcm", "fcm: " + e.getMessage());
                    hideLoadingAnimation();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
    public void onRefresh() {

        getOrder();
        swipeRefreshLayout.setRefreshing(false);
    }
}