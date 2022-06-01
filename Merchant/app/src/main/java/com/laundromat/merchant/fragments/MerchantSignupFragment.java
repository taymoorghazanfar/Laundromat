package com.laundromat.merchant.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.laundromat.merchant.R;
import com.laundromat.merchant.activities.PickLocationActivity;
import com.laundromat.merchant.activities.SignupActivity;
import com.laundromat.merchant.model.Merchant;
import com.laundromat.merchant.ui.interfaces.IMerchantSignupListener;
import com.laundromat.merchant.utils.ImageUtils;
import com.laundromat.merchant.utils.StringUtils;
import com.laundromat.merchant.utils.ValidationUtils;

public class MerchantSignupFragment extends Fragment implements View.OnClickListener, TextWatcher {

    private static final int REQUEST_CODE_AVATAR = 111;
    private static final int REQUEST_CODE_NIC = 222;
    private static final int REQUEST_CODE_LOCATION = 333;
    private static final int DESTINATION_ID = 1;
    IMerchantSignupListener iMerchantSignupListener;

    // Widgets
    private ImageView imageViewAvatar;
    private ImageView imageViewNic;
    private EditText editTextFullName;
    private EditText editTextPhoneNumber;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private EditText editTextNicNumber;
    private EditText editTextJazzCashNumber;
    private EditText editTextLocation;
    private Button buttonProceed;

    //Variables
    private Uri avatarUri, nicImageUri;
    private LatLng location;

    public MerchantSignupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_merchant_signup, container, false);

        initViews(view);

        return view;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        try {
            iMerchantSignupListener = (IMerchantSignupListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " Must implement IMerchantSignup");
        }
    }

    private void initViews(View view) {

        imageViewAvatar = view.findViewById(R.id.image_view_avatar);
        imageViewNic = view.findViewById(R.id.image_view_nic);

        editTextFullName = view.findViewById(R.id.edit_text_full_name);
        editTextFullName.addTextChangedListener(this);

        editTextPhoneNumber = view.findViewById(R.id.edit_text_phone_number);
        editTextPhoneNumber.addTextChangedListener(this);

        editTextEmail = view.findViewById(R.id.edit_text_email);
        editTextEmail.addTextChangedListener(this);

        editTextPassword = view.findViewById(R.id.edit_text_password);
        editTextPassword.addTextChangedListener(this);

        editTextConfirmPassword = view.findViewById(R.id.edit_text_confirm_password);
        editTextConfirmPassword.addTextChangedListener(this);

        editTextNicNumber = view.findViewById(R.id.edit_text_nic_number);
        editTextNicNumber.addTextChangedListener(this);

        editTextJazzCashNumber = view.findViewById(R.id.edit_text_jazz_cash_number);
        editTextJazzCashNumber.addTextChangedListener(this);

        editTextLocation = view.findViewById(R.id.edit_text_location);
        buttonProceed = view.findViewById(R.id.button_proceed);

        imageViewAvatar.setOnClickListener(this);
        imageViewNic.setOnClickListener(this);
        editTextLocation.setOnClickListener(this);
        buttonProceed.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.image_view_avatar) {

            getImageFromUser(REQUEST_CODE_AVATAR);

        } else if (view.getId() == R.id.image_view_nic) {

            getImageFromUser(REQUEST_CODE_NIC);

        } else if (view.getId() == R.id.edit_text_location) {

            getLocationFromUser();

        } else if (view.getId() == R.id.button_proceed) {

            initMerchant();
        }
    }

    private void getImageFromUser(int requestCode) {

        ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(requestCode);
    }

    private void getLocationFromUser() {

        Intent intent = new Intent(getActivity(), PickLocationActivity.class);
        intent.putExtra(PickLocationActivity.FORM_VIEW_INDICATOR, DESTINATION_ID);

        startActivityForResult(intent, REQUEST_CODE_LOCATION);
    }

    private void initMerchant() {

        String fullName = editTextFullName.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String nicNumber = editTextNicNumber.getText().toString().trim();
        String jazzCashNumber = editTextJazzCashNumber.getText().toString().trim();

        // check empty
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(phoneNumber)
                || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(nicNumber) || TextUtils.isEmpty(jazzCashNumber)
                || avatarUri == null || nicImageUri == null || location == null) {

            Toast.makeText(getContext(),
                    "All fields must be filled", Toast.LENGTH_SHORT).show();

            return;
        }

        if (!ValidationUtils.isNameValid(fullName)) {

            editTextFullName.setError("Full name has invalid format");
            return;
        }

        // check valid
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

        if (nicNumber.length() != 13) {

            editTextNicNumber.setError("NIC Number should be 13 characters long");

            return;
        }

        if (jazzCashNumber.length() != 7) {

            editTextJazzCashNumber.setError("Jazzcash number should be 7 characters long");

            return;
        }

        if (!ValidationUtils.isSpecialNumberValid(jazzCashNumber)) {

            editTextJazzCashNumber.setError("Jazzcash number has invalid format");
            return;
        }

        if (phoneNumber.charAt(0) == '0') {

            phoneNumber = phoneNumber.substring(1);
        }

        String finalPhoneNumber = phoneNumber;

        String avatar64 = ImageUtils.uriToBase64(getContext(), avatarUri);
        String nicImage64 = ImageUtils.uriToBase64(getContext(), nicImageUri);

        Merchant merchant = new Merchant();

        //todo: set merchant fcm token
        ((SignupActivity) getActivity()).showLoadingAnimation();

        FirebaseMessaging
                .getInstance()
                .getToken()
                .addOnSuccessListener(token -> {

                    ((SignupActivity) getActivity()).hideLoadingAnimation();

                    merchant.setFcmToken(token);
                    merchant.setFullName(fullName);
                    merchant.setPhoneNumber(finalPhoneNumber);
                    merchant.setEmail(email);
                    merchant.setPassword(password);
                    merchant.setNicNumber(nicNumber);
                    merchant.setJazzCashNumber(jazzCashNumber);
                    merchant.setLocation(location);
                    merchant.setAvatarUrl(avatar64);
                    merchant.setNicImageUrl(nicImage64);

                    verifyNewMerchantData(merchant);
                })
                .addOnFailureListener(e -> {

                    ((SignupActivity) getActivity()).hideLoadingAnimation();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                });
    }

    private void verifyNewMerchantData(Merchant merchant) {

        ((SignupActivity) getActivity()).showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("merchant-verifyNewMerchantData")
                .call(merchant.toJson())
                .addOnSuccessListener(httpsCallableResult -> {

                    ((SignupActivity) getActivity()).hideLoadingAnimation();

                    if (httpsCallableResult.getData() != null) {

                        boolean exist = (boolean) httpsCallableResult.getData();
                        Log.d("call_done", "verifyNewMerchantData: " + exist);

                        if (!exist) {

                            if (iMerchantSignupListener != null) {

                                iMerchantSignupListener.onMerchantSignup(merchant);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    ((SignupActivity) getActivity()).hideLoadingAnimation();

                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_AVATAR && resultCode == Activity.RESULT_OK) {

            if (data != null) {

                avatarUri = data.getData();
                imageViewAvatar.setImageURI(avatarUri);
            }
        } else if (requestCode == REQUEST_CODE_NIC && resultCode == Activity.RESULT_OK) {

            if (data != null) {

                nicImageUri = data.getData();
                imageViewNic.setImageURI(nicImageUri);
            }
        } else if (requestCode == REQUEST_CODE_LOCATION && resultCode == Activity.RESULT_OK) {

            if (data != null) {

                String locationName = data.getStringExtra(PickLocationActivity.LOCATION_NAME);
                location = data.getParcelableExtra(PickLocationActivity.LOCATION_LAT_LONG);

                editTextLocation.setText(locationName);
            }
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
        } else if (editTextNicNumber.getText().hashCode() == editable.hashCode()) {

            text = editTextNicNumber.getText().toString().trim();

            if (text.length() != 13) {

                editTextNicNumber.setError("NIC Number should be 13 characters long");
            }
        } else if (editTextJazzCashNumber.getText().hashCode() == editable.hashCode()) {

            text = editTextJazzCashNumber.getText().toString().trim();

            if (text.length() != 7) {

                editTextJazzCashNumber.setError("Jazzcash number should be 7 characters long");

                return;
            }

            if (!ValidationUtils.isSpecialNumberValid(text)) {

                editTextJazzCashNumber.setError("Jazzcash number has invalid format");
            }
        }
    }
}