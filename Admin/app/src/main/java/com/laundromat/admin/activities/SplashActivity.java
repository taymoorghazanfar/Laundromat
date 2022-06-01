package com.laundromat.admin.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.admin.R;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.utils.NetworkUtils;
import com.laundromat.admin.utils.ParseUtils;
import com.laundromat.admin.utils.PermissionUtils;

import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 69;
    private static final long SPLASH_DURATION = 1000;

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
            data.put("username", Session.getUsername(SplashActivity.this));
            data.put("password", Session.getPassword(SplashActivity.this));

            FirebaseFunctions
                    .getInstance()
                    .getHttpsCallable("admin-verifyLogin")
                    .call(data)
                    .addOnSuccessListener(httpsCallableResult -> {

                        if (httpsCallableResult.getData() != null) {

                            // saving new logged in user to session
                            Session.user = ParseUtils
                                    .parseAdmin(httpsCallableResult.getData());

                            getAllData();
                        }
                    })
                    .addOnFailureListener(e -> {

                        Toast.makeText(SplashActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void getAllData() {

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

                                                                                            // goto dashboard
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