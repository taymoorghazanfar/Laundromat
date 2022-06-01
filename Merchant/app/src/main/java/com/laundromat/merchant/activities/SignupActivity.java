package com.laundromat.merchant.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.laundromat.merchant.R;
import com.laundromat.merchant.model.Laundry;
import com.laundromat.merchant.model.Merchant;
import com.laundromat.merchant.ui.adapters.SignupViewPagerAdapter;
import com.laundromat.merchant.ui.interfaces.ISignupListener;
import com.laundromat.merchant.ui.interfaces.ILaundrySignupListener;
import com.laundromat.merchant.ui.interfaces.IMerchantSignupListener;

public class SignupActivity extends AppCompatActivity
        implements IMerchantSignupListener, ILaundrySignupListener {

    private static final int REQUEST_CODE_VERIFY_OTP = 69;
    // Interfaces
    ISignupListener iSignupListener;
    // Views
    private ViewPager viewPagerForms;
    private SignupViewPagerAdapter signupViewPagerAdapter;
    private RelativeLayout layoutLoading;
    // Variables
    private Merchant merchant;
    private Laundry laundry;

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
    public void onMerchantSignup(Merchant merchant) {

        viewPagerForms.setCurrentItem(1);
        this.merchant = merchant;
    }

    @Override
    public void onLaundrySignup(Laundry laundry) {

        this.laundry = laundry;

        //todo: goto verify OTP activity
        Intent intent = new Intent(SignupActivity.this, VerifyOtpActivity.class);
        intent.putExtra("phone_number", merchant.getPhoneNumber());
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

                this.iSignupListener.onSignupComplete(this.merchant, this.laundry);
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