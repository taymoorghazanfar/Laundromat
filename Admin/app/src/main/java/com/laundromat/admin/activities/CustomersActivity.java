package com.laundromat.admin.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.admin.R;
import com.laundromat.admin.model.Customer;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.adapters.CustomersRecyclerAdapter;
import com.laundromat.admin.ui.decorators.SpacesItemDecoration;
import com.laundromat.admin.ui.interfaces.ICustomerClickListener;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.ParseUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CustomersActivity extends AppCompatActivity
        implements View.OnClickListener, ICustomerClickListener, SwipeRefreshLayout.OnRefreshListener {

    // Constants
    private static final int REQUEST_CODE_CUSTOMER = 111;

    // Variables
    private List<Customer> customers;

    // Views
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout layoutLoading;
    private DrawerLayout drawer;
    private TextView textViewEmpty;
    private RecyclerView recyclerViewCustomers;
    private CustomersRecyclerAdapter adapter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers);

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

        recyclerViewCustomers = findViewById(R.id.recycler_view_customers);
        recyclerViewCustomers.setLayoutManager(new LinearLayoutManager(this));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        recyclerViewCustomers.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

        initRecyclerView();

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

        View optionLogout = findViewById(R.id.option_logout);
        optionLogout.setOnClickListener(this);
    }

    private void initRecyclerView() {

        Collections.sort(Session.user.getCustomers(), new Comparator<Customer>() {
            final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            @Override
            public int compare(Customer c1, Customer c2) {
                try {
                    return dateFormat.parse(c2.getDateCreated())
                            .compareTo(dateFormat.parse(c1.getDateCreated()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });

        customers = Session.user.getCustomers();

        adapter = new CustomersRecyclerAdapter(this, customers);
        adapter.setCustomerClickListener(this);
        recyclerViewCustomers.setAdapter(adapter);

        textViewEmpty.setVisibility(customers.size() > 0 ? View.GONE : View.VISIBLE);
    }

    private void getAllCustomers() {

        showLoadingAnimation();

        // get all customers
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("admin-getCustomers")
                .call()
                .addOnSuccessListener(httpsCallableResult5 -> {

                    hideLoadingAnimation();

                    Session.user.setCustomers(ParseUtils
                            .parseCustomers(httpsCallableResult5.getData()));

                    initRecyclerView();

                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Toast.makeText(CustomersActivity.this, e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.d("customers", "onFailure: " + e.getMessage());
                });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(CustomersActivity.this,
                DashboardActivity.class));
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.option_dashboard:
                startActivity(new Intent(CustomersActivity.this,
                        DashboardActivity.class));
                finish();
                break;

            case R.id.option_new_merchants:
                startActivity(new Intent(CustomersActivity.this, NewMerchantsActivity.class));
                finish();

            case R.id.option_new_drivers:
                startActivity(new Intent(CustomersActivity.this, NewDriversActivity.class));
                finish();
                break;

            case R.id.option_merchants:
                startActivity(new Intent(CustomersActivity.this, MerchantsActivity.class));
                finish();
                break;

            case R.id.option_drivers:
                startActivity(new Intent(CustomersActivity.this, DriversActivity.class));
                finish();
                break;

            case R.id.option_customers:
                if (drawer.isDrawerOpen(GravityCompat.START)) {

                    drawer.closeDrawer(GravityCompat.START);
                }
                break;

            case R.id.option_orders:
                startActivity(new Intent(CustomersActivity.this, OrdersActivity.class));
                finish();
                break;

            case R.id.option_trips:
                startActivity(new Intent(CustomersActivity.this, TripsActivity.class));
                finish();
                break;

            case R.id.option_services:
                startActivity(new Intent(CustomersActivity.this, ServiceTypesActivity.class));
                finish();
                break;

            case R.id.option_config:
                startActivity(new Intent(CustomersActivity.this, SettingsActivity.class));
                finish();
                break;

            case R.id.option_logout:
                AlertDialog.Builder alert;
                alert = new AlertDialog.Builder(this);
                alert.setTitle("Logout ?");

                alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                    dialog.dismiss();
                    Session.destroy(CustomersActivity.this);

                    startActivity(new Intent(CustomersActivity.this, LoginActivity.class));
                    finish();
                });

                alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

                alert.show();
        }
    }

    @Override
    public void onCustomerClick(int index, Customer customer) {

        Intent intent = new Intent(
                CustomersActivity.this,
                CustomerProfileActivity.class);

        intent.putExtra("customer", GsonUtils.customerToGson(customer));
        intent.putExtra("transactions", GsonUtils.transactionsToGson(customer.getTransactions()));
        intent.putExtra("index", index);

        startActivityForResult(intent, REQUEST_CODE_CUSTOMER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CUSTOMER) {

            initRecyclerView();
        }
    }

    @Override
    public void onRefresh() {

        getAllCustomers();
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
}