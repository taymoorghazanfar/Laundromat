package com.laundromat.delivery.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.delivery.R;
import com.laundromat.delivery.activities.SignupActivity;
import com.laundromat.delivery.model.Vehicle;
import com.laundromat.delivery.ui.interfaces.IVehicleSignupListener;
import com.laundromat.delivery.utils.ImageUtils;
import com.laundromat.delivery.utils.ValidationUtils;

import java.util.HashMap;

public class VehicleSignupFragment extends Fragment
        implements View.OnClickListener, TextWatcher {

    // Constants
    private static final int REQUEST_CODE_FRONT = 111;
    private static final int REQUEST_CODE_BACK = 222;
    private static final int REQUEST_CODE_LEFT = 333;
    private static final int REQUEST_CODE_RIGHT = 444;

    // Widgets
    private ImageView imageViewVehicleFront;
    private ImageView imageViewVehicleBack;
    private ImageView imageViewVehicleLeft;
    private ImageView imageViewVehicleRight;
    private EditText editTextPlateNumber;
    private EditText editTextVehicleName;
    private EditText editTextVehicleModel;
    private EditText editTextVehicleColor;
    private Button buttonPrevious;
    private Button buttonProceed;

    // Interface
    private IVehicleSignupListener iVehicleSignupListener;

    // Variables
    private Uri imageUriVehicleFront;
    private Uri imageUriVehicleBack;
    private Uri imageUriVehicleLeft;
    private Uri imageUriVehicleRight;

    public VehicleSignupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_vehicle_signup, container, false);

        initViews(view);

        return view;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        try {
            iVehicleSignupListener = (IVehicleSignupListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " Must implement IVehicleSignup");
        }
    }

    private void initViews(View view) {

        this.imageViewVehicleFront = view.findViewById(R.id.image_view_vehicle_front);
        this.imageViewVehicleBack = view.findViewById(R.id.image_view_vehicle_back);
        this.imageViewVehicleLeft = view.findViewById(R.id.image_view_vehicle_left);
        this.imageViewVehicleRight = view.findViewById(R.id.image_view_vehicle_right);

        this.editTextPlateNumber = view.findViewById(R.id.edit_text_plate_number);
        editTextPlateNumber.addTextChangedListener(this);

        this.editTextVehicleName = view.findViewById(R.id.edit_text_vehicle_name);
        editTextVehicleName.addTextChangedListener(this);

        this.editTextVehicleModel = view.findViewById(R.id.edit_text_vehicle_model);
        editTextVehicleModel.addTextChangedListener(this);

        this.editTextVehicleColor = view.findViewById(R.id.edit_text_vehicle_color);
        editTextVehicleColor.addTextChangedListener(this);

        this.buttonPrevious = view.findViewById(R.id.button_previous);
        this.buttonProceed = view.findViewById(R.id.button_proceed);

        this.imageViewVehicleFront.setOnClickListener(this);
        this.imageViewVehicleBack.setOnClickListener(this);
        this.imageViewVehicleLeft.setOnClickListener(this);
        this.imageViewVehicleRight.setOnClickListener(this);
        this.buttonPrevious.setOnClickListener(this);
        this.buttonProceed.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.image_view_vehicle_front) {

            getImageFromUser(REQUEST_CODE_FRONT);

        }
        if (view.getId() == R.id.image_view_vehicle_back) {

            getImageFromUser(REQUEST_CODE_BACK);

        }
        if (view.getId() == R.id.image_view_vehicle_left) {

            getImageFromUser(REQUEST_CODE_LEFT);

        }
        if (view.getId() == R.id.image_view_vehicle_right) {

            getImageFromUser(REQUEST_CODE_RIGHT);

        } else if (view.getId() == R.id.button_previous) {

            if (iVehicleSignupListener != null) {

                iVehicleSignupListener.onButtonPreviousClick();
            }

        } else if (view.getId() == R.id.button_proceed) {

            initVehicle();
        }
    }

    private void getImageFromUser(int requestCode) {

        ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(requestCode);
    }

    private void initVehicle() {

        String plateNumber = editTextPlateNumber.getText().toString().trim();
        String vehicleName = editTextVehicleName.getText().toString().trim();
        String vehicleModel = editTextVehicleModel.getText().toString().trim();
        String vehicleColor = editTextVehicleColor.getText().toString().trim();

        if (TextUtils.isEmpty(plateNumber) || TextUtils.isEmpty(vehicleName)
                || TextUtils.isEmpty(vehicleModel) || TextUtils.isEmpty(vehicleColor)
                || imageUriVehicleFront == null || imageUriVehicleBack == null
                || imageUriVehicleLeft == null || imageUriVehicleRight == null) {

            Toast.makeText(getContext(),
                    "All fields must be filled", Toast.LENGTH_SHORT).show();

            return;
        }

        if (!ValidationUtils.isInputValid(vehicleName)) {

            editTextVehicleName.setError("Vehicle name has invalid format");
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

        String front64 = ImageUtils.uriToBase64(getContext(), imageUriVehicleFront);
        String back64 = ImageUtils.uriToBase64(getContext(), imageUriVehicleBack);
        String left64 = ImageUtils.uriToBase64(getContext(), imageUriVehicleLeft);
        String right64 = ImageUtils.uriToBase64(getContext(), imageUriVehicleRight);

        HashMap<String, String> images = new HashMap<>();
        images.put("front", front64);
        images.put("back", back64);
        images.put("left", left64);
        images.put("right", right64);

        Vehicle vehicle = new Vehicle(vehicleName, plateNumber, vehicleColor, model, images);

        verifyVehicleData(vehicle);
    }

    private void verifyVehicleData(Vehicle vehicle) {

        ((SignupActivity) getActivity()).showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("vehicle-verifyNewVehicleData")
                .call(vehicle.toJson())
                .addOnSuccessListener(httpsCallableResult -> {

                    ((SignupActivity) getActivity()).hideLoadingAnimation();

                    if (httpsCallableResult.getData() != null) {

                        boolean exist = (boolean) httpsCallableResult.getData();
                        Log.d("call_done", "verifyNewMerchantData: " + exist);

                        if (!exist) {

                            if (iVehicleSignupListener != null) {

                                iVehicleSignupListener.onVehicleSignup(vehicle);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    ((SignupActivity) getActivity()).hideLoadingAnimation();

                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_FRONT && resultCode == Activity.RESULT_OK) {

            if (data != null) {

                imageUriVehicleFront = data.getData();
                imageViewVehicleFront.setImageURI(imageUriVehicleFront);
            }

        } else if (requestCode == REQUEST_CODE_BACK && resultCode == Activity.RESULT_OK) {

            if (data != null) {

                imageUriVehicleBack = data.getData();
                imageViewVehicleBack.setImageURI(imageUriVehicleBack);
            }

        } else if (requestCode == REQUEST_CODE_LEFT && resultCode == Activity.RESULT_OK) {

            if (data != null) {

                imageUriVehicleLeft = data.getData();
                imageViewVehicleLeft.setImageURI(imageUriVehicleLeft);
            }

        } else if (requestCode == REQUEST_CODE_RIGHT && resultCode == Activity.RESULT_OK) {

            if (data != null) {

                imageUriVehicleRight = data.getData();
                imageViewVehicleRight.setImageURI(imageUriVehicleRight);
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