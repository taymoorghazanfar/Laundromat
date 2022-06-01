package com.laundromat.admin.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.admin.R;
import com.laundromat.admin.model.Customer;
import com.laundromat.admin.model.order.OrderStatus;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.adapters.CustomerProfilePagerAdapter;
import com.laundromat.admin.ui.adapters.MerchantProfilePagerAdapter;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.ParseUtils;

public class CustomerProfileActivity extends
        AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    // Variables
    private String customerGson;
    private String transactionsGson;
    private int index;
    private Customer customer;

    // Views
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton buttonBack;
    private TextView textViewId;
    private TabLayout tabLayoutProfile;
    private ViewPager viewPagerProfile;
    private CustomerProfilePagerAdapter adapter;
    private RelativeLayout layoutLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        getIntentData();

        initViews();
    }

    private void getIntentData() {

        customerGson = getIntent().getStringExtra("customer");
        transactionsGson = getIntent().getStringExtra("transactions");
        index = getIntent().getIntExtra("index", -1);

        customer = GsonUtils.gsonToCustomer(customerGson);
    }

    private void initViews() {

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        textViewId = findViewById(R.id.text_view_id);
        textViewId.setText(customer.getId().substring(customer.getId().length() - 10));

        tabLayoutProfile = findViewById(R.id.tab_layout_profile);
        viewPagerProfile = findViewById(R.id.view_pager_profile);

        initTabLayout();
    }

    private void initTabLayout() {

        adapter = new CustomerProfilePagerAdapter(

                getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                customerGson,
                transactionsGson,
                index
        );

        viewPagerProfile.setAdapter(adapter);
        viewPagerProfile.setOffscreenPageLimit(2);

        tabLayoutProfile.setupWithViewPager(viewPagerProfile);
    }

    private void getCustomer() {

        showLoadingAnimation();
        Log.d("delivery_boy", "getDriver: " + customer.getId());
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("customer-getCustomerById")
                .call(customer.getId())
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        customer = ParseUtils.parseCustomer(httpsCallableResult.getData());
                        customerGson = GsonUtils.customerToGson(customer);
                        transactionsGson = GsonUtils.transactionsToGson(customer.getTransactions());

                        // save locally
                        Session.user.getCustomers().set(index, customer);

                        Log.d("delivery_boy", "getDriver: after " + customer.getId());

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

        getCustomer();
        swipeRefreshLayout.setRefreshing(false);
    }
}