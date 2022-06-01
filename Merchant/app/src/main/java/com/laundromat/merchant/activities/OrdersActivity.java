package com.laundromat.merchant.activities;

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
import com.laundromat.merchant.R;
import com.laundromat.merchant.model.order.Order;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.adapters.OrdersPagerAdapter;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class OrdersActivity extends AppCompatActivity
        implements View.OnClickListener {

    // Views
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private TabLayout tabLayoutOrders;
    private ViewPager viewPagerOrders;
    private OrdersPagerAdapter adapter;
    private RelativeLayout layoutLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

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

    private void initTabLayout() {

        viewPagerOrders = findViewById(R.id.view_pager_orders);
        adapter = new OrdersPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPagerOrders.setAdapter(adapter);
        viewPagerOrders.setOffscreenPageLimit(3);

        tabLayoutOrders = findViewById(R.id.tab_layout_orders);
        tabLayoutOrders.setupWithViewPager(viewPagerOrders);
    }

    private void showDashboardActivity() {

        startActivity(new Intent(OrdersActivity.this, DashboardActivity.class));
        finish();

//        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//
//            drawerLayout.closeDrawer(GravityCompat.START);
//        }
    }

    private void showOrdersActivity() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {

            drawer.closeDrawer(GravityCompat.START);
        }
    }

    private void showMenuActivity() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {

            drawer.closeDrawer(GravityCompat.START);
        }

        startActivity(new Intent(OrdersActivity.this, MenuCategoryActivity.class));
        finish();
    }

    private void showProfileActivity() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {

            drawer.closeDrawer(GravityCompat.START);
        }
        startActivity(new Intent(OrdersActivity.this, ProfileActivity.class));
        finish();
    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.option_dashboard) {

            startActivity(new Intent(OrdersActivity.this, DashboardActivity.class));
            finish();

        } else if (view.getId() == R.id.option_orders) {

            if (drawer.isDrawerOpen(GravityCompat.START)) {

                drawer.closeDrawer(GravityCompat.START);
            }

        } else if (view.getId() == R.id.option_menu) {

            startActivity(new Intent(OrdersActivity.this, MenuCategoryActivity.class));
            finish();

        } else if (view.getId() == R.id.option_transactions) {

            startActivity(new Intent(OrdersActivity.this, TransactionsActivity.class));
            finish();

        } else if (view.getId() == R.id.option_profile) {

            startActivity(new Intent(OrdersActivity.this, ProfileActivity.class));
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

                            Session.destroy(OrdersActivity.this);

                            startActivity(new Intent(OrdersActivity.this, LoginActivity.class));
                            finish();

                        })
                        .addOnFailureListener(e -> {

                            hideLoadingAnimation();

                            Toast.makeText(OrdersActivity.this,
                                    e.getMessage(), Toast.LENGTH_SHORT).show();

                            Log.d("laundry", "acceptTripRequest: " + e.getMessage());
                        });
            });

            alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

            alert.show();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(OrdersActivity.this, DashboardActivity.class));
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