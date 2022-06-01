package com.laundromat.delivery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.laundromat.delivery.R;
import com.laundromat.delivery.model.DeliveryBoy;
import com.laundromat.delivery.prefs.Session;
import com.laundromat.delivery.utils.ParseUtils;
import com.laundromat.delivery.utils.ValidationUtils;

import java.util.HashMap;
import java.util.Map;

import ss.anoop.awesometextinputlayout.AwesomeTextInputLayout;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    // Constants
    private static final int REQUEST_CODE_VERIFY_OTP = 69;

    // Variables
    boolean verifyPassword = false;
    String phoneNumber;
    String password;

    // Views
    private AwesomeTextInputLayout layoutPhoneNumber;
    private EditText editTextPhoneNumber;
    private AwesomeTextInputLayout layoutPassword;
    private EditText editTextPassword;
    private TextView textViewForgotPassword;
    private Button buttonLogin;
    private Button buttonSignup;
    private RelativeLayout layoutLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
    }

    private void initViews() {

        this.layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        this.layoutPhoneNumber = findViewById(R.id.layout_phone_number);
        this.editTextPhoneNumber = findViewById(R.id.edit_text_phone_number);
        editTextPhoneNumber.addTextChangedListener(this);

        this.layoutPassword = findViewById(R.id.layout_password);
        this.layoutPassword.setVisibility(View.GONE);

        this.editTextPassword = findViewById(R.id.edit_text_password);
        editTextPassword.addTextChangedListener(this);

        this.textViewForgotPassword = findViewById(R.id.text_view_forgot_password);
        this.textViewForgotPassword.setOnClickListener(this);

        this.buttonLogin = findViewById(R.id.button_login);
        this.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // first we verify phone number
                if (!verifyPassword) {

                    phoneNumber = editTextPhoneNumber.getText().toString().trim();

                    if (TextUtils.isEmpty(phoneNumber)) {

                        editTextPhoneNumber.setError("Enter your phone number");

                        return;
                    }

                    if (phoneNumber.length() < 10 || phoneNumber.length() > 11) {

                        editTextPhoneNumber.setError("Phone number should have atleast 10 and utmost 11 characters");

                        return;
                    }

                    if (phoneNumber.charAt(0) == '0') {

                        phoneNumber = phoneNumber.substring(1);
                    }

                    // check if phone number exist
                    Map<String, Object> data = new HashMap<>();
                    data.put("collection", "delivery_boys");
                    data.put("phone_number", phoneNumber);

                    showLoadingAnimation();

                    FirebaseFunctions
                            .getInstance()
                            .getHttpsCallable("delivery_boy-checkPhoneNumber")
                            .call(data)
                            .addOnSuccessListener(httpsCallableResult -> {

                                hideLoadingAnimation();
                                verifyPhoneNumber(phoneNumber);
                            })
                            .addOnFailureListener(e -> {

                                hideLoadingAnimation();
                                editTextPhoneNumber.setError(e.getMessage());
                                Log.d("signup", "initCustomer: login " + e.getMessage());
                            });

                    // then we verify password and phone and login
                } else {

                    password = editTextPassword.getText().toString().trim();

                    if (TextUtils.isEmpty(password)) {

                        Toast.makeText(LoginActivity.this,
                                "Enter your password", Toast.LENGTH_SHORT).show();

                        return;
                    }

                    verifyLoginCredentials(phoneNumber, password);
                }
            }
        });

        this.buttonSignup = findViewById(R.id.button_sign_up);
        this.buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                gotoSignupActivity();
            }
        });
    }

    private void gotoSignupActivity() {

        startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        finish();
    }

    private void verifyPhoneNumber(String phoneNumber) {

        Intent intent = new Intent(LoginActivity.this, VerifyOtpActivity.class);
        intent.putExtra("phone_number", phoneNumber);
        intent.putExtra("activity_id", "login_activity");

        startActivityForResult(intent, REQUEST_CODE_VERIFY_OTP);
    }

    private void verifyLoginCredentials(String phoneNumber, String password) {

        showLoadingAnimation();

        Map<String, Object> data = new HashMap<>();
        data.put("phone_number", phoneNumber);
        data.put("password", password);

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("delivery_boy-verifyLogin")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        // parse the retrieved user
                        DeliveryBoy deliveryBoy = ParseUtils
                                .parseDeliveryBoy(httpsCallableResult.getData());

                        Session.user = deliveryBoy;

                        // saving new logged in user's credentials to session
                        Session.setPhoneNumber(LoginActivity.this, deliveryBoy.getPhoneNumber());
                        Session.setPassword(LoginActivity.this, deliveryBoy.getPassword());

                        // update fcm token
                        FirebaseMessaging
                                .getInstance()
                                .getToken()
                                .addOnSuccessListener(token -> {

                                    HashMap<String, Object> data2 = new HashMap<>();
                                    data2.put("phone_number", Session.getPhoneNumber(getApplicationContext()));
                                    data2.put("fcm_token", token);

                                    FirebaseFunctions
                                            .getInstance()
                                            .getHttpsCallable("delivery_boy-setFcmToken")
                                            .call(data2)
                                            .addOnSuccessListener(httpsCallableResult1 -> {

                                                hideLoadingAnimation();

                                                // goto dashboard page
                                                gotoDashboard();

                                            })
                                            .addOnFailureListener(e -> {

                                                hideLoadingAnimation();
                                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                Log.d("login", "verifyLoginCredentials: " + e.getMessage());
                                            });
                                })
                                .addOnFailureListener(e -> {

                                    hideLoadingAnimation();
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.d("login", "verifyLoginCredentials: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("login", "verifyLoginCredentials: " + e.getMessage());
                });
    }

    private void gotoDashboard() {

        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));

        finish();
    }

    public void showLoadingAnimation() {

        layoutLoading.bringToFront();
        layoutLoading.setVisibility(View.VISIBLE);
    }

    public void hideLoadingAnimation() {

        layoutLoading.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_VERIFY_OTP && resultCode == RESULT_CANCELED) {

            //do nothing

        } else if (requestCode == REQUEST_CODE_VERIFY_OTP && resultCode == RESULT_OK) {

            verifyPassword = true;
            layoutPassword.setVisibility(View.VISIBLE);
            layoutPhoneNumber.setVisibility(View.GONE);
            buttonLogin.setText("Login");
            textViewForgotPassword.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.text_view_forgot_password) {

            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

        String text = "";

        if (editTextPhoneNumber.getText().hashCode() == editable.hashCode()) {

            text = editTextPhoneNumber.getText().toString().trim();

            if (text.length() < 10 || text.length() > 11) {

                editTextPhoneNumber.setError("Phone number should have atleast 10 and utmost 11 characters");

                return;
            }

            if (!ValidationUtils.isPhoneValid(text)) {

                editTextPhoneNumber.setError("Phone number has invalid format");
            }

        } else if (editTextPassword.getText().hashCode() == editable.hashCode()) {

            text = editTextPassword.getText().toString().trim();

            if (text.length() < 6) {

                editTextPassword.setError("Password length must be at least 6 characters");

                return;
            }

            if (!ValidationUtils.isPasswordValid(text)) {

                editTextPassword.setError("Password has invalid format");
            }

        }
    }
}