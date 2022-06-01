package com.laundromat.merchant.fragments;

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
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.merchant.R;
import com.laundromat.merchant.ui.interfaces.IPasswordFragmentListener;
import com.laundromat.merchant.utils.ValidationUtils;

import java.util.HashMap;
import java.util.Map;

public class UpdatePasswordFragment extends Fragment implements View.OnClickListener, TextWatcher {

    // Views
    private RelativeLayout layoutLoading;
    private EditText editTextNewPassword;
    private EditText editTextConfirmPassword;
    private Button buttonUpdatePassword;

    // Variables
    private String phoneNumber;

    // Interface
    private IPasswordFragmentListener listener;

    public UpdatePasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            phoneNumber = getArguments().getString("phone_number");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_update_password, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {

        this.layoutLoading = view.findViewById(R.id.layout_loading);
        this.layoutLoading.setVisibility(View.GONE);

        editTextNewPassword = view.findViewById(R.id.edit_text_new_password);
        editTextNewPassword.addTextChangedListener(this);

        editTextConfirmPassword = view.findViewById(R.id.edit_text_confirm_password);
        editTextConfirmPassword.addTextChangedListener(this);

        buttonUpdatePassword = view.findViewById(R.id.button_update_password);
        buttonUpdatePassword.setOnClickListener(this);
    }

    private void updatePassword() {

        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {

            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {

            editTextConfirmPassword.setError("Password length must be at least 6 characters");

            return;
        }

        if (!ValidationUtils.isPasswordValid(newPassword)) {

            editTextNewPassword.setError("Password has invalid format");
            return;
        }

        if (confirmPassword.length() < 6) {

            editTextConfirmPassword.setError("Password length must be at least 6 characters");

            return;
        }

        if (!ValidationUtils.isPasswordValid(confirmPassword)) {

            editTextNewPassword.setError("Password has invalid format");
            return;
        }

        if (!confirmPassword.equals(newPassword)) {

            editTextConfirmPassword.setError("Password must match the new password");
            return;
        }

        // update password
        Map<String, Object> data = new HashMap<>();
        data.put("collection", "merchants");
        data.put("phone_number", phoneNumber);
        data.put("password", newPassword);

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("merchant-changePass")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    hideLoadingAnimation();

                    if (listener != null) {

                        listener.onPasswordChangedListener();
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("update_password", "updatePassword: " + e.getMessage());
                });
    }

    public void setListener(IPasswordFragmentListener listener) {
        this.listener = listener;
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

        if (view.getId() == R.id.button_update_password) {

            updatePassword();
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

        if (editTextNewPassword.getText().hashCode() == editable.hashCode()) {

            text = editTextNewPassword.getText().toString().trim();

            if (text.length() < 6) {

                editTextNewPassword.setError("Password length must be at least 6 characters");

                return;
            }

            if (!ValidationUtils.isPasswordValid(text)) {

                editTextNewPassword.setError("Password has invalid format");
            }

        } else if (editTextConfirmPassword.getText().hashCode() == editable.hashCode()) {

            text = editTextConfirmPassword.getText().toString().trim();
            String newPassword = editTextNewPassword.getText().toString().trim();

            if (text.length() < 6) {

                editTextConfirmPassword.setError("Password length must be at least 6 characters");

                return;
            }

            if (!ValidationUtils.isPasswordValid(text)) {

                editTextConfirmPassword.setError("Password has invalid format");

            } else if (!text.equals(newPassword)) {

                editTextConfirmPassword.setError("Password must match the new password");
            }
        }
    }
}