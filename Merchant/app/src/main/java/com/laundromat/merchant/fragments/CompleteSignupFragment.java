package com.laundromat.merchant.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.merchant.R;
import com.laundromat.merchant.activities.LoginActivity;
import com.laundromat.merchant.activities.SignupActivity;
import com.laundromat.merchant.model.Laundry;
import com.laundromat.merchant.model.Merchant;
import com.laundromat.merchant.ui.interfaces.ISignupListener;

import java.util.HashMap;
import java.util.Map;

public class CompleteSignupFragment extends Fragment
        implements ISignupListener, View.OnClickListener {

    // Views
    private LinearLayout layoutMessage;
    private Button buttonComplete;

    // Variables
    private Merchant merchant;
    private Laundry laundry;

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
        this.layoutMessage.setVisibility(View.GONE);
        this.buttonComplete = view.findViewById(R.id.button_complete);
        this.buttonComplete.setOnClickListener(this);
    }

    private void sendSignupRequest() {

        ((SignupActivity) getActivity()).showLoadingAnimation();

        Map<String, Object> data = new HashMap<>();
        data.put("merchant", this.merchant.toJson());
        data.put("laundry", this.laundry.toJson());

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("merchant-createNewMerchant")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    ((SignupActivity) getActivity()).hideLoadingAnimation();

                    layoutMessage.setVisibility(View.VISIBLE);
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
    public void onSignupComplete(Merchant merchant, Laundry laundry) {

        this.merchant = merchant;
        this.laundry = laundry;
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_complete) {

            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }
}