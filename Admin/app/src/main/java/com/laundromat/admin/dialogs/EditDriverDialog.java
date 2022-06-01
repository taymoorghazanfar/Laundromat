package com.laundromat.admin.dialogs;

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

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.admin.R;
import com.laundromat.admin.activities.DriverProfileActivity;
import com.laundromat.admin.model.DeliveryBoy;
import com.laundromat.admin.ui.interfaces.IDriverEditedListener;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.ImageUtils;
import com.laundromat.admin.utils.StringUtils;
import com.laundromat.admin.utils.ValidationUtils;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditDriverDialog extends androidx.fragment.app.DialogFragment
        implements View.OnClickListener, TextWatcher {

    // Constants
    private static final int REQUEST_CODE_AVATAR = 111;
    // Interfaces
    IDriverEditedListener driverEditedListener;
    //Variables
    private Uri avatarUri;
    private DeliveryBoy driver;
    // Views
    private ImageView imageViewAvatar;
    private EditText editTextFullName;
    private EditText editTextEmail;
    private EditText editTextJazzCashNumber;
    private EditText editTextLicense;
    private Button buttonUpdate;
    private Button buttonCancel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            String driverGson = getArguments().getString("driver");
            driver = GsonUtils.gsonToDriver(driverGson);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_edit_driver, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        setupViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getDialog()).getWindow()
                .setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setListener(IDriverEditedListener driverEditedListener) {
        this.driverEditedListener = driverEditedListener;
    }

    private void initViews(View view) {

        imageViewAvatar = view.findViewById(R.id.image_view_avatar);

        editTextFullName = view.findViewById(R.id.edit_text_full_name);
        editTextFullName.addTextChangedListener(this);

        editTextEmail = view.findViewById(R.id.edit_text_email);
        editTextEmail.addTextChangedListener(this);

        editTextJazzCashNumber = view.findViewById(R.id.edit_text_jazz_cash_number);
        editTextJazzCashNumber.addTextChangedListener(this);

        editTextLicense = view.findViewById(R.id.edit_text_license);
        editTextLicense.addTextChangedListener(this);

        buttonUpdate = view.findViewById(R.id.button_update);
        buttonUpdate.setOnClickListener(this);

        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(this);

        imageViewAvatar.setOnClickListener(this);
        editTextLicense.setOnClickListener(this);
    }

    private void setupViews() {

        Picasso.get()
                .load(driver.getAvatarUrl())
                .into(imageViewAvatar);

        editTextFullName.setText(driver.getFullName());

        editTextEmail.setText(driver.getEmail());

        editTextJazzCashNumber.setText(driver.getJazzCashNumber());

        editTextLicense.setText(driver.getLicenseNumber());
    }

    private void getImageFromUser(int requestCode) {

        ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(requestCode);
    }

    private void update() {

        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String jazzCashNumber = editTextJazzCashNumber.getText().toString().trim();
        String licenseNumber = editTextLicense.getText().toString().trim();

        // check empty
        if (TextUtils.isEmpty(fullName)
                || TextUtils.isEmpty(email) || TextUtils.isEmpty(jazzCashNumber)
                || TextUtils.isEmpty(licenseNumber)) {

            Toast.makeText(getContext(),
                    "All fields must be filled", Toast.LENGTH_SHORT).show();

            return;
        }

        if (!ValidationUtils.isNameValid(fullName)) {

            editTextFullName.setError("Full name has invalid format");
            return;
        }

        if (!ValidationUtils.isEmailValid(email)) {

            editTextEmail.setError("Email is invalid");

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

        if (licenseNumber.length() != 8) {

            editTextLicense.setError("License number should be 8 characters long");

            return;
        }

        if (!ValidationUtils.isSpecialNumberValid(licenseNumber)) {

            editTextLicense.setError("License number has invalid format");

            return;
        }

        // check duplicate
        if (fullName.equals(driver.getFullName())
                && email.equals(driver.getEmail())
                && jazzCashNumber.equals(driver.getJazzCashNumber())
                && licenseNumber.equals(driver.getLicenseNumber())
                && avatarUri == null) {

            Toast.makeText(getContext(),
                    "Nothing to update", Toast.LENGTH_SHORT).show();

            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("id", driver.getId());
        data.put("email", email);
        data.put("license", licenseNumber);

        // check already exists
        getDialog().hide();
        ((DriverProfileActivity) getActivity()).showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("delivery_boy-checkEditValid")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        ((DriverProfileActivity) getActivity()).hideLoadingAnimation();

                        boolean valid = (boolean) httpsCallableResult.getData();

                        if (valid) {

                            boolean avatarUpdated = false;
                            String avatar64 = null;

                            if (avatarUri != null) {

                                avatarUpdated = true;
                                avatar64 = ImageUtils.uriToBase64(getContext(), avatarUri);
                            }

                            Map<String, Object> data2 = new HashMap<>();
                            data2.put("driver_id", driver.getId());
                            data2.put("full_name", fullName);
                            data2.put("email", email);
                            data2.put("jazz_cash", jazzCashNumber);
                            data2.put("license", licenseNumber);
                            data2.put("avatar_updated", avatarUpdated);
                            data2.put("avatar_64", avatar64);

                            getDialog().hide();
                            ((DriverProfileActivity) getActivity()).showLoadingAnimation();

                            FirebaseFunctions
                                    .getInstance()
                                    .getHttpsCallable("delivery_boy-updateDeliveryBoy")
                                    .call(data2)
                                    .addOnSuccessListener(httpsCallableResult2 -> {

                                        if (httpsCallableResult2.getData() != null) {

                                            ((DriverProfileActivity) getActivity()).hideLoadingAnimation();

                                            // get the response [download url OR acknowledgment]
                                            String response = (String) httpsCallableResult2.getData();

                                            String avatarUrl = null;

                                            // if there is download url to get
                                            if (!response.equals("updated")) {

                                                avatarUrl = response;
                                            }

                                            if (driverEditedListener != null) {

                                                driverEditedListener
                                                        .onDriverEdited(avatarUrl, fullName, email,
                                                                jazzCashNumber, licenseNumber);
                                                dismiss();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {

                                        ((DriverProfileActivity) getActivity()).hideLoadingAnimation();

                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.d("update", "update: " + e.getMessage());
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    ((DriverProfileActivity) getActivity()).hideLoadingAnimation();
                    getDialog().show();

                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("validate", "update: " + e.getMessage());
                });
    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.image_view_avatar) {

            getImageFromUser(REQUEST_CODE_AVATAR);

        } else if (view.getId() == R.id.button_update) {

            update();

        } else if (view.getId() == R.id.button_cancel) {

            dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_AVATAR && resultCode == Activity.RESULT_OK) {

            if (data != null) {

                avatarUri = data.getData();
                imageViewAvatar.setImageURI(avatarUri);
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

        } else if (editTextEmail.getText().hashCode() == editable.hashCode()) {

            text = editTextEmail.getText().toString().trim();

            if (!ValidationUtils.isEmailValid(text)) {

                editTextEmail.setError("Email has invalid format");
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