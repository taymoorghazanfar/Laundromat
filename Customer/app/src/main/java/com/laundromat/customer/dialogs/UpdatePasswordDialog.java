package com.laundromat.customer.dialogs;

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

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.customer.R;
import com.laundromat.customer.activities.ProfileActivity;
import com.laundromat.customer.prefs.Session;
import com.laundromat.customer.ui.interfaces.IPasswordUpdatedListener;
import com.laundromat.customer.utils.ValidationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UpdatePasswordDialog extends androidx.fragment.app.DialogFragment
        implements View.OnClickListener, TextWatcher {

    // Variables
    String password;

    // Views
    private EditText editTextOldPassword;
    private EditText editTextNewPassword;
    private EditText editTextConfirmPassword;
    private Button buttonUpdate;
    private Button buttonCancel;

    // Interfaces
    private IPasswordUpdatedListener passwordUpdatedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            password = getArguments().getString("password");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_update_password, container, false);
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

    public void setListener(IPasswordUpdatedListener passwordUpdatedListener) {
        this.passwordUpdatedListener = passwordUpdatedListener;
    }

    private void initViews(View view) {

        editTextOldPassword = view.findViewById(R.id.edit_text_old_password);
        editTextOldPassword.addTextChangedListener(this);

        editTextNewPassword = view.findViewById(R.id.edit_text_new_password);
        editTextNewPassword.addTextChangedListener(this);

        editTextConfirmPassword = view.findViewById(R.id.edit_text_confirm_password);
        editTextConfirmPassword.addTextChangedListener(this);

        buttonUpdate = view.findViewById(R.id.button_update);
        buttonCancel = view.findViewById(R.id.button_cancel);

        buttonUpdate.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_update) {

            update();

        } else if (view.getId() == R.id.button_cancel) {

            dismiss();
        }
    }

    private void update() {

        String oldPassword = editTextOldPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)
                || TextUtils.isEmpty(confirmPassword)) {

            Toast.makeText(getContext(),
                    "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (oldPassword.length() < 6) {

            editTextOldPassword.setError("Password must be at least 6 characters long");
            return;
        }

        if (!oldPassword.equals(password)) {

            editTextOldPassword.setError("Old password is incorrect");
            return;
        }

        if (newPassword.length() < 6) {

            editTextOldPassword.setError("Password must be at least 6 characters long");
            return;
        }

        if (!ValidationUtils.isPasswordValid(newPassword)) {

            editTextNewPassword.setError("Password has invalid format");
            return;
        }

        if (newPassword.equals(oldPassword)) {

            editTextNewPassword.setError("New password must be unique from old password");
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

        if (!confirmPassword.equals(newPassword)) {

            editTextConfirmPassword.setError("Passwords do not match");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("collection", "customers");
        data.put("user_id", Session.user.getId());
        data.put("password", newPassword);

        getDialog().hide();
        ((ProfileActivity) getActivity()).showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("customer-updatePassword")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    ((ProfileActivity) getActivity()).hideLoadingAnimation();

                    if (passwordUpdatedListener != null) {

                        passwordUpdatedListener.onPasswordUpdated(newPassword);
                        dismiss();
                    }
                })
                .addOnFailureListener(e -> {

                    ((ProfileActivity) getActivity()).hideLoadingAnimation();
                    getDialog().show();

                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
        if (editTextOldPassword.getText().hashCode() == editable.hashCode()) {

            text = editTextOldPassword.getText().toString().trim();

            if (text.length() < 6) {

                editTextOldPassword.setError("Password must be at least 6 characters long");
                return;
            }

            if (!text.equals(password)) {

                editTextOldPassword.setError("Old password is incorrect");
            }

        } else if (editTextNewPassword.getText().hashCode() == editable.hashCode()) {

            text = editTextNewPassword.getText().toString().trim();
            String oldPassword = editTextOldPassword.getText().toString().trim();

            if (text.length() < 6) {

                editTextNewPassword.setError("Password must be at least 6 characters long");
                return;
            }

            if (!ValidationUtils.isPasswordValid(text)) {

                editTextNewPassword.setError("Password has invalid format");
                return;
            }

            if (text.equals(oldPassword)) {

                editTextNewPassword.setError("New password must be unique from old password");
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
