package com.laundromat.admin.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import com.laundromat.admin.R;
import com.laundromat.admin.dialogs.OrderFilterDialog;
import com.laundromat.admin.dialogs.TripFilterDialog;
import com.laundromat.admin.model.DeliveryBoy;
import com.laundromat.admin.model.Trip;
import com.laundromat.admin.model.order.Order;
import com.laundromat.admin.model.order.OrderStatus;
import com.laundromat.admin.model.util.TripStatus;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.adapters.TripsRecyclerAdapter;
import com.laundromat.admin.ui.decorators.SpacesItemDecoration;
import com.laundromat.admin.ui.interfaces.IOrderClickListener;
import com.laundromat.admin.ui.interfaces.IOrderFilterListener;
import com.laundromat.admin.ui.interfaces.ITripClickListener;
import com.laundromat.admin.ui.interfaces.ITripFilterListener;
import com.laundromat.admin.utils.GsonUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TripsActivity extends AppCompatActivity
        implements View.OnClickListener, ITripClickListener, ITripFilterListener, SwipeRefreshLayout.OnRefreshListener {

    private static final int REQUEST_CODE_TRIP = 111;
    // Views
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout layoutLoading;
    private TextView textViewEmpty;
    private RecyclerView recyclerViewTrips;
    private TripsRecyclerAdapter adapter;
    private SearchView searchView;
    private ImageView imageViewFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

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

        recyclerViewTrips = findViewById(R.id.recycler_view_trips);
        recyclerViewTrips.setLayoutManager(new LinearLayoutManager(this));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        recyclerViewTrips.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

        setupRecyclerView(getAllTrips());

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

    private void setupRecyclerView(List<Trip> trips) {

        adapter = new TripsRecyclerAdapter(this, trips);
        adapter.setTripClickListener(this);
        recyclerViewTrips.setAdapter(adapter);

        textViewEmpty.setVisibility(trips.size() > 0 ? View.GONE : View.VISIBLE);
    }

    private List<Trip> getAllTrips() {

        List<Trip> trips = new ArrayList<>();

        for (DeliveryBoy driver : Session.user.getDeliveryBoys()) {

            trips.addAll(new ArrayList<>(driver.getTrips()));
        }

        Collections.sort(trips, new Comparator<Trip>() {
            final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            @Override
            public int compare(Trip trip1, Trip trip2) {
                try {
                    return dateFormat.parse(trip2.getDateCreated())
                            .compareTo(dateFormat.parse(trip1.getDateCreated()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });

        return trips;
    }

    private List<Trip> getFilteredTrips(TripStatus status) {

        List<Trip> trips = new ArrayList<>();

        for (Trip trip : getAllTrips()) {

            if (trip.getStatus() == status) {

                trips.add(trip);
            }
        }

        Collections.sort(trips,
                (trip1, trip2)
                        -> trip2.getDateCreated().compareTo(trip1.getDateCreated()));

        return trips;
    }

    private void showFilterDialog() {

        TripFilterDialog dialog = new TripFilterDialog();
        dialog.setCancelable(false);
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "dialog_filter");
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(TripsActivity.this,
                DashboardActivity.class));
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.option_dashboard:
                startActivity(new Intent(TripsActivity.this,
                        DashboardActivity.class));
                finish();
                break;

            case R.id.option_new_merchants:
                startActivity(new Intent(TripsActivity.this, NewMerchantsActivity.class));
                finish();

            case R.id.option_new_drivers:
                startActivity(new Intent(TripsActivity.this, NewDriversActivity.class));
                finish();
                break;

            case R.id.option_merchants:
                startActivity(new Intent(TripsActivity.this, MerchantsActivity.class));
                finish();
                break;

            case R.id.option_drivers:
                startActivity(new Intent(TripsActivity.this, DriversActivity.class));
                finish();
                break;

            case R.id.option_customers:
                startActivity(new Intent(TripsActivity.this, CustomersActivity.class));
                finish();
                break;

            case R.id.option_orders:
                startActivity(new Intent(TripsActivity.this, OrdersActivity.class));
                finish();
                break;

            case R.id.option_trips:
                if (drawer.isDrawerOpen(GravityCompat.START)) {

                    drawer.closeDrawer(GravityCompat.START);
                }
                break;

            case R.id.option_services:
                startActivity(new Intent(TripsActivity.this, ServiceTypesActivity.class));
                finish();
                break;

            case R.id.option_config:
                startActivity(new Intent(TripsActivity.this, SettingsActivity.class));
                finish();
                break;

            case R.id.option_logout:
                AlertDialog.Builder alert;
                alert = new AlertDialog.Builder(this);
                alert.setTitle("Logout ?");

                alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                    dialog.dismiss();
                    Session.destroy(TripsActivity.this);

                    startActivity(new Intent(TripsActivity.this, LoginActivity.class));
                    finish();
                });

                alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

                alert.show();

            case R.id.image_view_search_filter:
                showFilterDialog();
                break;
        }
    }

    @Override
    public void onTripClick(Trip trip) {

        Intent intent = new Intent(this, TripActivity.class);
        intent.putExtra("trip", GsonUtils.tripToGson(trip));

        startActivityForResult(intent, REQUEST_CODE_TRIP);
    }

    @Override
    public void onTripFiltered(TripStatus status) {

        setupRecyclerView(status == null ? getAllTrips() : getFilteredTrips(status));
    }

    @Override
    public void onRefresh() {

//        showLoadingAnimation();
//        setupRecyclerView(getAllTrips());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_TRIP) {

            setupRecyclerView(getAllTrips());
        }
    }
}