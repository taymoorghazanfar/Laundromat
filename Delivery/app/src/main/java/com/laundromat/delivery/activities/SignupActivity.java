package com.laundromat.delivery.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.laundromat.delivery.R;
import com.laundromat.delivery.model.DeliveryBoy;
import com.laundromat.delivery.model.Laundry;
import com.laundromat.delivery.model.Merchant;
import com.laundromat.delivery.model.Vehicle;
import com.laundromat.delivery.ui.adapters.SignupViewPagerAdapter;
import com.laundromat.delivery.ui.interfaces.ISignupListener;
import com.laundromat.delivery.ui.interfaces.IVehicleSignupListener;
import com.laundromat.delivery.ui.interfaces.IDeliveryBoySignupListener;

public class SignupActivity extends AppCompatActivity
        implements IDeliveryBoySignupListener, IVehicleSignupListener {

    private static final int REQUEST_CODE_VERIFY_OTP = 69;
    // Interfaces
    ISignupListener iSignupListener;
    // Views
    private ViewPager viewPagerForms;
    private SignupViewPagerAdapter signupViewPagerAdapter;
    private RelativeLayout layoutLoading;
    // Variables
    private DeliveryBoy deliveryBoy;
    private Vehicle vehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initViews();
    }

    public void setICompleteSignup(ISignupListener iSignupListener) {
        this.iSignupListener = iSignupListener;
    }

    private void initViews() {

        viewPagerForms = findViewById(R.id.view_pager_forms);
        signupViewPagerAdapter = new SignupViewPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPagerForms.setAdapter(signupViewPagerAdapter);
        viewPagerForms.setOffscreenPageLimit(2);

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);
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
    public void onDeliveryBoySignup(DeliveryBoy deliveryBoy) {

        viewPagerForms.setCurrentItem(1);
        this.deliveryBoy = deliveryBoy;
    }

    @Override
    public void onVehicleSignup(Vehicle vehicle) {

        this.vehicle = vehicle;

        Intent intent = new Intent(SignupActivity.this, VerifyOtpActivity.class);
        intent.putExtra("phone_number", deliveryBoy.getPhoneNumber());
        intent.putExtra("activity_id", "sign_up_activity");

        startActivityForResult(intent, REQUEST_CODE_VERIFY_OTP);
    }

    @Override
    public void onButtonPreviousClick() {

        viewPagerForms.setCurrentItem(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_VERIFY_OTP
                && resultCode == RESULT_CANCELED) {

            viewPagerForms.setCurrentItem(0);

        } else if (requestCode == REQUEST_CODE_VERIFY_OTP && resultCode == RESULT_OK) {

            if (this.iSignupListener != null) {

                this.iSignupListener.onSignupComplete(this.deliveryBoy, this.vehicle);
                viewPagerForms.setCurrentItem(2);
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (viewPagerForms != null) {

            if (viewPagerForms.getCurrentItem() == 0) {

                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();

            } else if (viewPagerForms.getCurrentItem() == 1) {

                viewPagerForms.setCurrentItem(0);

            } else {

                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }

        } else {

            super.onBackPressed();
        }
    }
}