package com.laundromat.customer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.transition.Slide;

import com.laundromat.customer.R;
import com.laundromat.customer.fragments.EnterPhoneFragment;
import com.laundromat.customer.fragments.OtpFragment;
import com.laundromat.customer.fragments.UpdatePasswordFragment;
import com.laundromat.customer.ui.interfaces.IOtpFragmentListener;
import com.laundromat.customer.ui.interfaces.IPasswordFragmentListener;
import com.laundromat.customer.ui.interfaces.IPhoneFragmentListener;

public class ForgotPasswordActivity extends AppCompatActivity
        implements View.OnClickListener, IPhoneFragmentListener,
        IOtpFragmentListener, IPasswordFragmentListener {

    // Views
    private ImageButton buttonBack;
    private FrameLayout frameLayout;
    private RelativeLayout layoutPasswordUpdated;
    private Button buttonLogin;

    // Variables
    private boolean passwordUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();

        EnterPhoneFragment enterPhoneFragment = new EnterPhoneFragment();
        enterPhoneFragment.setEnterTransition(new Slide(Gravity.END));
        enterPhoneFragment.setListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, enterPhoneFragment);
        fragmentTransaction.commit();
    }

    private void initViews() {

        frameLayout = findViewById(R.id.fragment_container);

        layoutPasswordUpdated = findViewById(R.id.layout_password_updated);
        layoutPasswordUpdated.setVisibility(View.GONE);

        buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(this);

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_back) {

            if (passwordUpdated) {

                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            } else {

                AlertDialog.Builder alert;
                alert = new AlertDialog.Builder(this);
                alert.setTitle("Abort reset password process ?");

                alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                    dialog.dismiss();
                    Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                });

                alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

                alert.show();
            }

        } else if (view.getId() == R.id.button_login) {

            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void onPhoneNumberEntered(String phoneNumber) {

        Bundle bundle = new Bundle();
        bundle.putString("phone_number", phoneNumber);

        OtpFragment otpFragment = new OtpFragment();
        otpFragment.setEnterTransition(new Slide(Gravity.END));
        otpFragment.setArguments(bundle);
        otpFragment.setListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, otpFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onOtpEntered(String phoneNumber) {

        Bundle bundle = new Bundle();
        bundle.putString("phone_number", phoneNumber);

        UpdatePasswordFragment updatePasswordFragment = new UpdatePasswordFragment();
        updatePasswordFragment.setEnterTransition(new Slide(Gravity.END));
        updatePasswordFragment.setArguments(bundle);
        updatePasswordFragment.setListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, updatePasswordFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onPasswordChangedListener() {

        passwordUpdated = true;

        frameLayout.setVisibility(View.GONE);
        layoutPasswordUpdated.setVisibility(View.VISIBLE);
        layoutPasswordUpdated.bringToFront();
        layoutPasswordUpdated.animate().translationY(0);
    }
}