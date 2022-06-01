package com.laundromat.delivery.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.delivery.R;
import com.laundromat.delivery.activities.LoginActivity;
import com.laundromat.delivery.activities.SignupActivity;
import com.laundromat.delivery.model.DeliveryBoy;
import com.laundromat.delivery.model.Laundry;
import com.laundromat.delivery.model.Merchant;
import com.laundromat.delivery.model.Vehicle;
import com.laundromat.delivery.ui.interfaces.ISignupListener;

import java.util.HashMap;
import java.util.Map;

public class CompleteSignupFragment extends Fragment
        implements ISignupListener, View.OnClickListener {

    // Views
    private LinearLayout layoutMessage;
    private Button buttonComplete;

    // Variables
    private DeliveryBoy deliveryBoy;
    private Vehicle vehicle;

    public CompleteSignupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_complete_signup, container, false);

        initViews(view);

        return view;
    }

    private void initViews(View view) {

        this.layoutMessage = view.findViewById(R.id.layout_message);
        layoutMessage.setVisibility(View.GONE);
        this.buttonComplete = view.findViewById(R.id.button_complete);
        this.buttonComplete.setOnClickListener(this);
    }

    private void sendSignupRequest() {

        ((SignupActivity) getActivity()).showLoadingAnimation();

        Map<String, Object> data = new HashMap<>();
        data.put("delivery_boy", this.deliveryBoy.toJson());
        data.put("vehicle", this.vehicle.toJson());

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("delivery_boy-createNewDeliveryBoy")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    ((SignupActivity) getActivity()).hideLoadingAnimation();

                    if (httpsCallableResult.getData() != null) {

                        layoutMessage.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {

                    ((SignupActivity) getActivity()).hideLoadingAnimation();

                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        ((SignupActivity) activity).setICompleteSignup(this);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        if (menuVisible) {
            sendSignupRequest();
        }
    }

    @Override
    public void onSignupComplete(DeliveryBoy deliveryBoy, Vehicle vehicle) {

        this.deliveryBoy = deliveryBoy;
        this.vehicle = vehicle;
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_complete) {

            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }
}