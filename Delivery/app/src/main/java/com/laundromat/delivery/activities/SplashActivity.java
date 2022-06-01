package com.laundromat.delivery.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.delivery.R;
import com.laundromat.delivery.prefs.Session;
import com.laundromat.delivery.utils.NetworkUtils;
import com.laundromat.delivery.utils.ParseUtils;
import com.laundromat.delivery.utils.PermissionUtils;

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
                    .getHttpsCallable("delivery_boy-verifyLogin")
                    .call(data)
                    .addOnSuccessListener(httpsCallableResult -> {

                        if (httpsCallableResult.getData() != null) {

                            // saving new logged in user to session
                            Session.user = ParseUtils
                                    .parseDeliveryBoy(httpsCallableResult.getData());

                            // start splash
                            Thread splashThread = new Thread() {

                                @Override
                                public void run() {
                                    try {
                                        sleep(SPLASH_DURATION);

                                        startActivity(new Intent(SplashActivity.this,
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
                        Log.d("splash", "startSplash: " + e.getMessage());
                    });
        }
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