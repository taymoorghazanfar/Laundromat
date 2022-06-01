package com.laundromat.customer.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.customer.R;
import com.laundromat.customer.model.Laundry;
import com.laundromat.customer.model.util.Cart;
import com.laundromat.customer.model.util.FareData;
import com.laundromat.customer.model.washable.WashableItem;
import com.laundromat.customer.model.washable.WashableItemCategory;
import com.laundromat.customer.prefs.CartPrefs;
import com.laundromat.customer.prefs.Session;
import com.laundromat.customer.ui.decorators.SpacesItemDecoration;
import com.laundromat.customer.ui.interfaces.IMenuItemClickListener;
import com.laundromat.customer.ui.interfaces.ITopReachedListener;
import com.laundromat.customer.ui.viewholders.MenuSection;
import com.laundromat.customer.utils.DateUtils;
import com.laundromat.customer.utils.GsonUtils;
import com.laundromat.customer.utils.LocationUtils;
import com.laundromat.customer.utils.NumberUtils;
import com.laundromat.customer.utils.ParseUtils;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class LaundryProfileActivity extends AppCompatActivity
        implements IMenuItemClickListener, View.OnClickListener, ITopReachedListener {

    // Constants
    private static final int REQUEST_CODE_MENU_ITEM = 111;
    private static final int REQUEST_CODE_CART = 222;

    // Variables
    Laundry laundry;
    Cart cart = Session.user.getCart();
    FareData fareData;

    // Views
    private RelativeLayout layoutLoading;
    private CircleImageView imageViewLogo;
    private TextView textViewDiscount;
    private TextView textViewHomeBased;
    private TextView textViewName;
    private TextView textViewDistance;
    private TextView textViewTiming;
    private TextView textViewDeliveryTime;
    private RecyclerView recyclerViewMenu;
    private ImageButton buttonBack;
    private ImageButton buttonLocation;
    private RelativeLayout layoutCheckoutButton;
    private RelativeLayout buttonCheckout;
    private TextView textViewCheckoutQuantity;
    private TextView textViewCheckoutPrice;
    private TextView textViewDiscountGiven;
    private TextView textViewFee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laundry_profile);

        getIntentData();
        initViews();
        initRecyclerView();
        getFareData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // save cart to session
        CartPrefs.set(LaundryProfileActivity.this, cart);
    }

    private void getIntentData() {

        String laundryGson = getIntent().getStringExtra("laundry");

        laundry = GsonUtils.gsonToLaundry(laundryGson);
    }

    @SuppressLint("NewApi")
    private void initViews() {

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        imageViewLogo = findViewById(R.id.image_view_logo);
        Picasso.get()
                .load(laundry.getLogoUrl())
                .into(imageViewLogo);

        // discount
        textViewDiscount = findViewById(R.id.text_view_discount);
        textViewDiscount.setText(MessageFormat
                .format("{0}% Discount", laundry.getDiscount()));

        textViewDiscount.setVisibility(laundry.getDiscount() == 0 ? View.GONE : View.VISIBLE);

        textViewHomeBased = findViewById(R.id.text_view_home_based);
        textViewHomeBased.setVisibility(laundry.isHomeBased() ? View.VISIBLE : View.GONE);

        textViewName = findViewById(R.id.text_view_name);
        textViewName.setText(laundry.getName());

        textViewDistance = findViewById(R.id.text_view_distance);
        textViewDistance.setText(MessageFormat.format("{0} KM",
                LocationUtils
                        .getDistanceBetweenTwoPoints(Session.user.getLocation(), laundry.getLocation())));

        textViewTiming = findViewById(R.id.text_view_timing);
        textViewTiming.setText(MessageFormat.format("{0} - {1}",
                laundry.getTimings().getOpeningTime(), laundry.getTimings().getClosingTime()));

        textViewDeliveryTime = findViewById(R.id.text_view_delivery_time);
        textViewDeliveryTime.setText
                (MessageFormat.format("{0} Day(s)", DateUtils.getLaundryDeliveryDuration
                        (laundry.getOrders())));

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        buttonLocation = findViewById(R.id.button_location);
        buttonLocation.setOnClickListener(this);

        layoutCheckoutButton = findViewById(R.id.layout_checkout_button);
        layoutCheckoutButton.setOnClickListener(this);

        buttonCheckout = findViewById(R.id.button_proceed);
        buttonCheckout.setOnClickListener(this);

        textViewCheckoutQuantity = findViewById(R.id.text_view_quantity);
        textViewCheckoutPrice = findViewById(R.id.text_view_price);
        textViewDiscountGiven = findViewById(R.id.text_view_discount_given);

        textViewFee = findViewById(R.id.text_view_fare);
    }

    private void initRecyclerView() {

        recyclerViewMenu = findViewById(R.id.recycler_view_menu);
        SectionedRecyclerViewAdapter adapter = new SectionedRecyclerViewAdapter();

        Map<String, List<WashableItem>> menuData = new HashMap<>();

        for (WashableItemCategory menuCategory : laundry.getMenu()) {

            menuData.put(menuCategory.getTitle(), menuCategory.getWashableItems());
        }

        // set values to adapter
        for (final Map.Entry<String, List<WashableItem>> entry : menuData.entrySet()) {

            if (entry.getValue().size() > 0) {

                adapter.addSection(
                        new MenuSection(
                                this, entry.getKey(), entry.getValue(),
                                this, this));
            }
        }

        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        this.recyclerViewMenu.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        recyclerViewMenu.setAdapter(adapter);
    }

    private void updateCheckoutButton() {

        if (!cart.isEmpty() && cart.getLaundry().getName().equals(laundry.getName())) {

            int totalSaleItems = cart.getTotalSaleItems();
            double totalPrice = cart.getPrice();

            textViewCheckoutQuantity.setText(MessageFormat.format(
                    "Checkout ({0} Items)", totalSaleItems));

            //todo: set discount given
            double discount = NumberUtils.getDiscount(laundry.getDiscount(), totalPrice);
            double discountGiven = totalPrice - discount;
            textViewDiscountGiven.setText(MessageFormat
                    .format("Discount: PKR {0}", discount));

            //todo: deduct discount from price
            textViewCheckoutPrice.setText(MessageFormat.format("PKR {0}", discountGiven));

            layoutCheckoutButton.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutCheckoutButton.bringToFront();
            layoutCheckoutButton.requestLayout();

        } else {

            layoutCheckoutButton.getLayoutParams().height = 0;
            layoutCheckoutButton.requestLayout();
        }
    }

    private void getFareData() {

        showLoadingAnimation();

        Map<String, Object> data = new HashMap<>();
        data.put("customer_latitude", Session.user.getLocation().latitude);
        data.put("customer_longitude", Session.user.getLocation().longitude);
        data.put("laundry_latitude", laundry.getLocation().latitude);
        data.put("laundry_longitude", laundry.getLocation().longitude);

        //todo: get distance, base fare, per km
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("admin-getFareData")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        hideLoadingAnimation();

                        fareData = ParseUtils
                                .parseFareData(httpsCallableResult.getData());

                        // setup fee text view
                        double fee = fareData.getBaseFare() + (fareData.getDistance() * fareData.getPerKm());
                        textViewFee.setText(MessageFormat.format("PKR {0} Fee", fee));

                        updateCheckoutButton();
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Log.d("checkout", "placeOrder: " + e.getMessage());

                });
    }

    @Override
    public void onMenuItemClick(WashableItem menuItem) {

        Intent intent = new Intent(LaundryProfileActivity.this, MenuItemActivity.class);
        intent.putExtra("laundry", laundry);
        intent.putExtra("item", menuItem);
        startActivityForResult(intent, REQUEST_CODE_MENU_ITEM);
    }

    @Override
    public void onTopReached(int position) {
//
//        if (position == 0) {
//
//            showInfoBar();
//
//        } else {
//
//            hideInfoBar();
//        }
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_back) {

            finish();

        } else if (view.getId() == R.id.button_proceed) {

            startActivityForResult(new Intent(
                            LaundryProfileActivity.this, CartActivity.class),
                    REQUEST_CODE_CART);

        } else if (view.getId() == R.id.button_location) {

            LocationUtils.showLocation(this, laundry.getName(), laundry.getLocation());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MENU_ITEM) {

            updateCheckoutButton();

        } else if (requestCode == REQUEST_CODE_CART) {

            updateCheckoutButton();
        }
    }

    private void showLoadingAnimation() {

        layoutCheckoutButton.getLayoutParams().height = 0;
        layoutCheckoutButton.requestLayout();

        layoutLoading.setVisibility(View.VISIBLE);
        layoutLoading.bringToFront();
        layoutLoading.animate().translationY(0);
    }

    private void hideLoadingAnimation() {

        layoutLoading.setVisibility(View.GONE);
        layoutLoading.animate().translationY(layoutLoading.getHeight());
        updateCheckoutButton();
    }
}