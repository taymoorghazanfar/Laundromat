package com.laundromat.admin.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.admin.R;
import com.laundromat.admin.model.DeliveryBoy;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.adapters.DriverProfilePagerAdapter;
import com.laundromat.admin.ui.adapters.MerchantProfilePagerAdapter;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.ParseUtils;

public class DriverProfileActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    // Variables
    private String driverGson;
    private String vehicleGson;
    private String transactionsGson;
    private int index;
    private DeliveryBoy driver;

    // Views
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton buttonBack;
    private TextView textViewId;
    private TabLayout tabLayoutProfile;
    private ViewPager viewPagerProfile;
    private DriverProfilePagerAdapter adapter;
    private RelativeLayout layoutLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);

        getIntentData();

        initViews();
    }

    private void getIntentData() {

        driverGson = getIntent().getStringExtra("driver");
        vehicleGson = getIntent().getStringExtra("vehicle");
        transactionsGson = getIntent().getStringExtra("transactions");
        index = getIntent().getIntExtra("index", -1);

        driver = GsonUtils.gsonToDriver(driverGson);
    }

    private void initViews() {

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        textViewId = findViewById(R.id.text_view_id);
        textViewId.setText(driver.getId().substring(driver.getId().length() - 10));

        tabLayoutProfile = findViewById(R.id.tab_layout_profile);
        viewPagerProfile = findViewById(R.id.view_pager_profile);

        initTabLayout();
    }

    private void initTabLayout() {

        adapter = new DriverProfilePagerAdapter(

                getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                driverGson,
                vehicleGson,
                transactionsGson,
                index
        );

        viewPagerProfile.setAdapter(adapter);
        viewPagerProfile.setOffscreenPageLimit(3);

        tabLayoutProfile.setupWithViewPager(viewPagerProfile);
    }

    private void getDriver() {

        showLoadingAnimation();

        Log.d("delivery_boy", "getDriver: " + driver.getId());
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("delivery_boy-getById")
                .call(driver.getId())
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        driver = ParseUtils.parseDriver(httpsCallableResult.getData());
                        driverGson = GsonUtils.driverToGson(driver);
                        vehicleGson = GsonUtils.vehicleToGson(driver.getVehicle());
                        transactionsGson = GsonUtils.transactionsToGson(driver.getTransactions());

                        // save locally
                        Session.user.getDeliveryBoys().set(index, driver);

                        Log.d("delivery_boy", "getDriver: after " + driver.getId());

                        initTabLayout();
                        hideLoadingAnimation();
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("checkout", "placeOrder: " + e.getMessage());

                });
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

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_back) {

            finish();
        }
    }

    @Override
    public void onRefresh() {

        getDriver();
        swipeRefreshLayout.setRefreshing(false);
    }
}