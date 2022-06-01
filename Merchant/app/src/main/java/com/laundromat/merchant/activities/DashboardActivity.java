package com.laundromat.merchant.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.laundromat.merchant.R;
import com.laundromat.merchant.model.Transaction;
import com.laundromat.merchant.model.observers.IMerchantObserver;
import com.laundromat.merchant.model.order.Order;
import com.laundromat.merchant.model.order.OrderStatus;
import com.laundromat.merchant.model.util.TransactionType;
import com.laundromat.merchant.model.washable.WashableItemCategory;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.adapters.CurrentOrdersRecyclerAdapter;
import com.laundromat.merchant.ui.decorators.SpacesItemDecoration;
import com.laundromat.merchant.ui.interfaces.ICurrentOrderClickListener;
import com.laundromat.merchant.utils.GsonUtils;
import com.laundromat.merchant.utils.LocationUtils;
import com.laundromat.merchant.utils.TimeUtils;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity
        implements View.OnClickListener, IMerchantObserver,
        ICurrentOrderClickListener, CompoundButton.OnCheckedChangeListener {

    // variables
    boolean checkSwitchStatus = false;

    // Views
    private Toolbar toolbar;
    private RelativeLayout layoutLoading;
    private DrawerLayout drawer;

    private ImageView imageViewLaundry;
    private TextView textViewLaundryName;
    private TextView textViewLocationAddress;
    private TextView textViewStatus;
    private SwitchCompat switchAvailable;

    private CardView cardViewOrders;
    private CardView cardViewTransactions;
    private CardView cardViewMenu;

    private TextView textViewOrders;
    private TextView textViewEarnings;
    private TextView textViewProducts;

    private TextView textViewCurrentOrders;
    private RecyclerView recyclerViewCurrentOrders;
    private CurrentOrdersRecyclerAdapter adapter;
    private TextView textViewEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (Session.user != null) {

            Session.user.registerObserver(this);
        }

        initViews();

        setupViews();
    }

    private void initViews() {

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        textViewEmpty = findViewById(R.id.text_view_empty);
        textViewEmpty.setVisibility(View.GONE);

        // setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initDrawer();

        initDrawerMenu();

        imageViewLaundry = findViewById(R.id.image_view_laundry);
        textViewLaundryName = findViewById(R.id.text_view_laundry_name);
        textViewLocationAddress = findViewById(R.id.text_view_location_address);
        textViewStatus = findViewById(R.id.text_view_status);
        switchAvailable = findViewById(R.id.switch_available);

        cardViewOrders = findViewById(R.id.card_view_orders);
        cardViewOrders.setOnClickListener(this);

        cardViewTransactions = findViewById(R.id.card_view_transactions);
        cardViewTransactions.setOnClickListener(this);

        cardViewMenu = findViewById(R.id.card_view_menu);
        cardViewMenu.setOnClickListener(this);

        textViewOrders = findViewById(R.id.text_view_total_orders);
        textViewEarnings = findViewById(R.id.text_view_earning);
        textViewProducts = findViewById(R.id.text_view_total_products);

        textViewCurrentOrders = findViewById(R.id.text_view_current_orders);
        recyclerViewCurrentOrders = findViewById(R.id.recycler_view_current_orders);
        recyclerViewCurrentOrders.setLayoutManager(new LinearLayoutManager(this));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        this.recyclerViewCurrentOrders.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
    }

    private void initDrawer() {

        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void initDrawerMenu() {

        TextView textViewName = findViewById(R.id.text_view_user_name);
        TextView textViewPhoneNumber = findViewById(R.id.text_view_phone_number);

        textViewName.setText(Session.user.getFullName());
        textViewPhoneNumber.setText(MessageFormat.format("+92{0}",
                Session.user.getPhoneNumber()));

        View optionHome = findViewById(R.id.option_dashboard);
        optionHome.setOnClickListener(this);

        View optionOrders = findViewById(R.id.option_orders);
        optionOrders.setOnClickListener(this);

        View optionMenu = findViewById(R.id.option_menu);
        optionMenu.setOnClickListener(this);

        View optionTransactions = findViewById(R.id.option_transactions);
        optionTransactions.setOnClickListener(this);

        View optionLogout = findViewById(R.id.option_logout);
        optionLogout.setOnClickListener(this);

        View optionProfile = findViewById(R.id.option_profile);
        optionProfile.setOnClickListener(this);
    }

    private void setupViews() {

        Picasso.get()
                .load(Session.user.getLaundry().getLogoUrl())
                .into(imageViewLaundry);

        textViewLaundryName.setText(Session.user.getLaundry().getName());

        String location = LocationUtils.getAddressFromLatLng(this,
                Session.user.getLaundry().getLocation().latitude,
                Session.user.getLaundry().getLocation().longitude);

        checkSwitchStatus = false;
        switchAvailable.setChecked(Session.user.getLaundry().isActive());
        switchAvailable.setOnCheckedChangeListener(this);
        checkSwitchStatus = true;

        textViewLocationAddress.setText(location);

        shopClock();

        getTotalOrders();

        getTotalEarnings();

        getTotalProducts();

        setupRecyclerView();
    }

    private void getTotalOrders() {

        textViewOrders.setText(MessageFormat.format("{0}\nOrders",
                Session.user.getLaundry().getOrders().size()));
    }

    @SuppressLint("SetTextI18n")
    private void getTotalEarnings() {

        double earnings = 0;

        for (Transaction transaction : Session.user.getTransactions()) {

            if (transaction.getType() == TransactionType.EARNING) {

                earnings += transaction.getAmount();
            }
        }

        textViewEarnings.setText("PKR " + earnings + "\nEarnings");
    }

    private void getTotalProducts() {

        int total = 0;

        for (WashableItemCategory category : Session.user.getLaundry().getMenu()) {

            total += category.getWashableItems().size();
        }

        textViewProducts.setText(MessageFormat
                .format("{0}\nProducts", total));
    }

    private void setupRecyclerView() {

        List<Order> orders = getCurrentOrders();

        adapter = new CurrentOrdersRecyclerAdapter(orders);
        adapter.setCurrentOrderClickListener(this);
        recyclerViewCurrentOrders.setAdapter(adapter);

        textViewCurrentOrders.setText(MessageFormat
                .format("Current Orders ({0})", orders.size()));

        textViewEmpty.setVisibility(orders.size() > 0 ? View.GONE : View.VISIBLE);
    }

    private ArrayList<Order> getCurrentOrders() {

        ArrayList<Order> orders = new ArrayList<>();

        for (Order order : Session.user.getLaundry().getOrders()) {

            if (order.getStatus() != OrderStatus.CANCELLED
                    && order.getStatus() != OrderStatus.DECLINED
                    && order.getStatus() != OrderStatus.COMPLETED) {

                // deep copy
                Gson gson = new Gson();
                orders.add(gson.fromJson(gson.toJson(order), Order.class));
            }
        }

        // sort the list by most recent order
        Collections.sort(orders, new Comparator<Order>() {

            final DateFormat f = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

            @Override
            public int compare(Order o1, Order o2) {
                try {
                    return f.parse(o2.getDateCreated()).compareTo(f.parse(o1.getDateCreated()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });

        return orders;
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.option_dashboard) {

            if (drawer.isDrawerOpen(GravityCompat.START)) {

                drawer.closeDrawer(GravityCompat.START);
            }

        } else if (view.getId() == R.id.option_orders) {

            startActivity(new Intent(DashboardActivity.this, OrdersActivity.class));
            finish();

        } else if (view.getId() == R.id.option_menu) {

            startActivity(new Intent(DashboardActivity.this, MenuCategoryActivity.class));
            finish();

        } else if (view.getId() == R.id.option_transactions) {

            startActivity(new Intent(DashboardActivity.this, TransactionsActivity.class));
            finish();

        } else if (view.getId() == R.id.option_profile) {

            startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
            finish();

        } else if (view.getId() == R.id.option_logout) {

            AlertDialog.Builder alert;
            alert = new AlertDialog.Builder(this);
            alert.setTitle("Logout ?");

            alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                dialog.dismiss();

                if (drawer.isDrawerOpen(GravityCompat.START)) {

                    drawer.closeDrawer(GravityCompat.START);
                }

                Map<String, Object> data = new HashMap<>();
                data.put("laundry_id", Session.user.getLaundry().getId());
                data.put("status", false);

                showLoadingAnimation();

                FirebaseFunctions
                        .getInstance()
                        .getHttpsCallable("laundry-setAvailability")
                        .call(data)
                        .addOnSuccessListener(httpsCallableResult -> {

                            hideLoadingAnimation();

                            Session.destroy(DashboardActivity.this);

                            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                            finish();

                        })
                        .addOnFailureListener(e -> {

                            hideLoadingAnimation();

                            Toast.makeText(DashboardActivity.this,
                                    e.getMessage(), Toast.LENGTH_SHORT).show();

                            Log.d("laundry", "acceptTripRequest: " + e.getMessage());
                        });
            });

            alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

            alert.show();

        } else if (view.getId() == R.id.card_view_orders) {

            startActivity(new Intent(DashboardActivity.this, OrdersActivity.class));
            finish();

        } else if (view.getId() == R.id.card_view_transactions) {

            startActivity(new Intent(DashboardActivity.this, TransactionsActivity.class));
            finish();

        } else if (view.getId() == R.id.card_view_menu) {

            startActivity(new Intent(DashboardActivity.this, MenuCategoryActivity.class));
            finish();
        }
    }

    @Override
    public void onCurrentOrderClick(Order order) {

        Intent intent;

        if (order.getStatus() != OrderStatus.REQUESTED) {

            intent = new Intent(DashboardActivity.this, OrderActivity.class);

        } else {

            intent = new Intent(DashboardActivity.this, OrderRequestActivity.class);

        }
        intent.putExtra("order", GsonUtils.orderToGson(order));
        startActivity(intent);
        finish();
    }

    private void shopClock() {
        final Handler hander = new Handler();
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            hander.post(() -> {

                checkShopTimings();
                shopClock();
            });
        }).start();
    }

    void checkShopTimings() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String currentTime = dateFormat.format(new Date());
        String openingTime = Session.user.getLaundry().getTimings().getOpeningTime();
        String closingTime = Session.user.getLaundry().getTimings().getClosingTime();

        try {
            boolean laundryOpen = TimeUtils.isTimeBetweenTwoTime(
                    openingTime, closingTime, currentTime);

            if (laundryOpen) {

                textViewStatus.setText("OPEN");

            } else {

                textViewStatus.setText("CLOSED");
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateView(String task, String orderId, OrderStatus status) {

        runOnUiThread(this::setupViews);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean status) {

        if (checkSwitchStatus) {

            String title = status ? "Go online now ?" : "Go offline now ?";

            AlertDialog.Builder alert;
            alert = new AlertDialog.Builder(this);
            alert.setTitle(title);

            alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                dialog.dismiss();
                checkSwitchStatus = true;

                Map<String, Object> data = new HashMap<>();
                data.put("laundry_id", Session.user.getLaundry().getId());
                data.put("status", status);

                showLoadingAnimation();

                FirebaseFunctions
                        .getInstance()
                        .getHttpsCallable("laundry-setAvailability")
                        .call(data)
                        .addOnSuccessListener(httpsCallableResult -> {

                            hideLoadingAnimation();

                            // change online status locally
                            Session.user.getLaundry().setActive(status);

                        })
                        .addOnFailureListener(e -> {

                            hideLoadingAnimation();

                            Toast.makeText(DashboardActivity.this,
                                    e.getMessage(), Toast.LENGTH_SHORT).show();

                            Log.d("laundry", "acceptTripRequest: " + e.getMessage());
                        });
            });

            alert.setNegativeButton("No", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                checkSwitchStatus = false;
                switchAvailable.setChecked(!status);
                checkSwitchStatus = true;
            });

            alert.show();
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
    public void onBackPressed() {
        finishAffinity();
    }
}