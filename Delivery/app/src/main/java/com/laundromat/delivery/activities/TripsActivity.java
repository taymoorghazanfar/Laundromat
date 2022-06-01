package com.laundromat.delivery.activities;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.delivery.R;
import com.laundromat.delivery.prefs.Session;
import com.laundromat.delivery.services.LocationServiceUtils;
import com.laundromat.delivery.ui.adapters.TripsPagerAdapter;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class TripsActivity extends AppCompatActivity
        implements View.OnClickListener {

    // Views
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private TabLayout tabLayoutTrips;
    private ViewPager viewPagerTrips;
    private TripsPagerAdapter adapter;
    private RelativeLayout layoutLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        initViews();
    }

    private void initViews() {

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        initToolbar();

        initDrawer();

        initDrawerMenu();

        initTabLayout();
    }

    private void initToolbar() {

        // setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

    private void initTabLayout() {

        viewPagerTrips = findViewById(R.id.view_pager_trips);
        adapter = new TripsPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPagerTrips.setAdapter(adapter);
        viewPagerTrips.setOffscreenPageLimit(3);

        tabLayoutTrips = findViewById(R.id.tab_layout_trips);
        tabLayoutTrips.setupWithViewPager(viewPagerTrips);
    }

    private void logout() {

        Session.destroy(TripsActivity.this);

        startActivity(new Intent(TripsActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.option_dashboard) {

            startActivity(new Intent(TripsActivity.this, DashboardActivity.class));
            finish();

        } else if (view.getId() == R.id.option_trips) {

            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {

                drawerLayout.closeDrawer(GravityCompat.START);
            }

        } else if (view.getId() == R.id.option_transactions) {

            startActivity(new Intent(TripsActivity.this, TransactionsActivity.class));
            finish();

        } else if (view.getId() == R.id.option_profile) {

            startActivity(new Intent(TripsActivity.this, ProfileActivity.class));
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

                            LocationServiceUtils.stopLocationService(TripsActivity.this);
                            Session.destroy(TripsActivity.this);

                            startActivity(new Intent(TripsActivity.this, LoginActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {

                            hideLoadingAnimation();

                            Toast.makeText(TripsActivity.this,
                                    e.getMessage(), Toast.LENGTH_SHORT).show();

                            Log.d("driver", "acceptTripRequest: " + e.getMessage());
                        });
            });

            alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

            alert.show();
        }
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(TripsActivity.this, DashboardActivity.class));
        finish();
    }

    public void showLoadingAnimation() {

        layoutLoading.setVisibility(View.VISIBLE);
        layoutLoading.bringToFront();
        layoutLoading.animate().translationY(0);
    }

    public void hideLoadingAnimation() {

        layoutLoading.setVisibility(View.GONE);
        layoutLoading.animate().translationY(layoutLoading.getHeight());
    }
}