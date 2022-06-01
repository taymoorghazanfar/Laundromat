package com.laundromat.delivery.activities;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.laundromat.delivery.R;
import com.laundromat.delivery.model.Transaction;
import com.laundromat.delivery.model.Trip;
import com.laundromat.delivery.model.Vehicle;
import com.laundromat.delivery.model.observers.ITripObserver;
import com.laundromat.delivery.model.util.TransactionType;
import com.laundromat.delivery.model.util.TripStatus;
import com.laundromat.delivery.prefs.Session;
import com.laundromat.delivery.services.LocationServiceUtils;
import com.laundromat.delivery.ui.adapters.TripsRecyclerAdapter;
import com.laundromat.delivery.ui.decorators.SpacesItemDecoration;
import com.laundromat.delivery.ui.interfaces.ITripClickListener;
import com.laundromat.delivery.utils.GsonUtils;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity
        implements View.OnClickListener, ITripObserver, ITripClickListener, CompoundButton.OnCheckedChangeListener {

    // variables
    boolean checkSwitchStatus = false;

    // Views
    private Toolbar toolbar;
    private RelativeLayout layoutLoading;
    private DrawerLayout drawerLayout;
    private ImageView imageViewDriver;
    private TextView textViewDriverName;
    private TextView textViewVehicle;
    private SwitchCompat switchAvailable;
    private TextView textViewTotalTrips;
    private TextView textViewEarnings;
    private TextView textViewTotalTransactions;
    private TextView textViewCurrentTrips;
    private RecyclerView recyclerViewCurrentTrips;
    private TripsRecyclerAdapter adapter;
    private TextView textViewEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (Session.user != null) {

            Session.user.registerObserver(this);
            LocationServiceUtils.startLocationService(DashboardActivity.this);
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

        imageViewDriver = findViewById(R.id.image_view_rider);
        textViewDriverName = findViewById(R.id.text_view_name);
        textViewVehicle = findViewById(R.id.text_view_vehicle);
        switchAvailable = findViewById(R.id.switch_available);

        textViewTotalTrips = findViewById(R.id.text_view_total_trips);
        textViewEarnings = findViewById(R.id.text_view_earning);
        textViewTotalTransactions = findViewById(R.id.text_view_total_transactions);

        textViewCurrentTrips = findViewById(R.id.text_view_current_trips);
        recyclerViewCurrentTrips = findViewById(R.id.recycler_view_current_trips);
        recyclerViewCurrentTrips.setLayoutManager(new LinearLayoutManager(this));

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        this.recyclerViewCurrentTrips.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
    }

    private void initDrawer() {

        drawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        drawerLayout.addDrawerListener(toggle);
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

        View optionOrders = findViewById(R.id.option_trips);
        optionOrders.setOnClickListener(this);

        View optionTransactions = findViewById(R.id.option_transactions);
        optionTransactions.setOnClickListener(this);

        View optionLogout = findViewById(R.id.option_logout);
        optionLogout.setOnClickListener(this);

        View optionProfile = findViewById(R.id.option_profile);
        optionProfile.setOnClickListener(this);
    }

    private void setupViews() {

        Picasso.get()
                .load(Session.user.getAvatarUrl())
                .into(imageViewDriver);

        textViewDriverName.setText(Session.user.getFullName());

        Vehicle vehicle = Session.user.getVehicle();
        String vehicleDetails = vehicle.name + " | " + vehicle.model +
                " | " + vehicle.color + " | " + vehicle.plateNumber;
        textViewVehicle.setText(vehicleDetails);

        switchAvailable.setChecked(Session.user.isAvailable());
        switchAvailable.setOnCheckedChangeListener(this);
        checkSwitchStatus = true;

        textViewTotalTrips.setText(MessageFormat
                .format("{0}\nTrips", Session.user.getTrips().size()));

        getTotalEarnings();

        textViewTotalTransactions.setText(MessageFormat
                .format("{0}\nTransactions", Session.user.getTransactions().size()));

        setupRecyclerView();
    }

    private void getTotalEarnings() {

        double earnings = 0;

        for (Transaction transaction : Session.user.getTransactions()) {

            if (transaction.getType() == TransactionType.EARNING) {

                earnings += transaction.getAmount();
            }
        }

        textViewEarnings
                .setText(MessageFormat.format("PKR {0}\nEarnings", earnings));
    }

    private void setupRecyclerView() {

        List<Trip> trips = getCurrentTrips();

        adapter = new TripsRecyclerAdapter(this, trips);
        adapter.setTripClickListener(this);
        recyclerViewCurrentTrips.setAdapter(adapter);

        textViewEmpty.setVisibility(getCurrentTrips().size() > 0 ? View.GONE : View.VISIBLE);

        textViewCurrentTrips.setText(MessageFormat
                .format("Current Trips ({0})", trips.size()));

        textViewEmpty.setVisibility(trips.size() > 0 ? View.GONE : View.VISIBLE);
    }

    private ArrayList<Trip> getCurrentTrips() {

        ArrayList<Trip> trips = new ArrayList<>();

        for (Trip trip : Session.user.getTrips()) {

            if (trip.getStatus() != TripStatus.DECLINED
                    && trip.getStatus() != TripStatus.COMPLETED
                    && trip.getStatus() != TripStatus.CANCELLED) {

                // deep copy
                Gson gson = new Gson();
                trips.add(gson.fromJson(gson.toJson(trip), Trip.class));
            }
        }

        // sort the list by most recent trips
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

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.option_dashboard) {

            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {

                drawerLayout.closeDrawer(GravityCompat.START);
            }

        } else if (view.getId() == R.id.option_trips) {

            startActivity(new Intent(DashboardActivity.this, TripsActivity.class));
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

                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {

                    drawerLayout.closeDrawer(GravityCompat.START);
                }

                Map<String, Object> data = new HashMap<>();
                data.put("driver_id", Session.user.getId());
                data.put("status", false);

                showLoadingAnimation();

                FirebaseFunctions
                        .getInstance()
                        .getHttpsCallable("delivery_boy-setAvailability")
                        .call(data)
                        .addOnSuccessListener(httpsCallableResult -> {

                            hideLoadingAnimation();

                            LocationServiceUtils.stopLocationService(DashboardActivity.this);
                            Session.destroy(DashboardActivity.this);

                            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {

                            hideLoadingAnimation();

                            Toast.makeText(DashboardActivity.this,
                                    e.getMessage(), Toast.LENGTH_SHORT).show();

                            Log.d("driver", "acceptTripRequest: " + e.getMessage());
                        });

            });

            alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

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
    public void updateView(String task, String tripId, TripStatus status) {

        runOnUiThread(this::setupViews);
    }

    @Override
    public void onTripClick(Trip trip) {

        Intent intent;

        if (trip.getStatus() == TripStatus.REQUESTED) {

            intent = new Intent(this, TripRequestActivity.class);

        } else {

            intent = new Intent(this, TripActivity.class);
        }

        intent.putExtra("trip", GsonUtils.tripToGson(trip));

        startActivity(intent);
        finish();
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
                data.put("driver_id", Session.user.getId());
                data.put("status", status);

                showLoadingAnimation();

                FirebaseFunctions
                        .getInstance()
                        .getHttpsCallable("delivery_boy-setAvailability")
                        .call(data)
                        .addOnSuccessListener(httpsCallableResult -> {

                            hideLoadingAnimation();

                            // change online status locally
                            Session.user.setAvailable(status);

                        })
                        .addOnFailureListener(e -> {

                            hideLoadingAnimation();

                            Toast.makeText(DashboardActivity.this,
                                    e.getMessage(), Toast.LENGTH_SHORT).show();

                            Log.d("driver", "acceptTripRequest: " + e.getMessage());
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

    @Override
    public void onBackPressed() {

        finishAffinity();
    }
}