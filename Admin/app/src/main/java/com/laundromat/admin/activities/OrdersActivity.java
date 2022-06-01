package com.laundromat.admin.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.admin.R;
import com.laundromat.admin.dialogs.OrderFilterDialog;
import com.laundromat.admin.model.Customer;
import com.laundromat.admin.model.order.Order;
import com.laundromat.admin.model.order.OrderStatus;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.adapters.OrdersRecyclerAdapter;
import com.laundromat.admin.ui.decorators.SpacesItemDecoration;
import com.laundromat.admin.ui.interfaces.IOrderClickListener;
import com.laundromat.admin.ui.interfaces.IOrderFilterListener;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.ParseUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrdersActivity extends AppCompatActivity
        implements View.OnClickListener, IOrderClickListener, IOrderFilterListener, SwipeRefreshLayout.OnRefreshListener {

    private static final int REQUEST_CODE_ORDER = 111;
    // Views
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout layoutLoading;
    private DrawerLayout drawer;
    private TextView textViewEmpty;
    private RecyclerView recyclerViewOrders;
    private OrdersRecyclerAdapter adapter;
    private SearchView searchView;
    private ImageView imageViewFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        initViews();
    }

    private void initViews() {

        // setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);


        initDrawer();

        initDrawerMenu();

        textViewEmpty = findViewById(R.id.text_view_empty);
        textViewEmpty.setVisibility(View.GONE);

        recyclerViewOrders = findViewById(R.id.recycler_view_orders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        recyclerViewOrders.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

        setupRecyclerView(getAllOrders());

        searchView = findViewById(R.id.search_view);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                adapter.getFilter().filter(newText);
                return false;
            }
        });

        imageViewFilter = findViewById(R.id.image_view_search_filter);
        imageViewFilter.setOnClickListener(this);
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

        View optionDashboard = findViewById(R.id.option_dashboard);
        optionDashboard.setOnClickListener(this);

        View optionNewMerchants = findViewById(R.id.option_new_merchants);
        optionNewMerchants.setOnClickListener(this);

        View optionNewDrivers = findViewById(R.id.option_new_drivers);
        optionNewDrivers.setOnClickListener(this);

        View optionAllMerchants = findViewById(R.id.option_merchants);
        optionAllMerchants.setOnClickListener(this);

        View optionAllDrivers = findViewById(R.id.option_drivers);
        optionAllDrivers.setOnClickListener(this);

        View optionCustomers = findViewById(R.id.option_customers);
        optionCustomers.setOnClickListener(this);

        View optionOrders = findViewById(R.id.option_orders);
        optionOrders.setOnClickListener(this);

        View optionTrips = findViewById(R.id.option_trips);
        optionTrips.setOnClickListener(this);


        View optionServiceTypes = findViewById(R.id.option_services);
        optionServiceTypes.setOnClickListener(this);

        View optionSettings = findViewById(R.id.option_config);
        optionSettings.setOnClickListener(this);
    }

    private void setupRecyclerView(List<Order> orders) {

        adapter = new OrdersRecyclerAdapter(orders);
        adapter.setOrderClickListener(this);
        recyclerViewOrders.setAdapter(adapter);

        textViewEmpty.setVisibility(orders.size() > 0 ? View.GONE : View.VISIBLE);
    }

    private List<Order> getAllOrders() {

        List<Order> orders = new ArrayList<>();

        for (Customer customer : Session.user.getCustomers()) {

            orders.addAll(new ArrayList<>(customer.getOrders()));
        }

        Collections.sort(orders, new Comparator<Order>() {
            final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            @Override
            public int compare(Order order1, Order order2) {
                try {
                    return dateFormat.parse(order2.getDateCreated())
                            .compareTo(dateFormat.parse(order1.getDateCreated()));

                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });


        return orders;
    }

    private List<Order> getFilteredOrders(OrderStatus status) {

        List<Order> orders = new ArrayList<>();

        for (Order order : getAllOrders()) {

            if (order.getStatus() == status) {

                orders.add(order);
            }
        }

        Collections.sort(orders,
                (order1, order2)
                        -> order2.getDateCreated().compareTo(order1.getDateCreated()));

        return orders;
    }

    private void refreshOrders() {

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("admin-getOrders")
                .call()
                .addOnSuccessListener(httpsCallableResult6 -> {

                    hideLoadingAnimation();

                    Session.user.setOrders(ParseUtils
                            .parseOrders(httpsCallableResult6.getData()));

                    setupRecyclerView(getAllOrders());

                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Toast.makeText(OrdersActivity.this, e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.d("orders", "onFailure: " + e.getMessage());
                });
    }

    private void showFilterDialog() {

        OrderFilterDialog dialog = new OrderFilterDialog();
        dialog.setCancelable(false);
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "dialog_filter");
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(OrdersActivity.this,
                DashboardActivity.class));
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.option_dashboard:
                startActivity(new Intent(OrdersActivity.this,
                        DashboardActivity.class));
                finish();
                break;

            case R.id.option_new_merchants:
                startActivity(new Intent(OrdersActivity.this, NewMerchantsActivity.class));
                finish();

            case R.id.option_new_drivers:
                startActivity(new Intent(OrdersActivity.this, NewDriversActivity.class));
                finish();
                break;

            case R.id.option_merchants:
                startActivity(new Intent(OrdersActivity.this, MerchantsActivity.class));
                finish();
                break;

            case R.id.option_drivers:
                startActivity(new Intent(OrdersActivity.this, DriversActivity.class));
                finish();
                break;

            case R.id.option_customers:
                startActivity(new Intent(OrdersActivity.this, CustomersActivity.class));
                finish();
                break;

            case R.id.option_orders:
                if (drawer.isDrawerOpen(GravityCompat.START)) {

                    drawer.closeDrawer(GravityCompat.START);
                }
                break;

            case R.id.option_trips:
                startActivity(new Intent(OrdersActivity.this, TripsActivity.class));
                finish();
                break;

            case R.id.option_services:
                startActivity(new Intent(OrdersActivity.this, ServiceTypesActivity.class));
                finish();
                break;

            case R.id.option_config:
                startActivity(new Intent(OrdersActivity.this, SettingsActivity.class));
                finish();
                break;

            case R.id.image_view_search_filter:
                showFilterDialog();
                break;

            case R.id.option_logout:
                AlertDialog.Builder alert;
                alert = new AlertDialog.Builder(this);
                alert.setTitle("Logout ?");

                alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                    dialog.dismiss();
                    Session.destroy(OrdersActivity.this);

                    startActivity(new Intent(OrdersActivity.this, LoginActivity.class));
                    finish();
                });

                alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

                alert.show();
        }
    }

    @Override
    public void onOrderFiltered(OrderStatus status) {

        setupRecyclerView(status == null ? getAllOrders() : getFilteredOrders(status));
    }

    @Override
    public void onOrderClick(Order order) {

        Intent intent = new Intent(this, OrderActivity.class);
        intent.putExtra("order", GsonUtils.orderToGson(order));

        startActivityForResult(intent, REQUEST_CODE_ORDER);
    }

    @Override
    public void onRefresh() {

        refreshOrders();
        swipeRefreshLayout.setRefreshing(false);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_ORDER){

            setupRecyclerView(getAllOrders());
        }
    }
}