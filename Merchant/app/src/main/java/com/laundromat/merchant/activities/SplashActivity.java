package com.laundromat.merchant.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.merchant.R;
import com.laundromat.merchant.model.Customer;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.utils.Globals;
import com.laundromat.merchant.utils.NetworkUtils;
import com.laundromat.merchant.utils.ParseUtils;
import com.laundromat.merchant.utils.PermissionUtils;

import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 69;
    private static final long SPLASH_DURATION = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkInternet();
    }

    private void requestPermissions() {

        if (!PermissionUtils.getRequestedPermissions(this).isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    PermissionUtils.getRequestedPermissions(this).toArray
                            (new String[0]), PERMISSION_REQUEST_CODE);
        } else {

            startSplash();
        }
    }

    private void startSplash() {

        // get services types
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("admin-getServiceTypes")
                .call()
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        Globals.setAvailableServices(ParseUtils
                                .parseServiceTypes(httpsCallableResult.getData()));

                        if (!Session.userExist(SplashActivity.this)) {

                            Thread splashThread = new Thread() {

                                @Override
                                public void run() {
                                    try {
                                        sleep(SPLASH_DURATION);

                                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));

                                        finish();

                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };

                            splashThread.start();

                        } else {

                            // get logged in user by phone and password from server
                            Map<String, Object> data = new HashMap<>();
                            data.put("phone_number", Session.getPhoneNumber(SplashActivity.this));
                            data.put("password", Session.getPassword(SplashActivity.this));

                            FirebaseFunctions
                                    .getInstance()
                                    .getHttpsCallable("merchant-verifyLogin")
                                    .call(data)
                                    .addOnSuccessListener(httpsCallableResult2 -> {

                                        if (httpsCallableResult2.getData() != null) {

                                            // saving new logged in user to session
                                            Session.user = ParseUtils
                                                    .parseMerchant(httpsCallableResult2.getData());

                                            // start splash
                                            Thread splashThread = new Thread() {

                                                @Override
                                                public void run() {
                                                    try {
                                                        sleep(SPLASH_DURATION);

                                                        startActivity(new
                                                                Intent(SplashActivity.this,
                                                                DashboardActivity.class));

                                                        finish();

                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            };

                                            splashThread.start();
                                        }
                                    })
                                    .addOnFailureListener(e -> {

                                        Toast.makeText(SplashActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(SplashActivity.this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showErrorDialog(String message) {

        AlertDialog.Builder alert;
        alert = new AlertDialog.Builder(this);
        alert.setTitle("Permissions Denied");
        alert.setMessage(message);

        alert.setPositiveButton("OK", (dialog, whichButton) -> finishAffinity());

        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {

            boolean permissionsGranted = true;

            if (grantResults.length > 0) {

                for (int grantResult : grantResults) {

                    if (grantResult != PackageManager.PERMISSION_GRANTED) {

                        permissionsGranted = false;
                        break;
                    }
                }

                if (permissionsGranted) {

                    startSplash();

                } else {

                    showErrorDialog(getString(R.string.splash_error_1));
                }
            } else {

                showErrorDialog(getString(R.string.splash_error_1));
            }
        }
    }

    private void checkInternet() {

        if (!NetworkUtils.isInternetConnected(this)) {

            android.app.AlertDialog.Builder infoBuilder = new android.app.AlertDialog.Builder(this);
            infoBuilder.setTitle("No internet connection. Connect to a network and try again");

            infoBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    dialogInterface.dismiss();
                    finishAffinity();
                }
            });

            android.app.AlertDialog infoDialog = infoBuilder.create();
            infoDialog.setCancelable(false);
            infoDialog.show();

        } else {

            requestPermissions();
        }
    }
}