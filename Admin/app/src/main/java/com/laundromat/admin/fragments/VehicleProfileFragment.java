package com.laundromat.admin.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.laundromat.admin.R;
import com.laundromat.admin.dialogs.EditLaundryDialog;
import com.laundromat.admin.dialogs.EditVehicleDialog;
import com.laundromat.admin.dialogs.EditVehicleImagesDialog;
import com.laundromat.admin.model.Vehicle;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.interfaces.IVehicleEditedListener;
import com.laundromat.admin.ui.interfaces.IVehicleImagesEditedListener;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.ImageUtils;
import com.squareup.picasso.Picasso;

public class VehicleProfileFragment extends Fragment
        implements View.OnClickListener, IVehicleEditedListener, IVehicleImagesEditedListener {

    // Constants
    private static final String VEHICLE = "vehicle";
    private static final String INDEX = "index";

    // Variables
    private Vehicle vehicle;
    private int index;

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

    private Button buttonEditVehicle;
    private Button buttonEditImages;

    public VehicleProfileFragment() {
        // Required empty public constructor
    }

    public static VehicleProfileFragment newInstance(String vehicleGson, int index) {

        VehicleProfileFragment fragment = new VehicleProfileFragment();
        Bundle args = new Bundle();
        args.putString(VEHICLE, vehicleGson);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            String vehicleGson = getArguments().getString(VEHICLE);
            index = getArguments().getInt(INDEX);

            if (vehicleGson != null) {

                vehicle = GsonUtils.gsonToVehicle(vehicleGson);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_vehicle_profile, container, false);

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

        buttonEditVehicle = view.findViewById(R.id.button_edit_vehicle);
        buttonEditVehicle.setOnClickListener(this);

        buttonEditImages = view.findViewById(R.id.button_edit_images);
        buttonEditImages.setOnClickListener(this);

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

        if (view.getId() == R.id.button_edit_vehicle) {

            Bundle bundle = new Bundle();
            bundle.putString("vehicle", GsonUtils.vehicleToGson(vehicle));

            EditVehicleDialog dialog = new EditVehicleDialog();
            dialog.setArguments(bundle);
            dialog.setCancelable(false);
            dialog.setListener(this);
            dialog.show(getChildFragmentManager(), "dialog_edit_vehicle");

        } else if (view.getId() == R.id.button_edit_images) {

            Bundle bundle = new Bundle();
            bundle.putString("vehicle", GsonUtils.vehicleToGson(vehicle));

            EditVehicleImagesDialog dialog = new EditVehicleImagesDialog();
            dialog.setArguments(bundle);
            dialog.setCancelable(false);
            dialog.setListener(this);
            dialog.show(getChildFragmentManager(), "dialog_edit_vehicle_images");

        } else if (view.getId() == R.id.image_view_vehicle) {

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

    @Override
    public void onVehicleEdited(String name, String plateNumber, String color, int model) {

        // update locally
        Session.user.getDeliveryBoys().get(index).getVehicle().setName(name);
        Session.user.getDeliveryBoys().get(index).getVehicle().setPlateNumber(plateNumber);
        Session.user.getDeliveryBoys().get(index).getVehicle().setColor(color);
        Session.user.getDeliveryBoys().get(index).getVehicle().setModel(model);

        vehicle = Session.user.getDeliveryBoys().get(index).getVehicle();

        setupViews();

        Toast.makeText(getContext(), "Vehicle Details Updated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVehicleImagesEdited(String front, String back, String left, String right) {

        // update locally
        Session.user.getDeliveryBoys().get(index).getVehicle().getImages().put("front", front);
        Session.user.getDeliveryBoys().get(index).getVehicle().getImages().put("back", back);
        Session.user.getDeliveryBoys().get(index).getVehicle().getImages().put("left", left);
        Session.user.getDeliveryBoys().get(index).getVehicle().getImages().put("right", right);

        vehicle = Session.user.getDeliveryBoys().get(index).getVehicle();

        // invalidate caches
        Picasso.get()
                .invalidate(front);

        Picasso.get()
                .invalidate(back);

        Picasso.get()
                .invalidate(left);

        Picasso.get()
                .invalidate(right);

        setupViews();

        Toast.makeText(getContext(), "Vehicle Images Updated", Toast.LENGTH_SHORT).show();
    }
}