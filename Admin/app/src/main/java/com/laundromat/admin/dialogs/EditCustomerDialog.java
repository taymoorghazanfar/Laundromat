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
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.admin.R;
import com.laundromat.admin.activities.CustomerProfileActivity;
import com.laundromat.admin.model.Customer;
import com.laundromat.admin.ui.interfaces.ICustomerEditedListener;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.ImageUtils;
import com.laundromat.admin.utils.StringUtils;
import com.laundromat.admin.utils.ValidationUtils;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditCustomerDialog extends androidx.fragment.app.DialogFragment
        implements View.OnClickListener, TextWatcher {

    // Constants
    private static final int REQUEST_CODE_AVATAR = 111;

    // Interfaces
    ICustomerEditedListener customerEditedListener;

    // Views
    private ImageView imageViewAvatar;
    private EditText editTextFullName;
    private EditText editTextEmail;
    private Button buttonUpdate;
    private Button buttonCancel;

    //Variables
    private Uri avatarUri;
    private Customer customer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            String customerGson = getArguments().getString("customer");
            customer = GsonUtils.gsonToCustomer(customerGson);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_edit_customer, container, false);
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

    public void setListener(ICustomerEditedListener customerEditedListener) {
        this.customerEditedListener = customerEditedListener;
    }

    private void initViews(View view) {

        imageViewAvatar = view.findViewById(R.id.image_view_avatar);

        editTextFullName = view.findViewById(R.id.edit_text_full_name);
        editTextFullName.addTextChangedListener(this);

        editTextEmail = view.findViewById(R.id.edit_text_email);
        editTextEmail.addTextChangedListener(this);

        buttonUpdate = view.findViewById(R.id.button_update);
        buttonUpdate.setOnClickListener(this);

        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(this);

        imageViewAvatar.setOnClickListener(this);
    }

    private void setupViews() {

        Picasso.get()
                .load(customer.getAvatarUrl())
                .into(imageViewAvatar);

        editTextFullName.setText(customer.getFullName());

        editTextEmail.setText(customer.getEmail());
    }

    private void getImageFromUser(int requestCode) {

        ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(requestCode);
    }

    private void update() {

        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();

        // check empty
        if (TextUtils.isEmpty(fullName)
                || TextUtils.isEmpty(email)) {

            Toast.makeText(getContext(),
                    "All fields must be filled", Toast.LENGTH_SHORT).show();

            return;
        }

        if (!ValidationUtils.isNameValid(fullName)) {

            editTextFullName.setError("Full name has invalid format");
            return;
        }

        if (!ValidationUtils.isEmailValid(email)) {

            editTextEmail.setError("Email is invalid");

            return;
        }

        // check duplicate
        if (fullName.equals(customer.getFullName())
                && email.equals(customer.getEmail())
                && avatarUri == null) {

            Toast.makeText(getContext(),
                    "Nothing to update", Toast.LENGTH_SHORT).show();

            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("id", customer.getId());
        data.put("email", email);

        // check already exists
        getDialog().hide();
        ((CustomerProfileActivity) getActivity()).showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("customer-checkEditValid")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        ((CustomerProfileActivity) getActivity()).hideLoadingAnimation();

                        boolean valid = (boolean) httpsCallableResult.getData();

                        if (valid) {

                            boolean avatarUpdated = false;
                            String avatar64 = null;

                            if (avatarUri != null) {

                                avatarUpdated = true;
                                avatar64 = ImageUtils.uriToBase64(getContext(), avatarUri);
                            }

                            Map<String, Object> data2 = new HashMap<>();
                            data2.put("customer_id", customer.getId());
                            data2.put("full_name", fullName);
                            data2.put("email", email);
                            data2.put("avatar_updated", avatarUpdated);
                            data2.put("avatar_64", avatar64);

                            getDialog().hide();
                            ((CustomerProfileActivity) getActivity()).showLoadingAnimation();

                            FirebaseFunctions
                                    .getInstance()
                                    .getHttpsCallable("customer-updateCustomer")
                                    .call(data2)
                                    .addOnSuccessListener(httpsCallableResult2 -> {

                                        if (httpsCallableResult2.getData() != null) {

                                            ((CustomerProfileActivity) getActivity()).hideLoadingAnimation();

                                            // get the response [download url OR acknowledgment]
                                            String response = (String) httpsCallableResult2.getData();

                                            String avatarUrl = null;

                                            // if there is download url to get
                                            if (!response.equals("updated")) {

                                                avatarUrl = response;
                                            }

                                            if (customerEditedListener != null) {

                                                customerEditedListener
                                                        .onCustomerEdited(avatarUrl, fullName, email);
                                                dismiss();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {

                                        ((CustomerProfileActivity) getActivity()).hideLoadingAnimation();

                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    ((CustomerProfileActivity) getActivity()).hideLoadingAnimation();
                    getDialog().show();

                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("validate", "update: " + e.getMessage());
                });
    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.image_view_avatar) {

            getImageFromUser(REQUEST_CODE_AVATAR);

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
        }
    }
}