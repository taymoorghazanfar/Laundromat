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
import com.laundromat.admin.model.Merchant;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.adapters.MerchantProfilePagerAdapter;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.ParseUtils;

public class MerchantProfileActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    // Variables
    private String merchantGson;
    private String laundryGson;
    private String transactionsGson;
    private int index;
    private Merchant merchant;

    // Views
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton buttonBack;
    private TextView textViewId;
    private TabLayout tabLayoutProfile;
    private ViewPager viewPagerProfile;
    private MerchantProfilePagerAdapter adapter;
    private RelativeLayout layoutLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_profile);

        getIntentData();

        initViews();
    }

    private void getIntentData() {

        merchantGson = getIntent().getStringExtra("merchant");
        laundryGson = getIntent().getStringExtra("laundry");
        transactionsGson = getIntent().getStringExtra("transactions");
        index = getIntent().getIntExtra("index", -1);

        merchant = GsonUtils.gsonToMerchant(merchantGson);
    }

    private void initViews() {

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        textViewId = findViewById(R.id.text_view_id);
        textViewId.setText(merchant.getId().substring(merchant.getId().length() - 10));

        tabLayoutProfile = findViewById(R.id.tab_layout_profile);
        viewPagerProfile = findViewById(R.id.view_pager_profile);

        initTabLayout();
    }

    private void initTabLayout() {

        adapter = new MerchantProfilePagerAdapter(

                getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                merchantGson,
                laundryGson,
                transactionsGson,
                index
        );

        viewPagerProfile.setAdapter(adapter);
        viewPagerProfile.setOffscreenPageLimit(3);

        tabLayoutProfile.setupWithViewPager(viewPagerProfile);
    }

    private void getMerchant() {

        Log.d("merchant", "getMerchant: " + merchant.getId());

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("merchant-getById")
                .call(merchant.getId())
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        merchant = ParseUtils
                                .parseMerchant(httpsCallableResult.getData());

                        Log.d("merchant", "getMerchant: after: " + merchant.getId());

                        merchantGson = GsonUtils.merchantToGson(merchant);
                        laundryGson = GsonUtils.laundryToGson(merchant.getLaundry());
                        transactionsGson = GsonUtils.transactionsToGson(merchant.getTransactions());

                        // save locally
                        Session.user.getMerchants().set(index, merchant);

                        initTabLayout();
                        hideLoadingAnimation();
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();
                    Log.d("merchant", "getMerchant: " + e.getMessage());
                    Toast.makeText(MerchantProfileActivity.this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();
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

        getMerchant();
        swipeRefreshLayout.setRefreshing(false);
    }
}