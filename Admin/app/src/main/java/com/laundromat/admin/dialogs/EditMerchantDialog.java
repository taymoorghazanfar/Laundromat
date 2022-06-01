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
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.admin.R;
import com.laundromat.admin.activities.MerchantProfileActivity;
import com.laundromat.admin.activities.PickLocationActivity;
import com.laundromat.admin.model.Merchant;
import com.laundromat.admin.ui.interfaces.IMerchantEditedListener;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.ImageUtils;
import com.laundromat.admin.utils.LocationUtils;
import com.laundromat.admin.utils.StringUtils;
import com.laundromat.admin.utils.ValidationUtils;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditMerchantDialog extends androidx.fragment.app.DialogFragment
        implements View.OnClickListener, TextWatcher {

    // Constants
    private static final int REQUEST_CODE_AVATAR = 111;
    private static final int REQUEST_CODE_LOCATION = 333;
    private static final int DESTINATION_ID = 1;
    // Interfaces
    IMerchantEditedListener merchantEditedListener;
    //Variables
    private Uri avatarUri;
    private LatLng location;
    private Merchant merchant;
    // Views
    private ImageView imageViewAvatar;
    private EditText editTextFullName;
    private EditText editTextEmail;
    private EditText editTextJazzCashNumber;
    private EditText editTextLocation;
    private Button buttonUpdate;
    private Button buttonCancel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            String merchantGson = getArguments().getString("merchant");
            merchant = GsonUtils.gsonToMerchant(merchantGson);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_edit_merchant, container, false);
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

    public void setListener(IMerchantEditedListener merchantEditedListener) {
        this.merchantEditedListener = merchantEditedListener;
    }

    private void initViews(View view) {

        imageViewAvatar = view.findViewById(R.id.image_view_avatar);

        editTextFullName = view.findViewById(R.id.edit_text_full_name);
        editTextFullName.addTextChangedListener(this);

        editTextEmail = view.findViewById(R.id.edit_text_email);
        editTextEmail.addTextChangedListener(this);

        editTextJazzCashNumber = view.findViewById(R.id.edit_text_jazz_cash_number);
        editTextJazzCashNumber.addTextChangedListener(this);

        editTextLocation = view.findViewById(R.id.edit_text_location);

        buttonUpdate = view.findViewById(R.id.button_update);
        buttonUpdate.setOnClickListener(this);

        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(this);

        imageViewAvatar.setOnClickListener(this);
        editTextLocation.setOnClickListener(this);
    }

    private void setupViews() {

        Picasso.get()
                .load(merchant.getAvatarUrl())
                .into(imageViewAvatar);

        editTextFullName.setText(merchant.getFullName());

        editTextEmail.setText(merchant.getEmail());

        editTextJazzCashNumber.setText(merchant.getJazzCashNumber());

        editTextLocation.setText(LocationUtils.getAddressFromLatLng(getContext(),
                merchant.getLocation().latitude, merchant.getLocation().longitude));
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

    private void update() {

        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String jazzCashNumber = editTextJazzCashNumber.getText().toString().trim();

        // check empty
        if (TextUtils.isEmpty(fullName)
                || TextUtils.isEmpty(email) || TextUtils.isEmpty(jazzCashNumber)) {

            Toast.makeText(getContext(),
                    "All fields must be filled", Toast.LENGTH_SHORT).show();

            return;
        }

        if (!ValidationUtils.isNameValid(fullName)) {

            editTextFullName.setError("Full name has invalid format");
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

        if (!ValidationUtils.isEmailValid(email)) {

            editTextEmail.setError("Email is invalid");

            return;
        }

        // check duplicate
        if (fullName.equals(merchant.getFullName())
                && email.equals(merchant.getEmail())
                && jazzCashNumber.equals(merchant.getJazzCashNumber())
                && avatarUri == null && location == null) {

            Toast.makeText(getContext(),
                    "Nothing to update", Toast.LENGTH_SHORT).show();

            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("id", merchant.getId());
        data.put("email", email);

        // check already exists
        getDialog().hide();
        ((MerchantProfileActivity) getActivity()).showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("merchant-checkEditValid")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        ((MerchantProfileActivity) getActivity()).hideLoadingAnimation();

                        boolean valid = (boolean) httpsCallableResult.getData();

                        if (valid) {

                            boolean avatarUpdated = false;
                            boolean locationUpdated = false;
                            String avatar64 = null;

                            if (avatarUri != null) {

                                avatarUpdated = true;
                                avatar64 = ImageUtils.uriToBase64(getContext(), avatarUri);
                            }

                            if (location != null) {

                                locationUpdated = true;
                            }

                            Map<String, Object> data2 = new HashMap<>();
                            data2.put("merchant_id", merchant.getId());
                            data2.put("full_name", fullName);
                            data2.put("email", email);
                            data2.put("jazz_cash", jazzCashNumber);
                            data2.put("avatar_updated", avatarUpdated);
                            data2.put("avatar_64", avatar64);
                            data2.put("location_updated", locationUpdated);
                            data2.put("location", location);

                            getDialog().hide();
                            ((MerchantProfileActivity) getActivity()).showLoadingAnimation();

                            FirebaseFunctions
                                    .getInstance()
                                    .getHttpsCallable("merchant-updateMerchant")
                                    .call(data2)
                                    .addOnSuccessListener(httpsCallableResult2 -> {

                                        if (httpsCallableResult2.getData() != null) {

                                            ((MerchantProfileActivity) getActivity()).hideLoadingAnimation();

                                            // get the response [download url OR acknowledgment]
                                            String response = (String) httpsCallableResult2.getData();

                                            String avatarUrl = null;

                                            // if there is download url to get
                                            if (!response.equals("updated")) {

                                                avatarUrl = response;
                                            }

                                            if (merchantEditedListener != null) {

                                                merchantEditedListener
                                                        .onMerchantEdited(avatarUrl, fullName,
                                                                email,
                                                                jazzCashNumber, location);
                                                dismiss();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {

                                        ((MerchantProfileActivity) getActivity()).hideLoadingAnimation();

                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    ((MerchantProfileActivity) getActivity()).hideLoadingAnimation();
                    getDialog().show();

                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("validate", "update: " + e.getMessage());
                });
    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.image_view_avatar) {

            getImageFromUser(REQUEST_CODE_AVATAR);

        } else if (view.getId() == R.id.edit_text_location) {

            getLocationFromUser();

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