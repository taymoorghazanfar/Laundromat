package com.laundromat.admin.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.admin.R;
import com.laundromat.admin.activities.DriverProfileActivity;
import com.laundromat.admin.model.Vehicle;
import com.laundromat.admin.ui.interfaces.IVehicleImagesEditedListener;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.ImageUtils;

import java.util.HashMap;
import java.util.Objects;

public class EditVehicleImagesDialog extends androidx.fragment.app.DialogFragment
        implements View.OnClickListener {

    // Constants
    private static final int REQUEST_CODE_FRONT = 111;
    private static final int REQUEST_CODE_BACK = 222;
    private static final int REQUEST_CODE_LEFT = 333;
    private static final int REQUEST_CODE_RIGHT = 444;

    // Interfaces
    IVehicleImagesEditedListener vehicleImagesEditedListener;

    // Views
    private ImageView imageViewVehicleFront;
    private ImageView imageViewVehicleBack;
    private ImageView imageViewVehicleLeft;
    private ImageView imageViewVehicleRight;

    private Button buttonCancel;
    private Button buttonUpdate;

    //Variables
    private Vehicle vehicle;

    private Uri imageUriVehicleFront;
    private Uri imageUriVehicleBack;
    private Uri imageUriVehicleLeft;
    private Uri imageUriVehicleRight;

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

        return inflater.inflate(R.layout.dialog_edit_vehicle_images, container, false);
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

    public void setListener(IVehicleImagesEditedListener vehicleImagesEditedListener) {
        this.vehicleImagesEditedListener = vehicleImagesEditedListener;
    }

    private void initViews(View view) {

        this.imageViewVehicleFront = view.findViewById(R.id.image_view_vehicle_front);
        this.imageViewVehicleBack = view.findViewById(R.id.image_view_vehicle_back);
        this.imageViewVehicleLeft = view.findViewById(R.id.image_view_vehicle_left);
        this.imageViewVehicleRight = view.findViewById(R.id.image_view_vehicle_right);

        this.imageViewVehicleFront.setOnClickListener(this);
        this.imageViewVehicleBack.setOnClickListener(this);
        this.imageViewVehicleLeft.setOnClickListener(this);
        this.imageViewVehicleRight.setOnClickListener(this);

        buttonUpdate = view.findViewById(R.id.button_update);
        buttonUpdate.setOnClickListener(this);

        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(this);
    }

    private void update() {

        // check empty
        if (imageUriVehicleFront == null || imageUriVehicleBack == null ||
                imageUriVehicleLeft == null || imageUriVehicleRight == null) {

            Toast.makeText(getContext(),
                    "All fields must be filled", Toast.LENGTH_SHORT).show();

            return;
        }

        String front64 = ImageUtils.uriToBase64(getContext(), imageUriVehicleFront);
        String back64 = ImageUtils.uriToBase64(getContext(), imageUriVehicleBack);
        String left64 = ImageUtils.uriToBase64(getContext(), imageUriVehicleLeft);
        String right64 = ImageUtils.uriToBase64(getContext(), imageUriVehicleRight);

        HashMap<String, String> data = new HashMap<>();
        data.put("vehicle_id", vehicle.getId());
        data.put("plate_number", vehicle.getPlateNumber());
        data.put("front", front64);
        data.put("back", back64);
        data.put("left", left64);
        data.put("right", right64);

        getDialog().hide();
        ((DriverProfileActivity) getActivity()).showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("vehicle-updateImages")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        ((DriverProfileActivity) getActivity()).hideLoadingAnimation();

                        String response = (String) httpsCallableResult.getData();

                        String[] urls = response.split("\\*");

                        if (vehicleImagesEditedListener != null) {

                            vehicleImagesEditedListener
                                    .onVehicleImagesEdited
                                            (urls[0], urls[1], urls[2], urls[3]);
                            dismiss();
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    ((DriverProfileActivity) getActivity()).hideLoadingAnimation();
                    getDialog().show();

                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void getImageFromUser(int requestCode) {

        ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(requestCode);
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

        } else if (view.getId() == R.id.button_update) {

            update();

        } else if (view.getId() == R.id.button_cancel) {

            dismiss();
        }
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
}
