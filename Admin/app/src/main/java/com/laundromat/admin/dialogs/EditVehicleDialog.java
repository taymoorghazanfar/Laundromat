package com.laundromat.admin.dialogs;

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
import com.laundromat.admin.R;
import com.laundromat.admin.activities.DriverProfileActivity;
import com.laundromat.admin.model.Vehicle;
import com.laundromat.admin.ui.interfaces.IVehicleEditedListener;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.StringUtils;
import com.laundromat.admin.utils.ValidationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditVehicleDialog extends androidx.fragment.app.DialogFragment
        implements View.OnClickListener, TextWatcher {

    // Interfaces
    IVehicleEditedListener vehicleEditedListener;

    // Views
    private EditText editTextPlateNumber;
    private EditText editTextVehicleName;
    private EditText editTextVehicleModel;
    private EditText editTextVehicleColor;
    private Button buttonCancel;
    private Button buttonUpdate;

    //Variables
    private Vehicle vehicle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            String vehicleGson = getArguments().getString("vehicle");
            vehicle = GsonUtils.gsonToVehicle(vehicleGson);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_edit_vehicle, container, false);
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

    public void setListener(IVehicleEditedListener vehicleEditedListener) {
        this.vehicleEditedListener = vehicleEditedListener;
    }

    private void initViews(View view) {

        this.editTextPlateNumber = view.findViewById(R.id.edit_text_plate_number);
        editTextPlateNumber.addTextChangedListener(this);

        this.editTextVehicleName = view.findViewById(R.id.edit_text_vehicle_name);
        editTextVehicleName.addTextChangedListener(this);

        this.editTextVehicleModel = view.findViewById(R.id.edit_text_vehicle_model);
        editTextVehicleModel.addTextChangedListener(this);

        this.editTextVehicleColor = view.findViewById(R.id.edit_text_vehicle_color);
        editTextVehicleColor.addTextChangedListener(this);

        buttonUpdate = view.findViewById(R.id.button_update);
        buttonUpdate.setOnClickListener(this);

        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(this);
    }

    private void setupViews() {

        editTextVehicleName.setText(vehicle.getName());
        editTextVehicleModel.setText(String.valueOf(vehicle.getModel()));
        editTextPlateNumber.setText(vehicle.getPlateNumber());
        editTextVehicleColor.setText(vehicle.getColor());
    }

    private void update() {

        String plateNumber = editTextPlateNumber.getText().toString().trim();
        String vehicleName = editTextVehicleName.getText().toString().trim();
        String vehicleModel = editTextVehicleModel.getText().toString().trim();
        String vehicleColor = editTextVehicleColor.getText().toString().trim();

        // check empty
        if (TextUtils.isEmpty(plateNumber) || TextUtils.isEmpty(vehicleName)
                || TextUtils.isEmpty(vehicleModel) || TextUtils.isEmpty(vehicleColor)) {

            Toast.makeText(getContext(),
                    "All fields must be filled", Toast.LENGTH_SHORT).show();

            return;
        }

        if (plateNumber.length() < 7 || plateNumber.length() > 8) {

            editTextPlateNumber.setError("Plate number should be alteast 7 and utmost 8 characters long");

            return;
        }

        if (!ValidationUtils.isPlateNumberValid(plateNumber)) {

            editTextPlateNumber.setError("Plate number has invalid format");

            return;
        }

        if (!ValidationUtils.isInputValid(vehicleName)) {

            editTextVehicleName.setError("Vehicle name has invalid format");
            return;
        }

        if (vehicleModel.length() != 4) {

            editTextVehicleModel.setError("Vehicle model is invalid");

            return;
        }

        int model = Integer.parseInt(vehicleModel);

        if (model < 2010) {

            editTextVehicleModel.setError("Vehicle model cannot be older than 2010");
            return;
        }

        if (!ValidationUtils.areAllCharacters(vehicleColor)) {

            editTextVehicleColor.setError("Vehicle color has invalid format");
            return;
        }

        // check if nothing is updated
        if (plateNumber.equals(vehicle.getPlateNumber())
                && vehicleName.equals(vehicle.getName())
                && Integer.parseInt(vehicleModel) == vehicle.getModel()
                && vehicleColor.equals(vehicle.getColor())) {

            Toast.makeText(getContext(),
                    "Nothing to update", Toast.LENGTH_SHORT).show();

            return;
        }

        // verify plate number
        Map<String, Object> data = new HashMap<>();
        data.put("plate_number", plateNumber);

        getDialog().hide();
        ((DriverProfileActivity) getActivity()).showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("vehicle-verifyPlateNumber")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    Map<String, Object> data2 = new HashMap<>();
                    data2.put("vehicle_id", vehicle.getId());
                    data2.put("plate_number", plateNumber);
                    data2.put("name", vehicleName);
                    data2.put("model", model);
                    data2.put("color", vehicleColor);

                    FirebaseFunctions
                            .getInstance()
                            .getHttpsCallable("vehicle-updateDetails")
                            .call(data2)
                            .addOnSuccessListener(httpsCallableResult2 -> {

                                ((DriverProfileActivity) getActivity()).hideLoadingAnimation();

                                if (vehicleEditedListener != null) {

                                    vehicleEditedListener
                                            .onVehicleEdited(vehicleName,
                                                    plateNumber, vehicleColor, model);

                                    dismiss();
                                }
                            })
                            .addOnFailureListener(e -> {

                                ((DriverProfileActivity) getActivity()).hideLoadingAnimation();
                                getDialog().show();

                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {

                    ((DriverProfileActivity) getActivity()).hideLoadingAnimation();
                    getDialog().show();

                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_update) {

            update();

        } else if (view.getId() == R.id.button_cancel) {

            dismiss();
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

        if (editTextPlateNumber.getText().hashCode() == editable.hashCode()) {

            text = editTextPlateNumber.getText().toString().trim();


            if (text.length() < 7 || text.length() > 8) {

                editTextPlateNumber.setError("Plate number should be alteast 7 and utmost 8 characters long");

                return;
            }

            if (!ValidationUtils.isPlateNumberValid(text)) {

                editTextPlateNumber.setError("Plate number has invalid format");
            }

        } else if (editTextVehicleName.getText().hashCode() == editable.hashCode()) {

            text = editTextVehicleName.getText().toString().trim();


            if (!ValidationUtils.isInputValid(text)) {

                editTextVehicleName.setError("Vehicle name has invalid format");
            }

        } else if (editTextVehicleModel.getText().hashCode() == editable.hashCode()) {

            text = editTextVehicleModel.getText().toString().trim();


            if (text.length() != 4) {

                editTextVehicleModel.setError("Vehicle model is invalid");

                return;
            }

            int model = Integer.parseInt(text);

            if (model < 2010) {

                editTextVehicleModel.setError("Vehicle model cannot be older than 2010");
            }

        } else if (editTextVehicleColor.getText().hashCode() == editable.hashCode()) {

            text = editTextVehicleColor.getText().toString().trim();

            if (!ValidationUtils.areAllCharacters(text)) {

                editTextVehicleColor.setError("Vehicle color has invalid format");
            }
        }
    }
}