package com.laundromat.customer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.laundromat.customer.R;
import com.laundromat.customer.model.Customer;
import com.laundromat.customer.model.Laundry;
import com.laundromat.customer.model.order.Order;
import com.laundromat.customer.model.order.OrderItem;
import com.laundromat.customer.model.order.OrderStatus;
import com.laundromat.customer.model.order.SaleItem;
import com.laundromat.customer.model.util.Cart;
import com.laundromat.customer.model.util.Location;
import com.laundromat.customer.model.util.PaymentMethod;
import com.laundromat.customer.prefs.Session;
import com.laundromat.customer.ui.viewholders.OrderSummarySection;
import com.laundromat.customer.utils.GsonUtils;
import com.laundromat.customer.utils.LocationUtils;
import com.laundromat.customer.utils.NumberUtils;
import com.laundromat.customer.utils.ParseUtils;
import com.laundromat.customer.utils.StringUtils;
import com.laundromat.customer.utils.TimeUtils;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class CheckoutActivity extends AppCompatActivity
        implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    // Variables
    private Customer customer = Session.user;
    private Cart cart = customer.getCart();
    private boolean isCod = false;
    private double discountGiven;
    private double discount;

    // Views
    private ImageButton buttonBack;
    private TextView textViewLaundryName;
    private ImageView imageViewLocation;
    private TextView textViewLocationName;
    private TextView textViewLocationAddress;
    private RadioGroup radioGroupPaymentMethod;
    private RecyclerView recyclerViewOrderSummary;
    private SectionedRecyclerViewAdapter adapter;
    private TextView textViewTotal;
    private TextView textViewPrice;
    private TextView textViewDiscountGiven;
    private AppCompatButton buttonPlaceOrder;
    private RelativeLayout layoutLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();

        initRecyclerView();
    }

    private void initViews() {

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        textViewLaundryName = findViewById(R.id.text_view_name);
        textViewLaundryName.setText(cart.getLaundry().getName());

        imageViewLocation = findViewById(R.id.image_view_location);
        String locationUrl = StringUtils.getMapsStaticImageUrl(this, customer.getLocation());
        Picasso.get()
                .load(locationUrl)
                .into(imageViewLocation);

        textViewLocationName = findViewById(R.id.text_view_location_name);
        String locationName = null;
        for (Location location : customer.getLocations()) {

            if (location.getLatLng().latitude == customer.getLocation().latitude
                    && location.getLatLng().longitude == customer.getLocation().longitude) {

                locationName = location.getName();
            }
        }
        textViewLocationName.setText(locationName);

        textViewLocationAddress = findViewById(R.id.text_view_location_address);
        String locationAddress = LocationUtils.getAddressFromLatLng(this,
                customer.getLocation().latitude, customer.getLocation().longitude);
        textViewLocationAddress.setText(locationAddress);

        radioGroupPaymentMethod = findViewById(R.id.radio_group_payment_method);
        radioGroupPaymentMethod.setOnCheckedChangeListener(this);

        //calculate discount
        discount = NumberUtils.getDiscount(cart.getLaundry().getDiscount(), cart.getPrice());
        discountGiven = cart.getPrice() - discount;

        textViewDiscountGiven = findViewById(R.id.text_view_discount_given);
        textViewDiscountGiven.setText(MessageFormat
                .format("Discount: PKR {0}", discount));

        textViewTotal = findViewById(R.id.text_view_total);
        textViewTotal.setText(MessageFormat.format("Total: PKR {0}", cart.getPrice()));

        textViewPrice = findViewById(R.id.text_view_price);
        textViewPrice.setText(MessageFormat.format("PKR {0}", discountGiven));

        buttonPlaceOrder = findViewById(R.id.button_place_order);
        buttonPlaceOrder.setOnClickListener(this);

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);
    }

    private void initRecyclerView() {

        recyclerViewOrderSummary = findViewById(R.id.recycler_view_order_summary);
        recyclerViewOrderSummary.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SectionedRecyclerViewAdapter();

        for (final Map.Entry<String, OrderItem> entry : cart.getOrderItems().entrySet()) {

            if (entry.getValue().getSaleItems().size() > 0) {

                List<SaleItem> saleItems = new ArrayList<>(entry.getValue().getSaleItems().values());

                adapter.addSection(
                        new OrderSummarySection(entry.getKey(), saleItems));
            }
        }

        recyclerViewOrderSummary.setAdapter(adapter);
    }

    private void placeOrder() {

        // check if laundry is still open
        Laundry laundry = cart.getLaundry();

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String currentTime = dateFormat.format(new Date());

        String openingTime = laundry.getTimings().getOpeningTime();
        String closingTime = laundry.getTimings().getClosingTime();

        boolean laundryOpen;

        try {

            laundryOpen = TimeUtils.isTimeBetweenTwoTime(openingTime, closingTime, currentTime);

            if (!laundryOpen) {

                AlertDialog.Builder alert;
                alert = new AlertDialog.Builder(this);
                alert.setTitle("The laundry is currently closed");
                alert.setCancelable(false);

                alert.setPositiveButton("OK", (dialog, whichButton) -> {

                    dialog.dismiss();
                });

                alert.show();

            } else {

                if (radioGroupPaymentMethod.getCheckedRadioButtonId() == -1) {

                    Toast.makeText(this,
                            "Please select a payment method", Toast.LENGTH_SHORT).show();
                    return;
                }

                // check if laundry is active

                showLoadingAnimation();

                FirebaseFunctions
                        .getInstance()
                        .getHttpsCallable("laundry-getLaundryById")
                        .call(laundry.getId())
                        .addOnSuccessListener(httpsCallableResult -> {

                            if (httpsCallableResult.getData() != null) {

                                Laundry gotLaundry = ParseUtils.parseLaundry(httpsCallableResult.getData());
                                boolean isActive = gotLaundry.isActive();

                                if (isActive) {

                                    // creating a new order object
                                    Order order = new Order();
                                    order.setCustomerId(customer.getId());
                                    order.setLaundryId(cart.getLaundry().getId());
                                    order.setLaundryName(cart.getLaundry().getName());
                                    order.setItems(cart.getOrderItems());
                                    order.setStatus(OrderStatus.REQUESTED);
                                    order.setDeliveryLocation(customer.getLocation());
                                    order.setItemsQuantity(cart.getTotalSaleItems());
                                    order.setPrice(discountGiven);
                                    order.setDiscount(discount);
                                    order.setPaymentMethod(isCod ? PaymentMethod.CASH : PaymentMethod.JAZZ_CASH);
                                    order.setPickupCode(StringUtils.getRandomCode(5));
                                    order.setDeliveryCode(StringUtils.getRandomCode(5));

                                    // send order request to merchant
                                    FirebaseFunctions
                                            .getInstance()
                                            .getHttpsCallable("order_task-sendOrderRequest")
                                            .call(order.toJson())
                                            .addOnSuccessListener(httpsCallableResult2 -> {

                                                if (httpsCallableResult2.getData() != null) {

                                                    hideLoadingAnimation();

                                                    String orderId = (String) httpsCallableResult2.getData();
                                                    order.setId(orderId);

                                                    // get deep copy of order
                                                    Gson gson = new Gson();
                                                    Order orderCopy = gson.fromJson(gson.toJson(order), Order.class);

                                                    customer.getOrders().add(orderCopy);

                                                    cart.clearCart(CheckoutActivity.this);

                                                    Intent intent = new Intent(
                                                            CheckoutActivity.this, OrderActivity.class);
                                                    intent.putExtra("order", GsonUtils.orderToGson(orderCopy));
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                }
                                            })
                                            .addOnFailureListener(e -> {

                                                hideLoadingAnimation();
                                                Log.d("checkout", "placeOrder: " + e.getMessage());
                                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                } else {

                                    hideLoadingAnimation();

                                    AlertDialog.Builder alert;
                                    alert = new AlertDialog.Builder(this);
                                    alert.setTitle("The laundry is currently closed");
                                    alert.setCancelable(false);

                                    alert.setPositiveButton("OK", (dialog, whichButton) -> {

                                        dialog.dismiss();
                                    });

                                    alert.show();
                                }
                            }
                        })
                        .addOnFailureListener(e -> {

                            hideLoadingAnimation();
                            Log.d("checkout", "placeOrder: " + e.getMessage());
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_back) {

            finish();

        } else if (view.getId() == R.id.button_place_order) {

            AlertDialog.Builder alert;
            alert = new AlertDialog.Builder(this);
            alert.setTitle("Place order now ?");

            alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                dialog.dismiss();
                placeOrder();
            });

            alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

            alert.show();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int id) {

        if (id == R.id.radio_button_cod) {

            isCod = true;

        } else if (id == R.id.radio_button_jazz_cash) {

            isCod = false;
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
}