package com.laundromat.admin.activities;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.admin.R;
import com.laundromat.admin.model.Admin;
import com.laundromat.admin.model.Customer;
import com.laundromat.admin.model.DeliveryBoy;
import com.laundromat.admin.model.Laundry;
import com.laundromat.admin.model.Merchant;
import com.laundromat.admin.model.Transaction;
import com.laundromat.admin.model.order.Order;
import com.laundromat.admin.model.util.TransactionType;
import com.laundromat.admin.model.washable.ServiceType;
import com.laundromat.admin.model.washable.WashableItemCategory;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.utils.ParseUtils;

import java.text.MessageFormat;
import java.util.List;

public class DashboardActivity extends AppCompatActivity
        implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private Toolbar toolbar;
    private DrawerLayout drawer;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout layoutLoading;

    private CardView cardViewNewMerchants;
    private TextView textViewNewMerchants;

    private CardView cardViewNewDrivers;
    private TextView textViewNewDrivers;

    private CardView cardViewCustomers;
    private TextView textViewCustomers;

    private CardView cardViewMerchants;
    private TextView textViewMerchants;

    private CardView cardViewDrivers;
    private TextView textViewDrivers;

    private CardView cardViewOrders;
    private TextView textViewOrders;

    private CardView cardViewTrips;
    private TextView textViewTrips;

    private CardView cardViewTransactions;
    private TextView textViewTransactions;

    private CardView cardViewServiceTypes;
    private TextView textViewServiceTypes;

    private CardView cardViewItems;
    private TextView textViewItems;

    private CardView cardViewEarnings;
    private TextView textViewEarnings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

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

        initDashboard();

        setupDashboard();
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

    private void initDashboard() {

        cardViewNewMerchants = findViewById(R.id.card_view_merchant_requests);
        cardViewNewMerchants.setOnClickListener(this);
        textViewNewMerchants = findViewById(R.id.text_view_merchant_requests);

        cardViewNewDrivers = findViewById(R.id.card_view_driver_requests);
        cardViewNewDrivers.setOnClickListener(this);
        textViewNewDrivers = findViewById(R.id.text_view_driver_requests);

        cardViewCustomers = findViewById(R.id.card_view_customers);
        cardViewCustomers.setOnClickListener(this);
        textViewCustomers = findViewById(R.id.text_view_customers);

        cardViewMerchants = findViewById(R.id.card_view_merchants);
        cardViewMerchants.setOnClickListener(this);
        textViewMerchants = findViewById(R.id.text_view_merchants);

        cardViewDrivers = findViewById(R.id.card_view_drivers);
        cardViewDrivers.setOnClickListener(this);
        textViewDrivers = findViewById(R.id.text_view_drivers);

        cardViewOrders = findViewById(R.id.card_view_orders);
        cardViewOrders.setOnClickListener(this);
        textViewOrders = findViewById(R.id.text_view_orders);

        cardViewTrips = findViewById(R.id.card_view_trips);
        cardViewTrips.setOnClickListener(this);
        textViewTrips = findViewById(R.id.text_view_trips);

        cardViewTransactions = findViewById(R.id.card_view_transactions);
        cardViewTransactions.setOnClickListener(this);
        textViewTransactions = findViewById(R.id.text_view_transactions);

        cardViewServiceTypes = findViewById(R.id.card_view_service_types);
        cardViewServiceTypes.setOnClickListener(this);
        textViewServiceTypes = findViewById(R.id.text_view_service_types);

        cardViewItems = findViewById(R.id.card_view_items);
        cardViewItems.setOnClickListener(this);
        textViewItems = findViewById(R.id.text_view_items);

        cardViewEarnings = findViewById(R.id.card_view_earnings);
        cardViewEarnings.setOnClickListener(this);
        textViewEarnings = findViewById(R.id.text_view_earnings);
    }

    private void setupDashboard() {

        Admin admin = Session.user;

        // new merchants
        textViewNewMerchants.setText(MessageFormat
                .format("{0}\nMerchants", admin.getNewMerchants().size()));

        // new drivers
        textViewNewDrivers.setText(MessageFormat
                .format("{0}\nDrivers", admin.getNewDeliveryBoys().size()));

        // customers
        textViewCustomers.setText(MessageFormat
                .format("{0}\nCustomers", admin.getCustomers().size()));

        // merchants
        textViewMerchants.setText(MessageFormat
                .format("{0}\nMerchants", admin.getMerchants().size()));

        // drivers
        textViewDrivers.setText(MessageFormat
                .format("{0}\nDrivers", admin.getDeliveryBoys().size()));

        // orders
        int orders = 0;

        for (Customer customer : admin.getCustomers()) {

            orders += customer.getOrders().size();
        }

        textViewOrders.setText(MessageFormat
                .format("{0}\nOrders", orders));

        // trips
        int trips = 0;
        for (DeliveryBoy deliveryBoy : admin.getDeliveryBoys()) {

            trips += deliveryBoy.getTrips().size();
        }

        textViewTrips.setText(MessageFormat
                .format("{0}\nTrips", trips));

        // transactions
        int transactions = 0;
        for (Customer customer : admin.getCustomers()) {

            transactions += customer.getTransactions().size();
        }

        for (Merchant merchant : admin.getMerchants()) {

            transactions += merchant.getTransactions().size();
        }

        for (DeliveryBoy deliveryBoy : admin.getDeliveryBoys()) {

            transactions += deliveryBoy.getTransactions().size();
        }

        textViewTransactions.setText(MessageFormat
                .format("{0}\nTransactions", transactions));

        // service types
        textViewServiceTypes.setText(MessageFormat
                .format("{0}\nAvailable Services", admin.getServiceTypes().size()));

        // menu items
        int totalItems = 0;
        for (Merchant merchant : admin.getMerchants()) {

            Laundry laundry = merchant.getLaundry();

            for (WashableItemCategory washableItemCategory : laundry.getMenu()) {

                totalItems += washableItemCategory.getWashableItems().size();
            }
        }

        textViewItems.setText(MessageFormat
                .format("{0}\nItems Listed", totalItems));

        // revenue
        double revenue = 0;
        for (Customer customer : admin.getCustomers()) {

            for (Transaction transaction : customer.getTransactions()) {

                if (transaction.getType() == TransactionType.ORDER_PAYMENT
                        || transaction.getType() == TransactionType.PICKUP_FEE) {

                    revenue += transaction.getAmount();
                }
            }
        }

        for (Merchant merchant : admin.getMerchants()) {

            for (Transaction transaction : merchant.getTransactions()) {

                if (transaction.getType() == TransactionType.DELIVERY_FEE) {

                    revenue += transaction.getAmount();
                }
            }
        }

        textViewEarnings.setText(MessageFormat
                .format("PKR {0}\n Total Revenue", revenue));
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.option_dashboard:
                if (drawer.isDrawerOpen(GravityCompat.START)) {

                    drawer.closeDrawer(GravityCompat.START);
                }
                break;

            case R.id.option_new_merchants:
            case R.id.card_view_merchant_requests:
                startActivity(new Intent(DashboardActivity.this, NewMerchantsActivity.class));
                finish();
                break;

            case R.id.option_new_drivers:
            case R.id.card_view_driver_requests:
                startActivity(new Intent(DashboardActivity.this, NewDriversActivity.class));
                finish();
                break;

            case R.id.option_merchants:
            case R.id.card_view_merchants:
                startActivity(new Intent(DashboardActivity.this, MerchantsActivity.class));
                finish();
                break;

            case R.id.option_drivers:
            case R.id.card_view_drivers:
                startActivity(new Intent(DashboardActivity.this, DriversActivity.class));
                finish();
                break;

            case R.id.option_customers:
            case R.id.card_view_customers:
                startActivity(new Intent(DashboardActivity.this, CustomersActivity.class));
                finish();
                break;

            case R.id.option_orders:
            case R.id.card_view_orders:
                startActivity(new Intent(DashboardActivity.this, OrdersActivity.class));
                finish();
                break;

            case R.id.option_trips:
            case R.id.card_view_trips:
                startActivity(new Intent(DashboardActivity.this, TripsActivity.class));
                finish();
                break;

            case R.id.option_services:
            case R.id.card_view_service_types:
            case R.id.card_view_items:
                startActivity(new Intent(DashboardActivity.this, ServiceTypesActivity.class));
                finish();
                break;

            case R.id.option_config:
                startActivity(new Intent(DashboardActivity.this, SettingsActivity.class));
                finish();
                break;

            case R.id.card_view_transactions:
                startActivity(new Intent(DashboardActivity.this, TransactionsActivity.class));
                finish();
                break;

            case R.id.option_logout:
                AlertDialog.Builder alert;
                alert = new AlertDialog.Builder(this);
                alert.setTitle("Logout ?");

                alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                    dialog.dismiss();
                    Session.destroy(DashboardActivity.this);

                    startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                    finish();
                });

                alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

                alert.show();
        }
    }

    @Override
    public void onRefresh() {

        getAllData();
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

    private void getAllData() {

        showLoadingAnimation();

        // get all new merchant registrations
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("admin-getNewRegistrationRequests")
                .call("merchant")
                .addOnSuccessListener(httpsCallableResult1 -> {

                    Session.user.setNewMerchants(ParseUtils
                            .parseMerchants(httpsCallableResult1.getData()));

                    Log.d("splash", "getAllData: new mer success");

                    // get all new delivery boys
                    FirebaseFunctions
                            .getInstance()
                            .getHttpsCallable("admin-getNewRegistrationRequests")
                            .call("delivery_boy")
                            .addOnSuccessListener(httpsCallableResult2 -> {

                                Session.user.setNewDeliveryBoys(ParseUtils
                                        .parseDeliveryBoys(httpsCallableResult2.getData()));

                                Log.d("splash", "getAllData: new del success");

                                // get all old merchants
                                FirebaseFunctions
                                        .getInstance()
                                        .getHttpsCallable("admin-getMerchants")
                                        .call()
                                        .addOnSuccessListener(httpsCallableResult3 -> {

                                            Session.user.setMerchants(ParseUtils
                                                    .parseMerchants(httpsCallableResult3.getData()));

                                            Log.d("splash", "getAllData: old mer success");

                                            // get all old delivery boys
                                            FirebaseFunctions
                                                    .getInstance()
                                                    .getHttpsCallable("admin-getDeliveryBoys")
                                                    .call()
                                                    .addOnSuccessListener(httpsCallableResult4 -> {

                                                        Session.user.setDeliveryBoys(ParseUtils
                                                                .parseDeliveryBoys(httpsCallableResult4.getData()));

                                                        Log.d("splash", "getAllData: old del success");

                                                        // get all customers
                                                        FirebaseFunctions
                                                                .getInstance()
                                                                .getHttpsCallable("admin-getCustomers")
                                                                .call()
                                                                .addOnSuccessListener(httpsCallableResult5 -> {

                                                                    Session.user.setCustomers(ParseUtils
                                                                            .parseCustomers(httpsCallableResult5.getData()));

                                                                    Log.d("splash", "getAllData: cus success");

                                                                    // get all orders
                                                                    FirebaseFunctions
                                                                            .getInstance()
                                                                            .getHttpsCallable("admin-getOrders")
                                                                            .call()
                                                                            .addOnSuccessListener(httpsCallableResult6 -> {

                                                                                Session.user.setOrders(ParseUtils
                                                                                        .parseOrders(httpsCallableResult6.getData()));

                                                                                Log.d("splash", "getAllData: ord success");

                                                                                // get all service types
                                                                                FirebaseFunctions
                                                                                        .getInstance()
                                                                                        .getHttpsCallable("admin-getServiceTypes")
                                                                                        .call()
                                                                                        .addOnSuccessListener(httpsCallableResult7 -> {

                                                                                            hideLoadingAnimation();

                                                                                            Session.user.setServiceTypes(ParseUtils
                                                                                                    .parseServiceTypes(httpsCallableResult7.getData()));

                                                                                            setupDashboard();
                                                                                        })
                                                                                        .addOnFailureListener(e -> {

                                                                                            hideLoadingAnimation();
                                                                                            Log.d("splash", "getAllData: " + e.getMessage());
                                                                                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                        });
                                                                            })
                                                                            .addOnFailureListener(e -> {

                                                                                hideLoadingAnimation();
                                                                                Log.d("splash", "getAllData: " + e.getMessage());
                                                                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                            });
                                                                })
                                                                .addOnFailureListener(e -> {

                                                                    hideLoadingAnimation();
                                                                    Log.d("splash", "getAllData: " + e.getMessage());
                                                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                });
                                                    })
                                                    .addOnFailureListener(e -> {

                                                        hideLoadingAnimation();
                                                        Log.d("splash", "getAllData: " + e.getMessage());
                                                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        e.printStackTrace();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {

                                            hideLoadingAnimation();
                                            Log.d("splash", "getAllData: " + e.getMessage());
                                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {

                                hideLoadingAnimation();
                                Log.d("splash", "getAllData: " + e.getMessage());
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();
                    Log.d("splash", "getAllData: " + e.getMessage());
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onBackPressed() {

        finishAffinity();
    }
}