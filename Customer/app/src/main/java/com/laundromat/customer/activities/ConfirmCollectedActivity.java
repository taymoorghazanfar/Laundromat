package com.laundromat.customer.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.customer.R;
import com.laundromat.customer.model.DeliveryBoy;
import com.laundromat.customer.model.observers.IOrderObserver;
import com.laundromat.customer.model.order.Order;
import com.laundromat.customer.model.order.OrderItem;
import com.laundromat.customer.model.order.OrderStatus;
import com.laundromat.customer.model.order.SaleItem;
import com.laundromat.customer.prefs.Session;
import com.laundromat.customer.ui.viewholders.OrderSummarySection;
import com.laundromat.customer.utils.GsonUtils;
import com.laundromat.customer.utils.ParseUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class ConfirmCollectedActivity extends AppCompatActivity
        implements View.OnClickListener, IOrderObserver {

    // Variables
    private Order order;
    private DeliveryBoy driver;

    // Views
    private RelativeLayout layoutLoading;

    private ImageButton buttonBack;
    private TextView textViewOrderId;

    private TextView textViewContactName;
    private TextView textViewContactPhone;
    private AppCompatButton buttonContact;

    private RecyclerView recyclerViewOrderSummary;
    private SectionedRecyclerViewAdapter adapter;

    private TextView textViewDeliveryCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_collected);

        getIntentData();

        initViews();

        if (Session.user == null) {

            getCustomer();

        } else {

            Session.user.registerObserver(this);
            getDriver();
        }
    }

    private void getIntentData() {

        String orderGson = getIntent().getStringExtra("order");
        order = GsonUtils.gsonToOrder(orderGson);
    }

    private void initViews() {

        this.layoutLoading = findViewById(R.id.layout_loading);

        this.textViewOrderId = findViewById(R.id.text_view_order_id);

        this.textViewContactName = findViewById(R.id.text_view_contact_name);
        this.textViewContactPhone = findViewById(R.id.text_view_contact_phone);

        this.recyclerViewOrderSummary = findViewById(R.id.recycler_view_order_summary);

        textViewDeliveryCode = findViewById(R.id.text_view_delivery_code);

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        buttonContact = findViewById(R.id.button_contact);
        buttonContact.setOnClickListener(this);
    }

    private void setupViews() {

        textViewOrderId.setText(MessageFormat.format("Order ID: {0}",
                order.getId().substring(order.getId().length() - 10)));

        textViewContactName.setText(driver.getFullName());

        textViewContactPhone.setText(MessageFormat
                .format("0092{0}", driver.getPhoneNumber()));

        textViewDeliveryCode.setText(order.getDeliveryCode());

        setupRecyclerView();
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

    private void getCustomer() {

        showLoadingAnimation();

        // get logged in user by phone and password from server
        Map<String, Object> data = new HashMap<>();
        data.put("phone_number", Session.getPhoneNumber(ConfirmCollectedActivity.this));
        data.put("password", Session.getPassword(ConfirmCollectedActivity.this));

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("customer-verifyLogin")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        // saving new logged in user to session
                        Session.user = ParseUtils
                                .parseCustomer(httpsCallableResult.getData());

                        Session.user.registerObserver(ConfirmCollectedActivity.this);
                        getDriver();

                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(ConfirmCollectedActivity.this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("splash", "startSplash: " + e.getMessage());
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

                        setupViews();
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Log.d("checkout", "placeOrder: " + e.getMessage());

                });
    }

    private void dialPhone() {

        String phone = "0092" + driver.getPhoneNumber();

        Intent intent = new Intent(Intent.ACTION_DIAL,
                Uri.fromParts("tel", phone, null));
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_back) {

            goBack();

        } else if (view.getId() == R.id.button_contact) {

            dialPhone();
        }
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

    private void hideLoadingAnimation() {

        layoutLoading.setVisibility(View.GONE);
        layoutLoading.animate().translationY(layoutLoading.getHeight());
    }

    private void goBack() {

        if (isTaskRoot()) {

            Intent intent = new Intent(ConfirmCollectedActivity.this,
                    LaundriesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

        } else {

            Intent intent = new Intent(ConfirmCollectedActivity.this,
                    OrdersActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
        }
    }

    @Override
    public void updateView(String task, String orderId, OrderStatus status) {

        order.setStatus(status);

        Intent intent = new Intent(ConfirmCollectedActivity.this,
                OrderActivity.class);
        intent.putExtra("order", GsonUtils.orderToGson(order));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
    }
}