package com.laundromat.admin.dialogs;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.admin.R;
import com.laundromat.admin.activities.ServiceTypesActivity;
import com.laundromat.admin.model.washable.ServiceType;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.interfaces.IServiceTypeCreatedListener;
import com.laundromat.admin.utils.ValidationUtils;

import java.util.Objects;

public class AddServiceTypeDialog extends androidx.fragment.app.DialogFragment
        implements View.OnClickListener, TextWatcher {

    // Views
    private EditText editTextName;
    private Button buttonSave;
    private Button buttonCancel;

    // Interfaces
    private IServiceTypeCreatedListener serviceCreatedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_add_service_type, container, false);
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

    public void setListener(IServiceTypeCreatedListener serviceCreatedListener) {
        this.serviceCreatedListener = serviceCreatedListener;
    }

    private void initViews(View view) {

        editTextName = view.findViewById(R.id.edit_text_name);
        editTextName.addTextChangedListener(this);

        buttonSave = view.findViewById(R.id.button_save);
        buttonCancel = view.findViewById(R.id.button_cancel);

        buttonSave.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_save) {

            save();

        } else if (view.getId() == R.id.button_cancel) {

            dismiss();
        }
    }

    private void save() {

        String serviceTypeName = editTextName.getText().toString().trim();

        // check empty
        if (TextUtils.isEmpty(serviceTypeName)) {

            Toast.makeText(getContext(),
                    "All Fields are required", Toast.LENGTH_SHORT).show();

            return;
        }

        if (!ValidationUtils.isNameValid(serviceTypeName)) {

            editTextName.setError("Service name has invalid format");
            return;
        }

        boolean exists = false;

        for (ServiceType serviceType : Session.user.getServiceTypes()) {

            if (serviceType.getName().equals(serviceTypeName)) {

                exists = true;
                break;
            }
        }

        // check duplicate
        if (exists) {

            editTextName.setError("Service type with provided name already exist");
            return;
        }

        ServiceType serviceType = new ServiceType(serviceTypeName);
        serviceType.setActive(true);

        getDialog().hide();
        ((ServiceTypesActivity) getActivity()).showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("admin-addServiceType")
                .call(serviceType.toJson())
                .addOnSuccessListener(httpsCallableResult -> {

                    ((ServiceTypesActivity) getActivity()).hideLoadingAnimation();

                    if (serviceCreatedListener != null) {

                        serviceCreatedListener.onServiceTypeCreated(serviceType);

                        dismiss();
                    }
                })
                .addOnFailureListener(e -> {

                    getDialog().show();
                    ((ServiceTypesActivity) getActivity()).showLoadingAnimation();

                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("add_service", "save: " + e.getMessage());
                });
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

        String text;

        if (editTextName.getText().hashCode() == editable.hashCode()) {

            text = editTextName.getText().toString().trim();

            if (!ValidationUtils.isNameValid(text)) {

                editTextName.setError("Service name has invalid format");
                return;
            }

            boolean exists = false;

            for (ServiceType serviceType : Session.user.getServiceTypes()) {

                if (serviceType.getName().equals(text)) {

                    exists = true;
                    break;
                }
            }

            // check duplicate
            if (exists) {

                editTextName.setError("Service type with provided name already exist");
            }
        }
    }
}