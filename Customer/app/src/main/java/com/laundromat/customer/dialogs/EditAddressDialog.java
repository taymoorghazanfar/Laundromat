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
import com.laundromat.customer.ui.interfaces.IAddressEditedListener;
import com.laundromat.customer.utils.ValidationUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.laundromat.customer.utils.LocationUtils.getAddressFromLatLng;

public class EditAddressDialog extends androidx.fragment.app.DialogFragment
        implements View.OnClickListener, TextWatcher {

    // Constants
    private static final int DESTINATION_ID = 1;
    private static final int REQUEST_CODE_LOCATION = 111;

    // Views
    private EditText editTextName;
    private EditText editTextLocation;
    private Button buttonUpdate;
    private Button buttonCancel;

    // Variables
    private Location location;
    private int locationIndex;
    private LatLng locationLatLng;

    // Interfaces
    private IAddressEditedListener addressEditedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            location = getArguments().getParcelable("address");
            locationIndex = getArguments().getInt("address_index");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_edit_address, container, false);
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

    public void setListener(IAddressEditedListener addressEditedListener) {
        this.addressEditedListener = addressEditedListener;
    }

    private void initViews(View view) {

        editTextName = view.findViewById(R.id.edit_text_name);
        editTextName.addTextChangedListener(this);
        editTextName.setText(location.getName());

        editTextLocation = view.findViewById(R.id.edit_text_location);
        String locationAddress = getAddressFromLatLng(getContext(),
                location.getLatLng().latitude, location.getLatLng().longitude);

        if (locationAddress != null) {

            editTextLocation.setText(locationAddress);

        } else {

            editTextLocation.setText(MessageFormat.format("{0}, {1}",
                    location.getLatLng().latitude, location.getLatLng().longitude));
        }
        buttonUpdate = view.findViewById(R.id.button_update);
        buttonCancel = view.findViewById(R.id.button_cancel);

        editTextLocation.setOnClickListener(this);
        buttonUpdate.setOnClickListener(this);
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
        if (view.getId() == R.id.button_update) {

            update();

        } else if (view.getId() == R.id.button_cancel) {

            dismiss();
        }
    }

    private void update() {

        boolean isEmpty = checkEmpty();
        boolean isDuplicate = checkDuplicate();

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

        //check duplicate
        if (isDuplicate) {

            Toast.makeText(getContext(), "Nothing to update", Toast.LENGTH_SHORT).show();
            return;
        }

        //update address
        updateAddress();
    }

    private boolean checkEmpty() {

        String locationName = editTextName.getText().toString().trim();

        return TextUtils.isEmpty(locationName);
    }

    private boolean checkDuplicate() {

        String locationName = editTextName.getText().toString().trim();

        return locationName.equals(location.getName()) && locationLatLng == null;
    }

    private void updateAddress() {

        String locationName = editTextName.getText().toString().trim();
        Location updatedLocation = new Location();
        updatedLocation.setName(locationName);

        boolean currentLocationUpdated = false;

        if (locationLatLng != null) {

            updatedLocation.setLatLng(locationLatLng);

            // check if updated location is currently selected by the customer
            if (location.getLatLng().latitude == Session.user.getLocation().latitude
                    && location.getLatLng().longitude == Session.user.getLocation().longitude) {

                currentLocationUpdated = true;

                // change the lat lng of currently selected location as well
                Session.user.setLocation(updatedLocation.getLatLng());
            }

        } else {

            updatedLocation.setLatLng(location.getLatLng());
        }

        // save the location to fire store
        Map<String, Object> data = new HashMap<>();
        data.put("customer_id", Session.user.getId());
        data.put("location", updatedLocation.toJson());
        data.put("location_index", locationIndex);
        data.put("update_current_location", currentLocationUpdated);

        getDialog().hide();
        ((ProfileActivity) getActivity()).showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("customer-updateAddress")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    ((ProfileActivity) getActivity()).hideLoadingAnimation();


                    if (addressEditedListener != null) {

                        addressEditedListener.onAddressEdited(updatedLocation, locationIndex);
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
