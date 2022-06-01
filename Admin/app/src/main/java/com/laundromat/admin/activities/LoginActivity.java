package com.laundromat.admin.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.admin.R;
import com.laundromat.admin.model.Admin;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.utils.ParseUtils;

import java.util.HashMap;
import java.util.Map;

import ss.anoop.awesometextinputlayout.AwesomeTextInputLayout;

public class LoginActivity extends AppCompatActivity {

    // Views
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
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

        this.editTextUsername = findViewById(R.id.edit_text_username);

        this.editTextPassword = findViewById(R.id.edit_text_password);

        this.buttonLogin = findViewById(R.id.button_login);
        this.buttonLogin.setOnClickListener(view -> {

            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {

                Toast.makeText(LoginActivity.this,
                        "All fields are required", Toast.LENGTH_SHORT).show();

                return;
            }

            verifyLoginCredentials(username, password);
        });
    }

    private void verifyLoginCredentials(String username, String password) {

        showLoadingAnimation();

        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("password", password);

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("admin-verifyLogin")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    hideLoadingAnimation();

                    if (httpsCallableResult.getData() != null) {

                        // parse the retrieved user
                        Admin admin = ParseUtils
                                .parseAdmin(httpsCallableResult.getData());

                        Session.user = admin;

                        // saving new logged in user's credentials to session
                        Session.setUsername(LoginActivity.this, admin.getUsername());
                        Session.setPassword(LoginActivity.this, admin.getPassword());

                        getAllData();
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Log.d("login", "verifyLoginCredentials: verify: " + e.getMessage());
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void getAllData() {

        showLoadingAnimation();

        // get all new merchant registrations
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("admin-getNewRegistrationRequests")
                .call("merchant")
                .addOnSuccessListener(httpsCallableResult1 -> {

                    Session.user.setNewMerchants(ParseUtils
                            .parseMerchants(httpsCallableResult1.getData()));

                    Log.d("splash", "getAllData: new mer success");

                    // get all new delivery boys
                    FirebaseFunctions
                            .getInstance()
                            .getHttpsCallable("admin-getNewRegistrationRequests")
                            .call("delivery_boy")
                            .addOnSuccessListener(httpsCallableResult2 -> {

                                Session.user.setNewDeliveryBoys(ParseUtils
                                        .parseDeliveryBoys(httpsCallableResult2.getData()));

                                Log.d("splash", "getAllData: new del success");

                                // get all old merchants
                                FirebaseFunctions
                                        .getInstance()
                                        .getHttpsCallable("admin-getMerchants")
                                        .call()
                                        .addOnSuccessListener(httpsCallableResult3 -> {

                                            Session.user.setMerchants(ParseUtils
                                                    .parseMerchants(httpsCallableResult3.getData()));

                                            Log.d("splash", "getAllData: old mer success");

                                            // get all old delivery boys
                                            FirebaseFunctions
                                                    .getInstance()
                                                    .getHttpsCallable("admin-getDeliveryBoys")
                                                    .call()
                                                    .addOnSuccessListener(httpsCallableResult4 -> {

                                                        Session.user.setDeliveryBoys(ParseUtils
                                                                .parseDeliveryBoys(httpsCallableResult4.getData()));

                                                        Log.d("splash", "getAllData: old del success");

                                                        // get all customers
                                                        FirebaseFunctions
                                                                .getInstance()
                                                                .getHttpsCallable("admin-getCustomers")
                                                                .call()
                                                                .addOnSuccessListener(httpsCallableResult5 -> {

                                                                    Session.user.setCustomers(ParseUtils
                                                                            .parseCustomers(httpsCallableResult5.getData()));

                                                                    Log.d("splash", "getAllData: cus success");

                                                                    // get all orders
                                                                    FirebaseFunctions
                                                                            .getInstance()
                                                                            .getHttpsCallable("admin-getOrders")
                                                                            .call()
                                                                            .addOnSuccessListener(httpsCallableResult6 -> {

                                                                                Session.user.setOrders(ParseUtils
                                                                                        .parseOrders(httpsCallableResult6.getData()));

                                                                                Log.d("splash", "getAllData: ord success");

                                                                                // get all service types
                                                                                FirebaseFunctions
                                                                                        .getInstance()
                                                                                        .getHttpsCallable("admin-getServiceTypes")
                                                                                        .call()
                                                                                        .addOnSuccessListener(httpsCallableResult7 -> {

                                                                                            Session.user.setServiceTypes(ParseUtils
                                                                                                    .parseServiceTypes(httpsCallableResult7.getData()));

                                                                                            Log.d("splash", "getAllData: ser success");

                                                                                            hideLoadingAnimation();
                                                                                            // goto dashboard
                                                                                            gotoDashboard();
                                                                                        })
                                                                                        .addOnFailureListener(e -> {

                                                                                            Log.d("splash", "getAllData: " + e.getMessage());
                                                                                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                        });
                                                                            })
                                                                            .addOnFailureListener(e -> {

                                                                                Log.d("splash", "getAllData: " + e.getMessage());
                                                                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                            });
                                                                })
                                                                .addOnFailureListener(e -> {

                                                                    Log.d("splash", "getAllData: " + e.getMessage());
                                                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                });
                                                    })
                                                    .addOnFailureListener(e -> {

                                                        Log.d("splash", "getAllData: " + e.getMessage());
                                                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        e.printStackTrace();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {

                                            Log.d("splash", "getAllData: " + e.getMessage());
                                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {

                                Log.d("splash", "getAllData: " + e.getMessage());
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {

                    Log.d("splash", "getAllData: " + e.getMessage());
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}