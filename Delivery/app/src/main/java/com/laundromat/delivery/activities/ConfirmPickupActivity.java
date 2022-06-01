package com.laundromat.delivery.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

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
import com.laundromat.delivery.model.util.PaymentMethod;
import com.laundromat.delivery.model.util.TransactionType;
import com.laundromat.delivery.model.util.TripStatus;
import com.laundromat.delivery.model.util.TripType;
import com.laundromat.delivery.prefs.Session;
import com.laundromat.delivery.utils.GsonUtils;
import com.laundromat.delivery.utils.ParseUtils;
import com.laundromat.delivery.utils.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import ss.anoop.awesometextinputlayout.AwesomeTextInputLayout;

public class ConfirmPickupActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    // Variables
    private Trip trip;
    private Order order;
    private Merchant merchant;
    private Customer customer;

    // Views
    private RelativeLayout layoutLoading;

    private ImageButton buttonBack;
    private TextView textViewTripId;

    private TextView textViewContact;
    private TextView textViewContactName;
    private TextView textViewContactPhone;
    private AppCompatButton buttonContact;

    private RelativeLayout layoutOrderPrice;
    private TextView textViewOrderPrice;
    private TextView textViewFareTitle;
    private TextView textViewFarePrice;
    private TextView textViewPaymentMethod;

    private CardView cardViewPayment;
    private AwesomeTextInputLayout layoutOrderAmount;
    private EditText editTextOrderAmount;
    private EditText editTextFareAmount;

    private EditText editTextPickupCode;
    private TextView textViewAsk;

    private LinearLayout buttonConfirmPickup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_pickup);

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

        this.textViewTripId = findViewById(R.id.text_view_trip_id);

        this.textViewContact = findViewById(R.id.text_view_contact);
        this.textViewContactName = findViewById(R.id.text_view_contact_name);
        this.textViewContactPhone = findViewById(R.id.text_view_contact_phone);

        this.layoutOrderPrice = findViewById(R.id.layout_order_price);
        this.textViewOrderPrice = findViewById(R.id.text_view_order_price);
        this.textViewFareTitle = findViewById(R.id.text_view_fare_title);
        this.textViewFarePrice = findViewById(R.id.text_view_pickup_fare);
        this.textViewPaymentMethod = findViewById(R.id.text_view_payment_method);

        cardViewPayment = findViewById(R.id.card_view_payment);
        layoutOrderAmount = findViewById(R.id.layout_order_amount);
        editTextOrderAmount = findViewById(R.id.edit_text_order_amount);
        editTextFareAmount = findViewById(R.id.edit_text_fare_amount);

        editTextPickupCode = findViewById(R.id.edit_text_pickup_code);
        editTextPickupCode.addTextChangedListener(this);

        textViewAsk = findViewById(R.id.text_view_ask);

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        buttonContact = findViewById(R.id.button_contact);
        buttonContact.setOnClickListener(this);

        buttonConfirmPickup = findViewById(R.id.button_confirm_pickup);
        buttonConfirmPickup.setOnClickListener(this);
    }

    private void setupViews() {

        textViewTripId.setText(MessageFormat.format("Trip ID: {0}",
                trip.getId().substring(trip.getId().length() - 10)));

        textViewContact.setText(trip.getType() ==
                TripType.PICKUP ? "Customer Contact" : "Merchant Contact");

        textViewContactName.setText(trip.getType() ==
                TripType.PICKUP ? customer.getFullName() : merchant.getFullName());

        textViewContactPhone.setText(trip.getType() ==
                TripType.PICKUP ? customer.getPhoneNumber() : merchant.getPhoneNumber());


        if (trip.getType() == TripType.PICKUP) {

            textViewOrderPrice.setText(MessageFormat.format("PKR {0}", order.getPrice()));

        } else {

            layoutOrderPrice.setVisibility(View.GONE);
            layoutOrderAmount.setVisibility(View.GONE);
        }

        textViewFareTitle.setText(trip.getType()
                == TripType.PICKUP ? "Pickup Fare" : "Delivery Fare");

        textViewFarePrice.setText(MessageFormat.format("PKR {0}", trip.getCost()));
        textViewPaymentMethod.setText(order.getPaymentMethod().toString().replace("_", " "));

        textViewAsk.setText(trip.getType() == TripType.PICKUP
                ? "Note: Ask customer to provide the pickup code"
                : "Note: Ask merchant to provide the pickup code");

        if (trip.getType() == TripType.PICKUP
                && trip.getOrder().getPaymentMethod() == PaymentMethod.JAZZ_CASH) {

            cardViewPayment.setVisibility(View.GONE);

        } else {

            cardViewPayment.setVisibility(View.VISIBLE);
        }
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

    private void confirmPickup() {

        Log.d("payment_method",
                "confirmPickup: " + order.getPaymentMethod().toString());

        if (trip.getType() == TripType.PICKUP) {

            boolean filled = true;
            boolean valid = true;
            boolean pickupCodeValid = true;

            String orderAmount = editTextOrderAmount.getText().toString().trim();
            String fareAmount = editTextFareAmount.getText().toString().trim();
            String pickupCode = editTextPickupCode.getText().toString().trim();

            ////////////////////////////////////////////////////////////////////////
            boolean validated = false;
            boolean empty = false;

            // if payment method is cash
            if (order.getPaymentMethod() == PaymentMethod.CASH) {

                if (TextUtils.isEmpty(orderAmount) || TextUtils.isEmpty(fareAmount)) {

                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                String orderPriceString = MessageFormat.format("{0}", order.getPrice());

                if (!orderAmount.equals(orderPriceString)) {

                    Toast.makeText(this, "Invalid order amount entered", Toast.LENGTH_SHORT).show();
                    return;
                }

                String tripPriceString = MessageFormat.format("{0}", trip.getCost());

                if (!fareAmount.equals(tripPriceString)) {

                    Log.d("fare_amount", "confirmPickup invalid : entered: " + fareAmount + ", real: " + tripPriceString);
                    Toast.makeText(this, "Invalid fare amount entered", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("fare_amount", "confirmPickup: valid: entered: " + fareAmount + ", real: " + tripPriceString);
            }

            if (TextUtils.isEmpty(pickupCode)) {

                Toast.makeText(this, "Enter pickup code", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pickupCode.equals(order.getPickupCode())) {

                Toast.makeText(this, "Pickup code is invalid", Toast.LENGTH_SHORT).show();
                return;
            }

            // customer transaction1
            Transaction customerTransaction1 = new Transaction();
            customerTransaction1.setId(StringUtils.getRandomCode(20));
            customerTransaction1.setAmount(order.getPrice());
            customerTransaction1.setPaymentMethod(order.getPaymentMethod());
            customerTransaction1.setType(TransactionType.ORDER_PAYMENT);

            // customer transaction2
            Transaction customerTransaction2 = new Transaction();
            customerTransaction2.setId(StringUtils.getRandomCode(20));
            customerTransaction2.setAmount(trip.getCost());
            customerTransaction2.setPaymentMethod(order.getPaymentMethod());
            customerTransaction2.setType(TransactionType.PICKUP_FEE);

            // merchant transaction
            Transaction merchantTransaction = new Transaction();
            merchantTransaction.setId(StringUtils.getRandomCode(20));
            merchantTransaction.setAmount(order.getPrice());
            merchantTransaction.setPaymentMethod(order.getPaymentMethod());
            merchantTransaction.setType(TransactionType.EARNING);

            // driver transaction
            Transaction driverTransaction = new Transaction();
            driverTransaction.setId(StringUtils.getRandomCode(20));
            driverTransaction.setAmount(trip.getCost());
            driverTransaction.setPaymentMethod(order.getPaymentMethod());
            driverTransaction.setType(TransactionType.EARNING);

            Map<String, Object> data = new HashMap<>();
            data.put("trip_id", trip.getId());
            data.put("trip_type", trip.getType().toString());
            data.put("customer_id", customer.getId());
            data.put("merchant_id", merchant.getId());
            data.put("driver_id", Session.user.getId());
            data.put("order", trip.getOrder().toJson());
            data.put("customer_transaction1", customerTransaction1.toJson());
            data.put("customer_transaction2", customerTransaction2.toJson());
            data.put("merchant_transaction", merchantTransaction.toJson());
            data.put("driver_transaction", driverTransaction.toJson());

            showLoadingAnimation();

            // update in cloud
            FirebaseFunctions
                    .getInstance()
                    .getHttpsCallable("trip_task-confirmPickedUp")
                    .call(data)
                    .addOnSuccessListener(httpsCallableResult -> {

                        hideLoadingAnimation();

                        // change trip status locally
                        for (int x = 0; x < Session.user.getTrips().size(); x++) {

                            if (Session.user.getTrips().get(x).getId().equals(trip.getId())) {

                                Session.user.getTrips().get(x).setStatus(TripStatus.PICKED_UP);

                                break;
                            }
                        }

                        // save transaction locally
                        Session.user.getTransactions().add(driverTransaction);

                        trip.setStatus(TripStatus.PICKED_UP);

                        // notify observers
                        Session.user.notifyObservers("STATUS_CHANGED",
                                trip.getId(), trip.getStatus());

                        // go back to trip activity
                        Intent intent = new Intent(ConfirmPickupActivity.this,
                                TripActivity.class);
                        intent.putExtra("trip", GsonUtils.tripToGson(trip));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {

                        hideLoadingAnimation();

                        Toast.makeText(ConfirmPickupActivity.this,
                                e.getMessage(), Toast.LENGTH_SHORT).show();

                        Log.d("trip", "acceptTripRequest: " + e.getMessage());
                    });
        } else {

            String fareAmount = editTextFareAmount.getText().toString().trim();
            String pickupCode = editTextPickupCode.getText().toString().trim();

            if (TextUtils.isEmpty(fareAmount)) {

                Toast.makeText(this,
                        "All Fields are required", Toast.LENGTH_SHORT).show();

                return;
            }

            if (TextUtils.isEmpty(pickupCode)) {

                Toast.makeText(this,
                        "Pickup code is required", Toast.LENGTH_SHORT).show();

                return;
            }

            String tripPriceString = MessageFormat.format("{0}", trip.getCost());

            if (!fareAmount.equals(tripPriceString)
                    && order.getPaymentMethod() == PaymentMethod.CASH) {

                Log.d("fare_amount", "confirmPickup invalid : entered: " + fareAmount + ", real: " + tripPriceString);
                Toast.makeText(this,
                        "Invalid fare amount entered", Toast.LENGTH_SHORT).show();

                return;
            }

            Log.d("fare_amount", "confirmPickup valid : entered: " + fareAmount + ", real: " + tripPriceString);

            if (!pickupCode.equals(order.getPickupCode())) {

                Toast.makeText(this,
                        "Pickup code is invalid", Toast.LENGTH_SHORT).show();

                return;
            }

            // merchant transaction
            Transaction merchantTransaction = new Transaction();
            merchantTransaction.setId(StringUtils.getRandomCode(20));
            merchantTransaction.setAmount(trip.getCost());
            merchantTransaction.setPaymentMethod(order.getPaymentMethod());
            merchantTransaction.setType(TransactionType.DELIVERY_FEE);

            // driver transaction
            Transaction driverTransaction = new Transaction();
            driverTransaction.setId(StringUtils.getRandomCode(20));
            driverTransaction.setAmount(trip.getCost());
            driverTransaction.setPaymentMethod(order.getPaymentMethod());
            driverTransaction.setType(TransactionType.EARNING);

            Map<String, Object> data = new HashMap<>();
            data.put("trip_id", trip.getId());
            data.put("trip_type", trip.getType().toString());
            data.put("customer_id", customer.getId());
            data.put("merchant_id", merchant.getId());
            data.put("driver_id", Session.user.getId());
            data.put("order", trip.getOrder().toJson());
            data.put("merchant_transaction", merchantTransaction.toJson());
            data.put("driver_transaction", driverTransaction.toJson());

            showLoadingAnimation();

            // update in cloud
            FirebaseFunctions
                    .getInstance()
                    .getHttpsCallable("trip_task-confirmPickedUp")
                    .call(data)
                    .addOnSuccessListener(httpsCallableResult -> {

                        hideLoadingAnimation();

                        // change trip status locally
                        for (int x = 0; x < Session.user.getTrips().size(); x++) {

                            if (Session.user.getTrips().get(x).getId().equals(trip.getId())) {

                                Session.user.getTrips().get(x).setStatus(TripStatus.PICKED_UP);

                                break;
                            }
                        }

                        // save transaction locally
                        Session.user.getTransactions().add(driverTransaction);

                        trip.setStatus(TripStatus.PICKED_UP);

                        // notify observers
                        Session.user.notifyObservers("STATUS_CHANGED",
                                trip.getId(), trip.getStatus());

                        // go back to trip activity
                        Intent intent = new Intent(ConfirmPickupActivity.this,
                                TripActivity.class);
                        intent.putExtra("trip", GsonUtils.tripToGson(trip));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {

                        hideLoadingAnimation();

                        Toast.makeText(ConfirmPickupActivity.this,
                                e.getMessage(), Toast.LENGTH_SHORT).show();

                        Log.d("trip", "acceptTripRequest: " + e.getMessage());
                    });
        }
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

            confirmPickup();
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
                    TripsActivity.class);
            startActivity(intent);
            finish();
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

        if (editTextPickupCode.getText().hashCode() == editable.hashCode()) {

            text = editTextPickupCode.getText().toString().trim();

            if (text.length() != 5) {

                editTextPickupCode.setError("Code must be 5 characters long");
            }
        }
    }
}