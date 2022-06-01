package com.laundromat.merchant.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.merchant.R;
import com.laundromat.merchant.model.DeliveryBoy;
import com.laundromat.merchant.model.Trip;
import com.laundromat.merchant.model.observers.IMerchantObserver;
import com.laundromat.merchant.model.order.Order;
import com.laundromat.merchant.model.order.OrderStatus;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.utils.GsonUtils;
import com.laundromat.merchant.utils.ParseUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class ConfirmPickupActivity extends AppCompatActivity
        implements View.OnClickListener, IMerchantObserver {

    // Variables
    private Trip trip;
    private Order order;
    private DeliveryBoy driver;

    // Views
    private RelativeLayout layoutLoading;

    private ImageButton buttonBack;
    private TextView textViewOrderId;

    private TextView textViewContactName;
    private TextView textViewContactPhone;
    private AppCompatButton buttonContact;

    private TextView textViewFarePrice;
    private TextView textViewPaymentMethod;

    private TextView textViewPickupCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_pickup);

        getIntentData();

        initViews();

        if (Session.user == null) {

            getMerchant();

        } else {

            Session.user.registerObserver(this);
            getTrip();
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

        this.textViewFarePrice = findViewById(R.id.text_view_pickup_fare);
        this.textViewPaymentMethod = findViewById(R.id.text_view_payment_method);

        this.textViewPickupCode = findViewById(R.id.text_view_pickup_code);

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        buttonContact = findViewById(R.id.button_contact);
        buttonContact.setOnClickListener(this);
    }

    private void setupViews() {

        textViewOrderId.setText(MessageFormat.format("Order ID: {0}",
                order.getId().substring(trip.getId().length() - 10)));

        textViewContactName.setText(driver.getFullName());

        textViewContactPhone.setText(driver.getPhoneNumber());

        textViewFarePrice.setText(MessageFormat.format("PKR {0}", trip.getCost()));
        textViewPaymentMethod.setText(order.getPaymentMethod().toString().replace("_", " "));

        textViewPickupCode.setText(order.getPickupCode());
    }

    private void getMerchant() {

        showLoadingAnimation();

        // get logged in user by phone and password from server
        Map<String, Object> data = new HashMap<>();
        data.put("phone_number", Session.getPhoneNumber(ConfirmPickupActivity.this));
        data.put("password", Session.getPassword(ConfirmPickupActivity.this));

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("merchant-verifyLogin")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        // saving new logged in user to session
                        Session.user = ParseUtils
                                .parseMerchant(httpsCallableResult.getData());

                        Session.user.registerObserver(ConfirmPickupActivity.this);

                        getTrip();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(ConfirmPickupActivity.this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("splash", "startSplash: " + e.getMessage());
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

                        getDriver();
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

            Intent intent = new Intent(ConfirmPickupActivity.this,
                    DashboardActivity.class);
            startActivity(intent);
            finish();

        } else {

            Intent intent = new Intent(ConfirmPickupActivity.this,
                    OrdersActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void updateView(String task, String orderId, OrderStatus status) {

        if (orderId.equals(order.getId())) {

            order.setStatus(status);

            if (task.equals("PICKED_UP")) {

                Intent intent = new Intent(ConfirmPickupActivity.this,
                        OrderActivity.class);
                intent.putExtra("order", GsonUtils.orderToGson(order));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
            }
        }
    }
}