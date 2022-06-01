package com.laundromat.merchant.activities;

import android.content.DialogInterface;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.merchant.R;
import com.laundromat.merchant.model.Customer;
import com.laundromat.merchant.model.Merchant;
import com.laundromat.merchant.model.observers.IMerchantObserver;
import com.laundromat.merchant.model.order.Order;
import com.laundromat.merchant.model.order.OrderItem;
import com.laundromat.merchant.model.order.OrderStatus;
import com.laundromat.merchant.model.order.SaleItem;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.viewholders.OrderSummarySection;
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

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class OrderRequestActivity extends AppCompatActivity
        implements View.OnClickListener, IMerchantObserver {

    // Variables
    private Merchant merchant = Session.user;
    private Order order;
    private Customer customer;

    // Views
    private ImageButton buttonBack;
    private TextView textViewLaundryName;
    private RelativeLayout layoutLoading;
    private RelativeLayout layoutDecline;
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
    private AppCompatButton buttonAccept;
    private AppCompatButton buttonDecline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_request);

        getIntentData();

        initViews();

        if (merchant == null) {

            getMerchant();

        } else {

            Session.user.registerObserver(this);

            if (checkOrderRequestHandled()) {

                Log.d("handled", "onCreate: true");

                Intent intent = new Intent(OrderRequestActivity.this,
                        OrderActivity.class);
                intent.putExtra("order", GsonUtils.orderToGson(order));

                startActivity(intent);
                finish();

            } else {

                getOrderCustomer();
            }
        }
    }

    private void getIntentData() {

        String orderGson = getIntent().getStringExtra("order");
        order = GsonUtils.gsonToOrder(orderGson);
    }

    private void initViews() {

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        layoutDecline = findViewById(R.id.layout_request_declined);
        layoutDecline.setVisibility(View.GONE);

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
        imageViewLocation = findViewById(R.id.image_view_location);
        textViewLocationDistance = findViewById(R.id.text_view_location_distance);
        textViewLocationAddress = findViewById(R.id.text_view_location_address);
        textViewCustomerName = findViewById(R.id.text_view_customer_name);
        textViewCustomerPhone = findViewById(R.id.text_view_customer_phone);

        buttonContact = findViewById(R.id.button_contact);
        buttonContact.setOnClickListener(this);

        buttonAccept = findViewById(R.id.button_accept);
        buttonAccept.setOnClickListener(this);

        buttonDecline = findViewById(R.id.button_decline);
        buttonDecline.setOnClickListener(this);
    }

    private void setupViews() {

        textViewLaundryName.setText(Session.user.getLaundry().getName());
        textViewOrderId.setText(order.getId().substring(order.getId().length() - 10));
        textViewDateCreated.setText(order.getDateCreated());
        textViewQuantity.setText(String.valueOf(order.getItemsQuantity()));
        textViewPrice.setText(MessageFormat.format("PKR {0}", (order.getPrice() + order.getDiscount())));
        textViewDiscount.setText(MessageFormat.format("PKR {0}", order.getDiscount()));
        textViewPayable.setText(MessageFormat.format("PKR {0}", order.getPrice()));
        textViewPaymentMethod.setText(order.getPaymentMethod().toString().replace("_", " "));

        setupRecyclerView();

        String locationUrl = StringUtils.getMapsStaticImageUrl(this,
                customer.getLocation());
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

    private boolean checkOrderRequestHandled() {

        // check if the order to be showed is already accepted/declined
        // means we didn't opened the order from notification but from inside the app
        // and changed its status. so view the order
        for (Order searchOrder : merchant.getLaundry().getOrders()) {

            if (searchOrder.getId().equals(order.getId())) {

                if (searchOrder.getStatus() == OrderStatus.ACCEPTED
                        || searchOrder.getStatus() == OrderStatus.DECLINED) {

                    return true;
                }
            }
        }

        return false;
    }

    private void getMerchant() {

        showLoadingAnimation();

        // get logged in user by phone and password from server
        Map<String, Object> data = new HashMap<>();
        data.put("phone_number", Session.getPhoneNumber(OrderRequestActivity.this));
        data.put("password", Session.getPassword(OrderRequestActivity.this));

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

                        if (checkOrderRequestHandled()) {

                            Log.d("handled", "onCreate 2: true");

                            Intent intent = new Intent(OrderRequestActivity.this,
                                    OrderActivity.class);
                            intent.putExtra("order", GsonUtils.orderToGson(order));

                            startActivity(intent);
                            finish();

                        } else {

                            getOrderCustomer();
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Toast.makeText(OrderRequestActivity.this,
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

                        setupViews();
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Log.d("checkout", "placeOrder: " + e.getMessage());

                });
    }

    private void declineOrderRequest() {

        AlertDialog.Builder alert;
        alert = new AlertDialog.Builder(this);
        alert.setTitle("Decline order request ?");

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
                        .getHttpsCallable("order_task-declineOrderRequest")
                        .call(data)
                        .addOnSuccessListener(httpsCallableResult -> {

                            hideLoadingAnimation();

                            // change order status locally
                            for (int x = 0; x < Session.user.getLaundry().getOrders().size(); x++) {

                                if (Session.user.getLaundry()
                                        .getOrders().get(x).getId().equals(order.getId())) {

                                    Session.user.getLaundry()
                                            .getOrders().get(x).setStatus(OrderStatus.DECLINED);
                                    break;
                                }
                            }

                            // notify observers
                            Session.user.notifyObservers("ORDER_DECLINE", order.getId(), OrderStatus.DECLINED);

                        })
                        .addOnFailureListener(e -> {

                            hideLoadingAnimation();

                            Toast.makeText(OrderRequestActivity.this,
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

    private void acceptOrderRequest() {

        AlertDialog.Builder alert;
        alert = new AlertDialog.Builder(this);
        alert.setTitle("Accept order request ?");

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

                // change order status to accepted and notify customer, then update view
                FirebaseFunctions
                        .getInstance()
                        .getHttpsCallable("order_task-acceptOrderRequest")
                        .call(data)
                        .addOnSuccessListener(httpsCallableResult -> {

                            hideLoadingAnimation();

                            // change order status locally
                            for (int x = 0; x < Session.user.getLaundry().getOrders().size(); x++) {

                                if (Session.user.getLaundry()
                                        .getOrders().get(x).getId().equals(order.getId())) {

                                    Session.user.getLaundry()
                                            .getOrders().get(x).setStatus(OrderStatus.ACCEPTED);
                                    break;
                                }
                            }

                            // notify observers
                            Session.user.notifyObservers("ORDER_ACCEPT", order.getId(),
                                    OrderStatus.ACCEPTED);
                        })
                        .addOnFailureListener(e -> {

                            hideLoadingAnimation();

                            Toast.makeText(OrderRequestActivity.this,
                                    e.getMessage(), Toast.LENGTH_SHORT).show();

                            Log.d("accept_order", "onClick: " + e.getMessage());
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

    private void contactCustomer() {

        String phone = "0092" + customer.getPhoneNumber();
        Intent intent = new Intent(Intent.ACTION_DIAL,
                Uri.fromParts("tel", phone, null));
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_back) {

            goBack();

        } else if (view.getId() == R.id.button_contact) {

            contactCustomer();

        } else if (view.getId() == R.id.button_accept) {

            acceptOrderRequest();

        } else if (view.getId() == R.id.button_decline) {

            declineOrderRequest();
        }
    }

    private void goBack() {

        // if there are no activities in the background
        if (isTaskRoot()) {

            startActivity(new
                    Intent(OrderRequestActivity.this, DashboardActivity.class));
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

    private void hideLoadingAnimation() {

        layoutLoading.setVisibility(View.GONE);
        layoutLoading.animate().translationY(layoutLoading.getHeight());
    }

    @Override
    public void updateView(String task, String orderId, OrderStatus status) {

        if (orderId.equals(order.getId())) {

            order.setStatus(status);

            if (status == OrderStatus.DECLINED) {

                runOnUiThread(this::showDeclineLayout);

            } else {

                runOnUiThread(() -> {

                    Intent intent = new Intent(OrderRequestActivity.this, OrderActivity.class);
                    intent.putExtra("order", GsonUtils.orderToGson(order));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);
                });
            }
        }
    }
}