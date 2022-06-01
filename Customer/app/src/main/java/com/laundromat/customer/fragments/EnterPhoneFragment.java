package com.laundromat.customer.fragments;

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
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.customer.R;
import com.laundromat.customer.ui.interfaces.IPhoneFragmentListener;
import com.laundromat.customer.utils.ValidationUtils;

import java.util.HashMap;
import java.util.Map;

public class EnterPhoneFragment extends Fragment implements View.OnClickListener, TextWatcher {

    // Views
    private RelativeLayout layoutLoading;
    private EditText editTextPhoneNumber;
    private Button buttonProceed;

    // Variables
    private String phoneNumber;

    // Interface
    private IPhoneFragmentListener listener;

    public EnterPhoneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_enter_phone, container, false);
        initViews(view);
        return view;
    }

    public void setListener(IPhoneFragmentListener listener) {
        this.listener = listener;
    }

    private void initViews(View view) {

        this.layoutLoading = view.findViewById(R.id.layout_loading);
        this.layoutLoading.setVisibility(View.GONE);

        this.editTextPhoneNumber = view.findViewById(R.id.edit_text_phone_number);
        editTextPhoneNumber.addTextChangedListener(this);

        this.buttonProceed = view.findViewById(R.id.button_proceed);
        this.buttonProceed.setOnClickListener(this);
    }

    private void showLoadingAnimation() {

        layoutLoading.setVisibility(View.VISIBLE);
        layoutLoading.bringToFront();
        layoutLoading.animate().translationY(0);
    }

    private void hideLoadingAnimation() {

        layoutLoading.setVisibility(View.GONE);
        layoutLoading.animate().translationY(layoutLoading.getHeight());
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_proceed) {

            checkPhoneNumber();
        }
    }

    private void checkPhoneNumber() {

        phoneNumber = editTextPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(phoneNumber)) {

            editTextPhoneNumber.setError("Enter your phone number");
            return;
        }

        if (phoneNumber.length() < 10 || phoneNumber.length() > 11) {

            editTextPhoneNumber.setError("Phone number should have atleast 10 and utmost 11 characters");

            return;
        }

        if (!ValidationUtils.isPhoneValid(phoneNumber)) {

            editTextPhoneNumber.setError("Phone number has invalid format");
        }

        if (phoneNumber.charAt(0) == '0') {

            phoneNumber = phoneNumber.substring(1);
        }

        // check if phone number exist
        Map<String, Object> data = new HashMap<>();
        data.put("collection", "customers");
        data.put("phone_number", phoneNumber);

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("customer-checkPhoneNumber")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    hideLoadingAnimation();
                    verifyPhoneNumber(phoneNumber);
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();
                    editTextPhoneNumber.setError(e.getMessage());
                    Log.d("signup", "initCustomer: login " + e.getMessage());
                });
    }

    private void verifyPhoneNumber(String phoneNumber) {

        // goto otp fragment
        if (listener != null) {

            listener.onPhoneNumberEntered(phoneNumber);
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

        if (editTextPhoneNumber.getText().hashCode() == editable.hashCode()) {

            text = editTextPhoneNumber.getText().toString().trim();

            if (text.length() < 10 || text.length() > 11) {

                editTextPhoneNumber.setError("Phone number should have atleast 10 and utmost 11 characters");

                return;
            }

            if (!ValidationUtils.isPhoneValid(text)) {

                editTextPhoneNumber.setError("Phone number has invalid format");
            }
        }
    }
}