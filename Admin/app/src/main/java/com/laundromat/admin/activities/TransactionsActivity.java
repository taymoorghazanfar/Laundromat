package com.laundromat.admin.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.laundromat.admin.R;
import com.laundromat.admin.model.Admin;
import com.laundromat.admin.model.Customer;
import com.laundromat.admin.model.DeliveryBoy;
import com.laundromat.admin.model.Merchant;
import com.laundromat.admin.model.Transaction;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.adapters.TransactionsRecyclerAdapter;
import com.laundromat.admin.ui.decorators.SpacesItemDecoration;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TransactionsActivity extends AppCompatActivity
        implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    // Variables
    private final Admin admin = Session.user;

    // Views
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout layoutLoading;

    private RecyclerView recyclerViewTransactions;
    private TransactionsRecyclerAdapter adapter;
    private TextView textViewEmpty;

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

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
    }

    private void initRecyclerView() {

        List<Transaction> transactions = getAllTransactions();

        // sort the list by most recent order
        Collections.sort(transactions, new Comparator<Transaction>() {
            final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            @Override
            public int compare(Transaction transaction1, Transaction transaction2) {
                try {
                    return dateFormat.parse(transaction2.getDateCreated())
                            .compareTo(dateFormat.parse(transaction1.getDateCreated()));

                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });

        recyclerViewTransactions = findViewById(R.id.recycler_view_transactions);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        this.recyclerViewTransactions.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

        adapter = new TransactionsRecyclerAdapter(transactions);
        recyclerViewTransactions.setAdapter(adapter);

        textViewEmpty.setVisibility(transactions.size()
                > 0 ? View.GONE : View.VISIBLE);
    }

    private List<Transaction> getAllTransactions() {

        List<Transaction> transactions = new ArrayList<>();

        for (Customer customer : admin.getCustomers()) {

            transactions.addAll(customer.getTransactions());
        }

        for (Merchant merchant : admin.getMerchants()) {

            transactions.addAll(merchant.getTransactions());
        }

        for (DeliveryBoy deliveryBoy : admin.getDeliveryBoys()) {

            transactions.addAll(deliveryBoy.getTransactions());
        }

        return transactions;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(TransactionsActivity.this,
                DashboardActivity.class));
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.option_dashboard:
                startActivity(new Intent(TransactionsActivity.this,
                        DashboardActivity.class));
                finish();
                break;

            case R.id.option_new_merchants:
                startActivity(new Intent(TransactionsActivity.this, NewMerchantsActivity.class));
                finish();

            case R.id.option_new_drivers:
                startActivity(new Intent(TransactionsActivity.this, NewDriversActivity.class));
                finish();
                break;

            case R.id.option_merchants:
                startActivity(new Intent(TransactionsActivity.this, MerchantsActivity.class));
                finish();
                break;

            case R.id.option_drivers:
                startActivity(new Intent(TransactionsActivity.this, DriversActivity.class));
                finish();
                break;

            case R.id.option_customers:
                startActivity(new Intent(TransactionsActivity.this, CustomersActivity.class));
                finish();
                break;

            case R.id.option_orders:
                startActivity(new Intent(TransactionsActivity.this, OrdersActivity.class));
                finish();
                break;

            case R.id.option_trips:
                startActivity(new Intent(TransactionsActivity.this, TripsActivity.class));
                finish();
                break;

            case R.id.option_services:
                startActivity(new Intent(TransactionsActivity.this, ServiceTypesActivity.class));
                finish();
                break;

            case R.id.option_config:
                startActivity(new Intent(TransactionsActivity.this, SettingsActivity.class));
                finish();
                break;

            case R.id.option_logout:
                AlertDialog.Builder alert;
                alert = new AlertDialog.Builder(this);
                alert.setTitle("Logout ?");

                alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                    dialog.dismiss();
                    Session.destroy(TransactionsActivity.this);

                    startActivity(new Intent(TransactionsActivity.this, LoginActivity.class));
                    finish();
                });

                alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

                alert.show();
        }
    }

    @Override
    public void onRefresh() {

//        showLoadingAnimation();
//        initRecyclerView();
//        hideLoadingAnimation();
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