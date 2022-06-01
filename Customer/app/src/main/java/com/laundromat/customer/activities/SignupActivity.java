package com.laundromat.customer.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.laundromat.customer.R;
import com.laundromat.customer.model.Customer;
import com.laundromat.customer.model.util.Location;
import com.laundromat.customer.prefs.Session;
import com.laundromat.customer.utils.ImageUtils;
import com.laundromat.customer.utils.ValidationUtils;

import java.util.ArrayList;
import java.util.Map;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    private static final int REQUEST_CODE_LOCATION = 111;
    private static final int REQUEST_CODE_AVATAR = 222;
    private static final int REQUEST_CODE_VERIFY_OTP = 333;
    private static final int DESTINATION_ID = 1;
    private ImageView imageViewAvatar;
    private EditText editTextFullName;
    private EditText editTextPhoneNumber;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private EditText editTextLocation;
    private AppCompatButton buttonSignup;
    private RelativeLayout layoutLoading;

    // Variables
    private Uri avatarUri;
    private LatLng locationLatLng;
    private Customer customer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initViews();
    }

    private void initViews() {

        imageViewAvatar = findViewById(R.id.image_view_avatar);

        editTextFullName = findViewById(R.id.edit_text_full_name);
        editTextFullName.addTextChangedListener(this);

        editTextPhoneNumber = findViewById(R.id.edit_text_phone_number);
        editTextPhoneNumber.addTextChangedListener(this);

        editTextEmail = findViewById(R.id.edit_text_email);
        editTextEmail.addTextChangedListener(this);

        editTextPassword = findViewById(R.id.edit_text_password);
        editTextPassword.addTextChangedListener(this);

        editTextConfirmPassword = findViewById(R.id.edit_text_confirm_password);
        editTextConfirmPassword.addTextChangedListener(this);

        editTextLocation = findViewById(R.id.edit_text_location);
        buttonSignup = findViewById(R.id.button_signup);
        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        imageViewAvatar.setOnClickListener(this);
        editTextLocation.setOnClickListener(this);
        buttonSignup.setOnClickListener(this);
    }

    private void getImageFromUser(int requestCode) {

        ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .compress(1024)
                .start(requestCode);
    }

    private void getLocationFromUser() {

        Intent intent = new Intent(SignupActivity.this, PickLocationActivity.class);
        intent.putExtra(PickLocationActivity.FORM_VIEW_INDICATOR, DESTINATION_ID);

        startActivityForResult(intent, REQUEST_CODE_LOCATION);
    }

    private void initCustomer() {

        String fullName = editTextFullName.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // check empty
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(phoneNumber)
                || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(confirmPassword)
                || avatarUri == null) {

            Toast.makeText(this,
                    "All fields must be filled", Toast.LENGTH_SHORT).show();

            return;
        }

        if (!ValidationUtils.isNameValid(fullName)) {

            editTextFullName.setError("Full name has invalid format");

            return;
        }

        if (phoneNumber.length() < 10 || phoneNumber.length() > 11) {

            editTextPhoneNumber.setError("Phone number should have atleast 10 and utmost 11 characters");

            return;
        }

        if (!ValidationUtils.isPhoneValid(phoneNumber)) {

            editTextPhoneNumber.setError("Phone number has invalid format");

            return;
        }

        if (!ValidationUtils.isEmailValid(email)) {

            editTextEmail.setError("Email is invalid");

            return;
        }

        if (password.length() < 6) {

            editTextPassword.setError("Password length must be at least 6 characters");

            return;
        }

        if (!ValidationUtils.isPasswordValid(password)) {

            editTextPassword.setError("Password has invalid format");

            return;
        }

        if (confirmPassword.length() < 6) {

            editTextConfirmPassword.setError("Password length must be at least 6 characters");

            return;
        }

        if (!ValidationUtils.isPasswordValid(confirmPassword)) {

            editTextConfirmPassword.setError("Password has invalid format");

            return;
        }

        if (!confirmPassword.equals(password)) {

            editTextConfirmPassword.setError("Passwords do not match");
            return;
        }

        if (phoneNumber.charAt(0) == '0') {

            phoneNumber = phoneNumber.substring(1);
        }

        String avatar64 = ImageUtils.uriToBase64(this, avatarUri);

        Location location = new Location("Home", locationLatLng);
        ArrayList<Location> locations = new ArrayList<>();
        locations.add(location);

        customer = new Customer();

        //todo: set customer fcm token
        showLoadingAnimation();
        String finalPhoneNumber = phoneNumber;
        FirebaseMessaging
                .getInstance()
                .getToken()
                .addOnSuccessListener(token -> {

                    hideLoadingAnimation();

                    customer.setFcmToken(token);
                    customer.setFullName(fullName);
                    customer.setPhoneNumber(finalPhoneNumber);
                    customer.setEmail(email);
                    customer.setPassword(password);
                    customer.setLocation(locations.get(0).getLatLng());
                    customer.setLocations(locations);
                    customer.setAvatarUrl(avatar64);

                    verifyNewCustomerData();
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();
                    Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("signup", "initCustomer: get fcm " + e.getMessage());
                    e.printStackTrace();

                });
    }

    private void verifyNewCustomerData() {

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("customer-verifyNewCustomerData")
                .call(customer.toJson())
                .addOnSuccessListener(httpsCallableResult -> {

                    hideLoadingAnimation();

                    if (httpsCallableResult.getData() != null) {

                        boolean exist = (boolean) httpsCallableResult.getData();

                        if (!exist) {

                            verifyPhoneNumber();
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("signup", "initCustomer: verify data " + e.getMessage());
                    e.printStackTrace();
                });
    }

    private void verifyPhoneNumber() {

        Intent intent = new Intent(SignupActivity.this, VerifyOtpActivity.class);
        intent.putExtra("phone_number", customer.getPhoneNumber());
        intent.putExtra("activity_id", "login_activity");

        startActivityForResult(intent, REQUEST_CODE_VERIFY_OTP);
    }

    private void registerCustomer() {

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("customer-createNewCustomer")
                .call(customer.toJson())
                .addOnSuccessListener(httpsCallableResult -> {

                    Map<String, Object> data = (Map<String, Object>) httpsCallableResult.getData();

                    String customerId = (String) data.get("customerId");
                    String avatarDownloadUrl = (String) data.get("downloadUrl");

                    customer.setId(customerId);
                    customer.setAvatarUrl(avatarDownloadUrl);

                    Session.user = customer;
                    Session.setPhoneNumber(SignupActivity.this, customer.getPhoneNumber());
                    Session.setPassword(SignupActivity.this, customer.getPassword());

                    hideLoadingAnimation();

                    gotoDashboard();

                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    Log.d("signup", "initCustomer: signup complete" + e.getMessage());
                    e.printStackTrace();
                });
    }

    private void gotoDashboard() {

        startActivity(new Intent(SignupActivity.this, LaundriesActivity.class));

        finish();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_AVATAR && resultCode == Activity.RESULT_OK) {

            if (data != null) {

                avatarUri = data.getData();
                imageViewAvatar.setImageURI(avatarUri);
            }
        } else if (requestCode == REQUEST_CODE_LOCATION && resultCode == Activity.RESULT_OK) {

            if (data != null) {

                String locationName = data.getStringExtra(PickLocationActivity.LOCATION_NAME);
                locationLatLng = data.getParcelableExtra(PickLocationActivity.LOCATION_LAT_LONG);

                editTextLocation.setText(locationName);
            }
        } else if (requestCode == REQUEST_CODE_VERIFY_OTP && resultCode == Activity.RESULT_OK) {

            registerCustomer();
        }
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.image_view_avatar) {

            getImageFromUser(REQUEST_CODE_AVATAR);

        } else if (view.getId() == R.id.edit_text_location) {

            getLocationFromUser();

        } else if (view.getId() == R.id.button_signup) {

            initCustomer();
        }
    }

    // text watcher
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

        String text = "";

        if (editTextFullName.getText().hashCode() == editable.hashCode()) {

            text = editTextFullName.getText().toString().trim();

            if (!ValidationUtils.isNameValid(text)) {

                editTextFullName.setError("Full name has invalid format");
            }

        } else if (editTextPhoneNumber.getText().hashCode() == editable.hashCode()) {

            text = editTextPhoneNumber.getText().toString().trim();

            if (text.length() < 10 || text.length() > 11) {

                editTextPhoneNumber.setError("Phone number should have atleast 10 and utmost 11 characters");

                return;
            }

            if (!ValidationUtils.isPhoneValid(text)) {

                editTextPhoneNumber.setError("Phone number has invalid format");
            }

        } else if (editTextEmail.getText().hashCode() == editable.hashCode()) {

            text = editTextEmail.getText().toString().trim();

            if (!ValidationUtils.isEmailValid(text)) {

                editTextEmail.setError("Email has invalid format");
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

        } else if (editTextConfirmPassword.getText().hashCode() == editable.hashCode()) {

            text = editTextConfirmPassword.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (text.length() < 6) {

                editTextConfirmPassword.setError("Password length must be at least 6 characters");

                return;
            }

            if (!ValidationUtils.isPasswordValid(text)) {

                editTextConfirmPassword.setError("Password has invalid format");

                return;
            }

            if (!text.equals(password)) {

                editTextConfirmPassword.setError("Passwords do not match");
            }
        }
    }
}