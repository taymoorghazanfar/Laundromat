package com.laundromat.customer.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.customer.R;
import com.laundromat.customer.activities.PickLocationActivity;
import com.laundromat.customer.activities.ProfileActivity;
import com.laundromat.customer.model.util.Location;
import com.laundromat.customer.prefs.Session;
import com.laundromat.customer.ui.interfaces.IAddressCreatedListener;
import com.laundromat.customer.utils.ValidationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddAddressDialog extends androidx.fragment.app.DialogFragment
        implements View.OnClickListener, TextWatcher {

    // Constants
    private static final int DESTINATION_ID = 1;
    private static final int REQUEST_CODE_LOCATION = 111;

    // Views
    private EditText editTextName;
    private EditText editTextLocation;
    private Button buttonSave;
    private Button buttonCancel;

    // Variables
    private LatLng locationLatLng;

    // Interfaces
    private IAddressCreatedListener addressCreatedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_add_address, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getDialog()).getWindow()
                .setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setListener(IAddressCreatedListener addressCreatedListener) {
        this.addressCreatedListener = addressCreatedListener;
    }

    private void initViews(View view) {

        editTextName = view.findViewById(R.id.edit_text_name);
        editTextName.addTextChangedListener(this);

        editTextLocation = view.findViewById(R.id.edit_text_location);
        buttonSave = view.findViewById(R.id.button_save);
        buttonCancel = view.findViewById(R.id.button_cancel);

        editTextLocation.setOnClickListener(this);
        buttonSave.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
    }

    private void getLocationFromUser() {

        Intent intent = new Intent(getActivity(), PickLocationActivity.class);
        intent.putExtra(PickLocationActivity.FORM_VIEW_INDICATOR, DESTINATION_ID);

        startActivityForResult(intent, REQUEST_CODE_LOCATION);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.edit_text_location) {

            getLocationFromUser();
        }
        if (view.getId() == R.id.button_save) {

            save();

        } else if (view.getId() == R.id.button_cancel) {

            dismiss();
        }
    }

    private void save() {

        boolean isEmpty = checkEmpty();

        //check empty
        if (isEmpty) {

            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // check valid
        String locationName = editTextName.getText().toString().trim();

        if (!ValidationUtils.isNameValid(locationName)) {

            editTextName.setError("Address name has invalid format");

            return;
        }

        //create address
        createAddress();
    }

    private boolean checkEmpty() {

        String locationName = editTextName.getText().toString().trim();

        return TextUtils.isEmpty(locationName) || locationLatLng == null;
    }

    private void createAddress() {

        String locationName = editTextName.getText().toString().trim();
        Location location = new Location(locationName, locationLatLng);

        // save the menu category to fire store
        Map<String, Object> data = new HashMap<>();
        data.put("customer_id", Session.user.getId());
        data.put("location", location.toJson());

        getDialog().hide();
        ((ProfileActivity) getActivity()).showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("customer-addAddress")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    ((ProfileActivity) getActivity()).hideLoadingAnimation();

                    if (addressCreatedListener != null) {

                        addressCreatedListener.onAddressCreated(location);
                        dismiss();
                    }
                })
                .addOnFailureListener(e -> {

                    getDialog().show();
                    ((ProfileActivity) getActivity()).hideLoadingAnimation();

                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_LOCATION && resultCode == Activity.RESULT_OK) {

            if (data != null) {

                String locationAddress = data.getStringExtra(PickLocationActivity.LOCATION_NAME);
                locationLatLng = data.getParcelableExtra(PickLocationActivity.LOCATION_LAT_LONG);

                editTextLocation.setText(locationAddress);
            }
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

        if (editTextName.getText().hashCode() == editable.hashCode()) {

            text = editTextName.getText().toString().trim();

            if (!ValidationUtils.isNameValid(text)) {

                editTextName.setError("Address name has invalid format");
            }
        }
    }
}
