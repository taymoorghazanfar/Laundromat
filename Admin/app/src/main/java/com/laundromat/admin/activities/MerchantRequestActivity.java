package com.laundromat.admin.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.admin.R;
import com.laundromat.admin.model.Merchant;
import com.laundromat.admin.ui.adapters.MerchantRequestPagerAdapter;
import com.laundromat.admin.utils.GsonUtils;

import java.util.HashMap;

public class MerchantRequestActivity extends AppCompatActivity
        implements View.OnClickListener {

    // Variables
    private String merchantGson;
    private String laundryGson;
    private Merchant merchant;
    private boolean requestAlreadyHandled = false;

    // Views
    private ImageButton buttonBack;
    private RelativeLayout layoutLoading;
    private RelativeLayout layoutHandled;
    private TabLayout tabLayoutProfile;
    private ViewPager viewPagerProfile;
    private MerchantRequestPagerAdapter adapter;

    private AppCompatButton buttonAccept;
    private AppCompatButton buttonDecline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_request);

        getIntentData();

        initViews();

        // check if request has already been handled
        HashMap<String, Object> data = new HashMap<>();
        data.put("collection", "new_merchants");
        data.put("id", merchant.getId());

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("admin-checkRequestHandled")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    hideLoadingAnimation();

                    initTabLayout();
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    requestAlreadyHandled = true;
                    showHandledLayout();
                });
    }

    private void getIntentData() {

        merchantGson = getIntent().getStringExtra("merchant");
        laundryGson = getIntent().getStringExtra("laundry");

        merchant = GsonUtils.gsonToMerchant(merchantGson);
    }

    private void initViews() {

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        layoutHandled = findViewById(R.id.layout_handled);
        layoutHandled.setVisibility(View.GONE);

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        buttonAccept = findViewById(R.id.button_accept);
        buttonAccept.setOnClickListener(this);

        buttonDecline = findViewById(R.id.button_decline);
        buttonDecline.setOnClickListener(this);

        viewPagerProfile = findViewById(R.id.view_pager_profile);
        tabLayoutProfile = findViewById(R.id.tab_layout_profile);
    }

    private void initTabLayout() {

        adapter = new MerchantRequestPagerAdapter(
                getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                merchantGson,
                laundryGson);

        viewPagerProfile.setAdapter(adapter);
        viewPagerProfile.setOffscreenPageLimit(2);

        tabLayoutProfile.setupWithViewPager(viewPagerProfile);
    }

    private void acceptMerchantRegistration() {

        //todo: check if request has already been handled
        HashMap<String, Object> data = new HashMap<>();
        data.put("collection", "new_merchants");
        data.put("id", merchant.getId());

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("admin-checkRequestHandled")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    // handle registration request
                    data.clear();
                    data.put("entity_type", "merchant");
                    data.put("user", merchant.toJson());

                    FirebaseFunctions
                            .getInstance()
                            .getHttpsCallable("admin-acceptRegistrationRequest")
                            .call(data)
                            .addOnSuccessListener(httpsCallableResult2 -> {

                                hideLoadingAnimation();

                                Intent intent = new Intent();
                                intent.putExtra("task", "accept");

                                setResult(RESULT_OK, intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {

                                hideLoadingAnimation();
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    requestAlreadyHandled = true;
                    showHandledLayout();
                });
    }

    private void declineMerchantRegistration() {

        //todo: check if request has already been handled
        HashMap<String, Object> data = new HashMap<>();
        data.put("collection", "new_merchants");
        data.put("id", merchant.getId());

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("admin-checkRequestHandled")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    data.clear();
                    data.put("entity_type", "merchant");
                    data.put("user", merchant.toJson());

                    FirebaseFunctions
                            .getInstance()
                            .getHttpsCallable("admin-declineRegistrationRequest")
                            .call(data)
                            .addOnSuccessListener(httpsCallableResult2 -> {

                                hideLoadingAnimation();

                                Intent intent = new Intent();
                                intent.putExtra("task", "decline");

                                setResult(RESULT_OK, intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {

                                hideLoadingAnimation();
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    requestAlreadyHandled = true;
                    showHandledLayout();
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

    public void showHandledLayout() {

        layoutHandled.setVisibility(View.VISIBLE);
        layoutHandled.bringToFront();
        layoutHandled.animate().translationY(0);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_back) {

            if (requestAlreadyHandled) {

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
            }

            finish();

        } else if (view.getId() == R.id.button_accept) {

            AlertDialog.Builder alert;
            alert = new AlertDialog.Builder(this);
            alert.setTitle("Accept registration request ?");

            alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                dialog.dismiss();
                acceptMerchantRegistration();
            });

            alert.setNegativeButton("No", (dialogInterface, i) ->
                    dialogInterface.dismiss());

            alert.show();
        } else if (view.getId() == R.id.button_decline) {

            AlertDialog.Builder alert;
            alert = new AlertDialog.Builder(this);
            alert.setTitle("Decline registration request ?");

            alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                dialog.dismiss();
                declineMerchantRegistration();
            });

            alert.setNegativeButton("No", (dialogInterface, i) ->
                    dialogInterface.dismiss());

            alert.show();
        }
    }
}