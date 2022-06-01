package com.laundromat.customer.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.customer.R;
import com.laundromat.customer.activities.ProfileActivity;
import com.laundromat.customer.prefs.Session;
import com.laundromat.customer.ui.interfaces.IAddressDeletedListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DeleteAddressDialog extends androidx.fragment.app.DialogFragment
        implements View.OnClickListener {

    // Variables
    private int locationIndex;
    private LatLng location;

    // Views
    private Button buttonDelete;
    private Button buttonCancel;

    private IAddressDeletedListener addressDeletedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            locationIndex = getArguments().getInt("address_index");
            location = getArguments().getParcelable("location");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_delete_address, container, false);
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

    public void setListener(IAddressDeletedListener addressDeletedListener) {
        this.addressDeletedListener = addressDeletedListener;
    }

    private void initViews(View view) {

        buttonDelete = view.findViewById(R.id.button_delete);
        buttonCancel = view.findViewById(R.id.button_cancel);

        buttonDelete.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_delete) {

            delete();

        } else if (view.getId() == R.id.button_cancel) {

            dismiss();
        }
    }

    private void delete() {

        Map<String, Object> data = new HashMap<>();
        data.put("customer_id", Session.user.getId());
        data.put("location_index", locationIndex);
        data.put("location", location);

        getDialog().hide();
        ((ProfileActivity) getActivity()).showLoadingAnimation();


        // delete menu category from database
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("customer-deleteAddress")
                .call(data)
                .addOnSuccessListener(httpsCallableResult2 -> {

                    ((ProfileActivity) getActivity()).hideLoadingAnimation();


                    if (addressDeletedListener != null) {

                        addressDeletedListener.onAddressDeleted(locationIndex);
                        dismiss();
                    }
                })
                .addOnFailureListener(e -> {

                    getDialog().show();
                    ((ProfileActivity) getActivity()).hideLoadingAnimation();

                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}