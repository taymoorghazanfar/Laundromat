package com.laundromat.delivery.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.delivery.R;
import com.laundromat.delivery.model.Customer;
import com.laundromat.delivery.model.Merchant;
import com.laundromat.delivery.model.Transaction;
import com.laundromat.delivery.model.Trip;
import com.laundromat.delivery.model.order.Order;
import com.laundromat.delivery.model.order.OrderItem;
import com.laundromat.delivery.model.order.SaleItem;
import com.laundromat.delivery.model.util.TransactionType;
import com.laundromat.delivery.model.util.TripStatus;
import com.laundromat.delivery.model.util.TripType;
import com.laundromat.delivery.prefs.Session;
import com.laundromat.delivery.ui.viewholders.OrderSummarySection;
import com.laundromat.delivery.utils.GsonUtils;
import com.laundromat.delivery.utils.ParseUtils;
import com.laundromat.delivery.utils.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class ConfirmDeliveryActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    // Variables
    private Trip trip;
    private Order order;
    private Merchant merchant;
    private Customer customer;

    // Views
    private RelativeLayout layoutLoading;
    private RelativeLayout layoutCompleted;
    private TextView textViewEarning;

    private ImageButton buttonBack;
    private TextView textViewTripId;

    private TextView textViewContact;
    private TextView textViewContactName;
    private TextView textViewContactPhone;
    private AppCompatButton buttonContact;

    private RecyclerView recyclerViewOrderSummary;
    private SectionedRecyclerViewAdapter adapter;

    private EditText editTextDeliveryCode;
    private TextView textViewAsk;

    private LinearLayout buttonConfirmDelivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_delivery);

        getIntentData();

        initViews();

        getMerchant();
    }

    private void getIntentData() {

        String tripGson = getIntent().getStringExtra("trip");
        trip = GsonUtils.gsonToTrip(tripGson);

        order = trip.getOrder();
    }

    private void initViews() {

        this.layoutLoading = findViewById(R.id.layout_loading);
        this.layoutCompleted = findViewById(R.id.layout_completed);
        this.textViewEarning = findViewById(R.id.text_view_earning);

        this.textViewTripId = findViewById(R.id.text_view_trip_id);

        this.textViewContact = findViewById(R.id.text_view_contact);
        this.textViewContactName = findViewById(R.id.text_view_contact_name);
        this.textViewContactPhone = findViewById(R.id.text_view_contact_phone);

        this.recyclerViewOrderSummary = findViewById(R.id.recycler_view_order_summary);

        editTextDeliveryCode = findViewById(R.id.edit_text_delivery_code);
        editTextDeliveryCode.addTextChangedListener(this);

        textViewAsk = findViewById(R.id.text_view_ask);

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        buttonContact = findViewById(R.id.button_contact);
        buttonContact.setOnClickListener(this);

        buttonConfirmDelivery = findViewById(R.id.button_confirm_pickup);
        buttonConfirmDelivery.setOnClickListener(this);
    }

    private void setupViews() {

        this.layoutLoading.setVisibility(View.GONE);
        this.layoutCompleted.setVisibility(View.GONE);

        textViewTripId.setText(MessageFormat.format("Trip ID: {0}",
                trip.getId().substring(trip.getId().length() - 10)));

        textViewContact.setText(trip.getType() ==
                TripType.PICKUP ? "Merchant Contact" : "Customer Contact");

        textViewContactName.setText(trip.getType() ==
                TripType.PICKUP ? merchant.getFullName() : customer.getFullName());

        textViewContactPhone.setText(trip.getType() ==
                TripType.PICKUP ? merchant.getPhoneNumber() : customer.getPhoneNumber());

        textViewAsk.setText(trip.getType() == TripType.PICKUP
                ? "Note: Ask merchant to provide the delivery code"
                : "Note: Ask customer to provide the delivery code");

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

    private void getMerchant() {

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("merchant-getMerchantByLaundryId")
                .call(order.getLaundryId())
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        hideLoadingAnimation();

                        merchant = ParseUtils.parseMerchant(httpsCallableResult.getData());

                        getCustomer();
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Log.d("checkout", "placeOrder: " + e.getMessage());

                });
    }

    private void getCustomer() {

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

    private void confirmDelivery() {

        String deliveryCode = editTextDeliveryCode.getText().toString().trim();

        if (TextUtils.isEmpty(deliveryCode)) {

            Toast.makeText(this,
                    "All Fields are required", Toast.LENGTH_SHORT).show();

            return;
        }

        if (!deliveryCode.equals(order.getDeliveryCode())) {

            Toast.makeText(this,
                    "Delivery code is invalid", Toast.LENGTH_SHORT).show();

            return;
        }

        // add trip completed date
        trip.setDateFinished(StringUtils.getCurrentDateTime());

        Map<String, Object> data = new HashMap<>();
        data.put("trip_id", trip.getId());
        data.put("trip_type", trip.getType().toString());
        data.put("trip_date_completed", trip.getDateFinished());
        data.put("customer_id", customer.getId());
        data.put("merchant_id", merchant.getId());
        data.put("driver_id", Session.user.getId());
        data.put("order", trip.getOrder().toJson());

        showLoadingAnimation();

        // update in cloud
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("trip_task-confirmDelivery")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    hideLoadingAnimation();

                    // change trip status locally
                    for (int x = 0; x < Session.user.getTrips().size(); x++) {

                        if (Session.user.getTrips().get(x).getId().equals(trip.getId())) {

                            Session.user.getTrips().get(x).setStatus(TripStatus.COMPLETED);
                            Session.user.getTrips().get(x).setDateFinished(trip.getDateFinished());

                            break;
                        }
                    }

                    trip.setStatus(TripStatus.COMPLETED);

                    // notify observers
                    Session.user.notifyObservers("STATUS_CHANGED",
                            trip.getId(), trip.getStatus());

                    // show completed layout
                    showCompletedLayout();
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Toast.makeText(ConfirmDeliveryActivity.this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();

                    Log.d("trip", "acceptTripRequest: " + e.getMessage());
                });
    }

    private void dialPhone() {

        String phone = trip.getType() == TripType.PICKUP
                ? customer.getPhoneNumber() : merchant.getPhoneNumber();

        phone = "0092" + phone;

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

        } else if (view.getId() == R.id.button_confirm_pickup) {

            confirmDelivery();
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

    private void showCompletedLayout() {

        textViewEarning.setText(MessageFormat.format("PKR {0}", trip.getCost()));
        layoutCompleted.setVisibility(View.VISIBLE);
        layoutCompleted.bringToFront();
        layoutCompleted.animate().translationY(0);
    }

    private void goBack() {

        if (trip.getStatus() == TripStatus.COMPLETED) {

            Intent intent = new Intent(ConfirmDeliveryActivity.this,
                    TripsActivity.class);
            startActivity(intent);
            finish();

        } else if (isTaskRoot()) {

            Intent intent = new Intent(ConfirmDeliveryActivity.this,
                    DashboardActivity.class);
            startActivity(intent);
            finish();

        } else {

            Intent intent = new Intent(ConfirmDeliveryActivity.this,
                    TripActivity.class);
            intent.putExtra("trip", GsonUtils.tripToGson(trip));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

        String text = "";

        if (editTextDeliveryCode.getText().hashCode() == editable.hashCode()) {

            text = editTextDeliveryCode.getText().toString().trim();

            if (text.length() != 5) {

                editTextDeliveryCode.setError("Code must be 5 characters long");
            }
        }
    }
}