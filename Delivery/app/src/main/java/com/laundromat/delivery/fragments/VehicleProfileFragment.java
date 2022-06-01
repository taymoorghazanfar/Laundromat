package com.laundromat.delivery.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.laundromat.delivery.R;
import com.laundromat.delivery.model.Vehicle;
import com.laundromat.delivery.prefs.Session;
import com.laundromat.delivery.utils.ImageUtils;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;

import de.hdodenhof.circleimageview.CircleImageView;

public class VehicleProfileFragment extends Fragment
        implements View.OnClickListener {

    // Views
    private ImageView imageViewVehicle;
    private TextView textViewName;
    private TextView textViewModel;
    private TextView textViewPlateNumber;
    private TextView textViewColor;

    public VehicleProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    private void setupViews() {

        Vehicle vehicle = Session.user.getVehicle();

        Picasso.get()
                .load(vehicle.images.get("front"))
                .into(imageViewVehicle);

        textViewName.setText(vehicle.getName());
        textViewModel.setText(String.valueOf(vehicle.getModel()));
        textViewPlateNumber.setText(vehicle.getPlateNumber());
        textViewColor.setText(vehicle.getColor());
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.image_view_vehicle) {

            ImageUtils.showImage(getContext(),
                    Session.user.getVehicle().images.get("front"));
        }
    }
}