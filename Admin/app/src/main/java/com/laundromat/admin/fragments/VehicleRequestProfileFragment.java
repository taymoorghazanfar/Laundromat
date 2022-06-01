package com.laundromat.admin.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.laundromat.admin.R;
import com.laundromat.admin.model.Vehicle;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.ImageUtils;
import com.squareup.picasso.Picasso;

public class VehicleRequestProfileFragment extends Fragment implements View.OnClickListener {

    // Constants
    private static final String VEHICLE = "vehicle";

    // Variables
    Vehicle vehicle;

    // Views
    private ImageView imageViewVehicle;
    private TextView textViewName;
    private TextView textViewModel;
    private TextView textViewPlateNumber;
    private TextView textViewColor;

    private ImageView imageViewFront;
    private ImageView imageViewBack;
    private ImageView imageViewLeft;
    private ImageView imageViewRight;

    public VehicleRequestProfileFragment() {
        // Required empty public constructor
    }

    public static VehicleRequestProfileFragment newInstance(String vehicleGson) {
        VehicleRequestProfileFragment fragment = new VehicleRequestProfileFragment();
        Bundle args = new Bundle();
        args.putString(VEHICLE, vehicleGson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            String vehicleGson = getArguments().getString(VEHICLE);

            if (vehicleGson != null) {

                vehicle = GsonUtils.gsonToVehicle(vehicleGson);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_vehicle_request_profile, container, false);

        initViews(view);

        setupViews();

        return view;
    }

    private void initViews(View view) {

        imageViewVehicle = view.findViewById(R.id.image_view_vehicle);
        imageViewVehicle.setOnClickListener(this);

        textViewName = view.findViewById(R.id.text_view_name);
        textViewModel = view.findViewById(R.id.text_view_model);
        textViewPlateNumber = view.findViewById(R.id.text_view_plate_number);
        textViewColor = view.findViewById(R.id.text_view_color);

        imageViewFront = view.findViewById(R.id.image_view_front);
        imageViewFront.setOnClickListener(this);

        imageViewBack = view.findViewById(R.id.image_view_back);
        imageViewBack.setOnClickListener(this);

        imageViewLeft = view.findViewById(R.id.image_view_left);
        imageViewLeft.setOnClickListener(this);

        imageViewRight = view.findViewById(R.id.image_view_right);
        imageViewRight.setOnClickListener(this);
    }

    private void setupViews() {

        Picasso.get()
                .load(vehicle.images.get("front"))
                .into(imageViewVehicle);

        textViewName.setText(vehicle.getName());
        textViewModel.setText(String.valueOf(vehicle.getModel()));
        textViewPlateNumber.setText(vehicle.getPlateNumber());
        textViewColor.setText(vehicle.getColor());

        Picasso.get()
                .load(vehicle.images.get("front"))
                .into(imageViewFront);

        Picasso.get()
                .load(vehicle.images.get("back"))
                .into(imageViewBack);

        Picasso.get()
                .load(vehicle.images.get("left"))
                .into(imageViewLeft);

        Picasso.get()
                .load(vehicle.images.get("right"))
                .into(imageViewRight);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.image_view_vehicle) {

            ImageUtils.showImage(getContext(), vehicle.images.get("front"));

        } else if (view.getId() == R.id.image_view_front) {

            ImageUtils.showImage(getContext(), vehicle.images.get("front"));

        } else if (view.getId() == R.id.image_view_back) {

            ImageUtils.showImage(getContext(), vehicle.images.get("back"));

        } else if (view.getId() == R.id.image_view_left) {

            ImageUtils.showImage(getContext(), vehicle.images.get("left"));

        } else if (view.getId() == R.id.image_view_right) {

            ImageUtils.showImage(getContext(), vehicle.images.get("right"));
        }
    }
}