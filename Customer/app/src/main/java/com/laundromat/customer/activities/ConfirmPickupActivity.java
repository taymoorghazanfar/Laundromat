package com.laundromat.customer.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.customer.R;
import com.laundromat.customer.model.DeliveryBoy;
import com.laundromat.customer.model.Trip;
import com.laundromat.customer.model.observers.IOrderObserver;
import com.laundromat.customer.model.order.Order;
import com.laundromat.customer.model.order.OrderStatus;
import com.laundromat.customer.model.util.PaymentMethod;
import com.laundromat.customer.prefs.CartPrefs;
import com.laundromat.customer.prefs.Session;
import com.laundromat.customer.utils.GsonUtils;
import com.laundromat.customer.utils.ParseUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class ConfirmPickupActivity extends AppCompatActivity
        implements View.OnClickListener, IOrderObserver {

    // Constants
    private static final int REQUEST_CODE_ORDER = 111;
    private static final int REQUEST_CODE_FARE = 222;

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

    private TextView textViewOrderPrice;
    private TextView textViewFarePrice;
    private TextView textViewPaymentMethod;

    private CardView cardViewPickupCode;
    private TextView textViewPickupCode;

    // payment views
    private CardView cardViewOrderPayment;
    private LinearLayout layoutOrderPayment;
    private LinearLayout layoutOrderPayed;
    private TextView textViewOrderAmount;
    private LinearLayout buttonPayOrder;

    private CardView cardViewFarePayment;
    private LinearLayout layoutFarePayment;
    private LinearLayout layoutFarePayed;
    private TextView textViewFareAmount;
    private LinearLayout buttonPayFare;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_pickup);

        getIntentData();

        initViews();

        if (Session.user == null) {

            getCustomer();

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

        this.textViewOrderPrice = findViewById(R.id.text_view_order_price);
        this.textViewFarePrice = findViewById(R.id.text_view_pickup_fare);
        this.textViewPaymentMethod = findViewById(R.id.text_view_payment_method);

        this.cardViewPickupCode = findViewById(R.id.card_view_pickup_code);
        this.textViewPickupCode = findViewById(R.id.text_view_pickup_code);

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        buttonContact = findViewById(R.id.button_contact);
        buttonContact.setOnClickListener(this);

        // payment views
        cardViewOrderPayment = findViewById(R.id.card_view_order_payment);
        layoutOrderPayment = findViewById(R.id.layout_order_payment);
        layoutOrderPayed = findViewById(R.id.layout_order_payed);
        textViewOrderAmount = findViewById(R.id.text_view_order_price_jazz);
        buttonPayOrder = findViewById(R.id.button_order_pay);

        cardViewFarePayment = findViewById(R.id.card_view_fare_payment);
        layoutFarePayment = findViewById(R.id.layout_fare_payment);
        layoutFarePayed = findViewById(R.id.layout_fare_payed);
        textViewFareAmount = findViewById(R.id.text_view_fare_price_jazz);
        buttonPayFare = findViewById(R.id.button_fare_pay);
    }

    private void setupViews() {

        textViewOrderId.setText(MessageFormat.format("Order ID: {0}",
                order.getId().substring(trip.getId().length() - 10)));

        textViewContactName.setText(driver.getFullName());

        textViewContactPhone.setText(driver.getPhoneNumber());

        textViewOrderPrice.setText(MessageFormat.format("PKR {0}", order.getPrice()));
        textViewFarePrice.setText(MessageFormat.format("PKR {0}", trip.getCost()));
        textViewPaymentMethod.setText(order.getPaymentMethod().toString().replace("_", " "));

        textViewPickupCode.setText(order.getPickupCode());

        if (order.getPaymentMethod() == PaymentMethod.JAZZ_CASH) {

            Log.d("jazz", "setupViews: jazz");

            cardViewOrderPayment.setVisibility(View.VISIBLE);
            textViewOrderAmount.setText(MessageFormat.format("PKR {0}", order.getPrice()));
            buttonPayOrder.setOnClickListener(this);

            cardViewFarePayment.setVisibility(View.VISIBLE);
            textViewFareAmount.setText(MessageFormat.format("PKR {0}", trip.getCost()));
            buttonPayFare.setOnClickListener(this);

            if (!order.isPayed() || !trip.isPayed()) {

                cardViewPickupCode.setVisibility(View.GONE);
            }

            if (!order.isPayed()) {

                layoutOrderPayment.setVisibility(View.VISIBLE);
                layoutOrderPayed.setVisibility(View.GONE);

            } else {

                layoutOrderPayment.setVisibility(View.GONE);
                layoutOrderPayed.setVisibility(View.VISIBLE);
            }

            if (!trip.isPayed()) {

                layoutFarePayment.setVisibility(View.VISIBLE);
                layoutFarePayed.setVisibility(View.GONE);

            } else {

                layoutFarePayment.setVisibility(View.GONE);
                layoutFarePayed.setVisibility(View.VISIBLE);
            }
        } else {

            cardViewOrderPayment.setVisibility(View.GONE);
            cardViewFarePayment.setVisibility(View.GONE);
        }
    }

    private void getCustomer() {

        showLoadingAnimation();

        // get logged in user by phone and password from server
        Map<String, Object> data = new HashMap<>();
        data.put("phone_number", Session.getPhoneNumber(ConfirmPickupActivity.this));
        data.put("password", Session.getPassword(ConfirmPickupActivity.this));

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("customer-verifyLogin")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        // saving new logged in user to session
                        Session.user = ParseUtils
                                .parseCustomer(httpsCallableResult.getData());

                        Session.user.registerObserver(ConfirmPickupActivity.this);

                        // get saved cart
                        if (CartPrefs.get(ConfirmPickupActivity.this) != null) {

                            Session.user.setCart(CartPrefs.get(ConfirmPickupActivity.this));
                        }

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

    private void payOrderJazzCash() {

        Intent intent = new Intent(ConfirmPickupActivity.this, JazzCashActivity.class);
        intent.putExtra("price", String.valueOf(order.getPrice()));
        startActivityForResult(intent, REQUEST_CODE_ORDER);
    }

    private void payFareJazzCash() {

        Intent intent = new Intent(ConfirmPickupActivity.this, JazzCashActivity.class);
        intent.putExtra("price", String.valueOf(trip.getCost()));
        startActivityForResult(intent, REQUEST_CODE_FARE);
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

        } else if (view.getId() == R.id.button_order_pay) {

            payOrderJazzCash();

        } else if (view.getId() == R.id.button_fare_pay) {

            payFareJazzCash();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ORDER && resultCode == RESULT_OK) {

            if (data != null) {

                String responseCode = data.getStringExtra("pp_ResponseCode");

                if (responseCode != null) {

                    // if payment was successful
                    if (responseCode.equals("000")) {

                        order.setPayed(true);

                        showLoadingAnimation();

                        // update payment status in cloud
                        FirebaseFunctions
                                .getInstance()
                                .getHttpsCallable("payment-setOrderPayed")
                                .call(order.getId())
                                .addOnSuccessListener(httpsCallableResult -> {

                                    if (httpsCallableResult.getData() != null) {

                                        hideLoadingAnimation();

                                        layoutOrderPayment.setVisibility(View.GONE);
                                        layoutOrderPayed.setVisibility(View.VISIBLE);

                                        if (order.isPayed() && trip.isPayed()) {

                                            cardViewPickupCode.setVisibility(View.VISIBLE);
                                        }

                                        // save order locally
                                        for (int x = 0; x < Session.user.getOrders().size(); x++) {

                                            if (Session.user.getOrders().get(x).getId().equals(order.getId())) {

                                                Session.user.getOrders().get(x).setPayed(true);
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {

                                    hideLoadingAnimation();

                                    Log.d("checkout", "placeOrder: " + e.getMessage());

                                });
                    } else {

                        Toast.makeText(this, "Order payment failed. Try again later", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    Toast.makeText(this, "Order payment failed. Try again later", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == REQUEST_CODE_FARE && resultCode == RESULT_OK) {

            if (data != null) {

                String responseCode = data.getStringExtra("pp_ResponseCode");

                if (responseCode != null) {

                    // if payment was successful
                    if (responseCode.equals("000")) {

                        trip.setPayed(true);

                        showLoadingAnimation();

                        // update payment status in cloud
                        FirebaseFunctions
                                .getInstance()
                                .getHttpsCallable("payment-setTripPayed")
                                .call(trip.getId())
                                .addOnSuccessListener(httpsCallableResult -> {

                                    if (httpsCallableResult.getData() != null) {

                                        hideLoadingAnimation();

                                        layoutFarePayment.setVisibility(View.GONE);
                                        layoutFarePayed.setVisibility(View.VISIBLE);

                                        if (order.isPayed() && trip.isPayed()) {

                                            cardViewPickupCode.setVisibility(View.VISIBLE);
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {

                                    hideLoadingAnimation();

                                    Log.d("checkout", "placeOrder: " + e.getMessage());

                                });
                    } else {

                        Toast.makeText(this, "Trip fare payment failed. Try again later", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    Toast.makeText(this, "Trip fare payment failed. Try again later", Toast.LENGTH_SHORT).show();
                }
            }
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
                    LaundriesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

        } else {

            Intent intent = new Intent(ConfirmPickupActivity.this,
                    OrdersActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
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