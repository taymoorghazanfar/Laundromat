package com.laundromat.admin.activities;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.admin.R;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.utils.StringUtils;
import com.laundromat.admin.utils.ValidationUtils;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity
        implements View.OnClickListener, TextWatcher {

    // Views
    private Toolbar toolbar;
    private DrawerLayout drawer;

    private RelativeLayout layoutLoading;

    private EditText editTextEmail;
    private EditText editTextBaseFare;
    private EditText editTextPerKm;
    private EditText editTextRadius;

    private Button buttonUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();

        setupViews();
    }

    private void initViews() {

        // setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        initDrawer();

        initDrawerMenu();

        editTextEmail = findViewById(R.id.edit_text_email);
        editTextEmail.addTextChangedListener(this);

        editTextBaseFare = findViewById(R.id.edit_text_base_fare);
        editTextBaseFare.addTextChangedListener(this);

        editTextPerKm = findViewById(R.id.edit_text_per_km);
        editTextPerKm.addTextChangedListener(this);

        editTextRadius = findViewById(R.id.edit_text_radius);
        editTextRadius.addTextChangedListener(this);

        buttonUpdate = findViewById(R.id.button_update);
        buttonUpdate.setOnClickListener(this);
    }

    private void setupViews() {

        buttonUpdate.setEnabled(false);

        String email = Session.user.getEmail();
        double baseFare = Session.user.getBaseFare();
        double perKmCharges = Session.user.getPerKmCharges();
        double radius = Session.user.getDeliveryRadius();

        editTextEmail.setText(email);
        editTextBaseFare.setText(String.valueOf(baseFare));
        editTextPerKm.setText(String.valueOf(perKmCharges));
        editTextRadius.setText(String.valueOf(radius));
    }

    private boolean enableUpdateButton() {

        String email = editTextEmail.getText().toString().trim();
        String baseFare = editTextBaseFare.getText().toString().trim();
        String perKmCharges = editTextPerKm.getText().toString().trim();
        String radius = editTextRadius.getText().toString().trim();

        // check empty
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(baseFare)
                || TextUtils.isEmpty(perKmCharges) || TextUtils.isEmpty(radius)) {

            return false;
        }

        double baseFareData = Double.parseDouble(baseFare);
        double perKmData = Double.parseDouble(perKmCharges);
        double radiusData = Double.parseDouble(radius);

        // check if values are valid
        if (!ValidationUtils.isEmailValid(email) || baseFareData <= 0
                || perKmData <= 0 || radiusData <= 0) {

            return false;
        }

        // check duplicate
        if (email.equals(Session.user.getEmail())
                && baseFareData == Session.user.getBaseFare()
                && perKmData == Session.user.getPerKmCharges()
                && radiusData == Session.user.getDeliveryRadius()) {

            return false;
        }

        return true;
    }

    private void updateSettings() {

        String email = editTextEmail.getText().toString().trim();
        String baseFare = editTextBaseFare.getText().toString().trim();
        String perKmCharges = editTextPerKm.getText().toString().trim();
        String radius = editTextRadius.getText().toString().trim();

        double baseFareData = Double.parseDouble(baseFare);
        double perKmData = Double.parseDouble(perKmCharges);
        double radiusData = Double.parseDouble(radius);

        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("base_fare", baseFareData);
        data.put("per_km", perKmData);
        data.put("radius", radiusData);

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("admin-updateSettings")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    hideLoadingAnimation();

                    // locally update data
                    Session.user.setEmail(email);
                    Session.user.setBaseFare(baseFareData);
                    Session.user.setPerKmCharges(perKmData);
                    Session.user.setDeliveryRadius(radiusData);

                    setupViews();

                    Toast.makeText(this, "Settings Updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Toast.makeText(this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("admin_settings", "updateSettings: " + e.getMessage());
                });
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

        View optionDashboard = findViewById(R.id.option_dashboard);
        optionDashboard.setOnClickListener(this);

        View optionNewMerchants = findViewById(R.id.option_new_merchants);
        optionNewMerchants.setOnClickListener(this);

        View optionNewDrivers = findViewById(R.id.option_new_drivers);
        optionNewDrivers.setOnClickListener(this);

        View optionAllMerchants = findViewById(R.id.option_merchants);
        optionAllMerchants.setOnClickListener(this);

        View optionAllDrivers = findViewById(R.id.option_drivers);
        optionAllDrivers.setOnClickListener(this);

        View optionCustomers = findViewById(R.id.option_customers);
        optionCustomers.setOnClickListener(this);

        View optionOrders = findViewById(R.id.option_orders);
        optionOrders.setOnClickListener(this);

        View optionTrips = findViewById(R.id.option_trips);
        optionTrips.setOnClickListener(this);

        View optionServiceTypes = findViewById(R.id.option_services);
        optionServiceTypes.setOnClickListener(this);

        View optionSettings = findViewById(R.id.option_config);
        optionSettings.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SettingsActivity.this,
                DashboardActivity.class));
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.option_dashboard:
                startActivity(new Intent(SettingsActivity.this,
                        DashboardActivity.class));
                finish();
                break;

            case R.id.option_new_merchants:
                startActivity(new Intent(SettingsActivity.this, NewMerchantsActivity.class));
                finish();

            case R.id.option_new_drivers:
                startActivity(new Intent(SettingsActivity.this, NewDriversActivity.class));
                finish();
                break;

            case R.id.option_merchants:
                startActivity(new Intent(SettingsActivity.this, MerchantsActivity.class));
                finish();
                break;

            case R.id.option_drivers:
                startActivity(new Intent(SettingsActivity.this, DriversActivity.class));
                finish();
                break;

            case R.id.option_customers:
                startActivity(new Intent(SettingsActivity.this, CustomersActivity.class));
                finish();
                break;

            case R.id.option_orders:
                startActivity(new Intent(SettingsActivity.this, OrdersActivity.class));
                finish();
                break;

            case R.id.option_trips:
                startActivity(new Intent(SettingsActivity.this, TripsActivity.class));
                finish();
                break;

            case R.id.option_services:
                startActivity(new Intent(SettingsActivity.this, ServiceTypesActivity.class));
                finish();
                break;

            case R.id.option_config:
                if (drawer.isDrawerOpen(GravityCompat.START)) {

                    drawer.closeDrawer(GravityCompat.START);
                }
                break;

            case R.id.option_logout:
                AlertDialog.Builder alert;
                alert = new AlertDialog.Builder(this);
                alert.setTitle("Logout ?");

                alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                    dialog.dismiss();
                    Session.destroy(SettingsActivity.this);

                    startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                    finish();
                });

                alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

                alert.show();

            case R.id.button_update:
                alert = new AlertDialog.Builder(this);
                alert.setTitle("Update Settings ?");

                alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                    dialog.dismiss();
                    updateSettings();

                });

                alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

                alert.show();

                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        buttonUpdate.setEnabled(enableUpdateButton());
    }

    @Override
    public void afterTextChanged(Editable editable) {

        String text;

        if (editTextEmail.getText().hashCode() == editable.hashCode()) {

            text = editTextEmail.getText().toString().trim();

            if (!ValidationUtils.isEmailValid(text)) {

                editTextEmail.setError("Email has invalid format");
            }

        } else if (editTextBaseFare.getText().hashCode() == editable.hashCode()) {

            text = editTextBaseFare.getText().toString().trim();

            if (!TextUtils.isEmpty(text)) {

                double baseFareData = Double.parseDouble(text);

                if (baseFareData <= 0) {

                    editTextBaseFare.setError("Base fare should be greater than 0");
                }
            }

        } else if (editTextPerKm.getText().hashCode() == editable.hashCode()) {

            text = editTextPerKm.getText().toString().trim();

            if (!TextUtils.isEmpty(text)) {

                double perKmData = Double.parseDouble(text);

                if (perKmData <= 0) {

                    editTextPerKm.setError("Per KM charges should be greater than 0");
                }
            }

        } else if (editTextRadius.getText().hashCode() == editable.hashCode()) {

            text = editTextRadius.getText().toString().trim();

            if (!TextUtils.isEmpty(text)) {

                double radiusData = Double.parseDouble(text);

                if (radiusData <= 0) {

                    editTextRadius.setError("Delivery radius should be greater than 0");
                }
            }
        }
    }

    private void showLoadingAnimation() {

        layoutLoading.setVisibility(View.VISIBLE);
        layoutLoading.bringToFront();
        layoutLoading.animate().translationY(0);
    }

    private void hideLoadingAnimation() {

        layoutLoading.setVisibility(View.GONE);
        layoutLoading.animate().translationY(layoutLoading.getHeight());
    }
}