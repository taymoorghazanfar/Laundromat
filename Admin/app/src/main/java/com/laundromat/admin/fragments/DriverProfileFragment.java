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
import com.laundromat.admin.dialogs.EditDriverDialog;
import com.laundromat.admin.model.DeliveryBoy;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.interfaces.IDriverEditedListener;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.ImageUtils;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverProfileFragment extends Fragment
        implements View.OnClickListener, IDriverEditedListener {

    // Constants
    private static final String DRIVER = "driver";
    private static final String INDEX = "index";

    // Variables
    DeliveryBoy driver;
    private int index;

    // Views
    private CircleImageView imageViewAvatar;
    private TextView textViewName;
    private TextView textViewPhone;
    private TextView textViewEmail;
    private TextView textViewJazz;
    private TextView textViewNic;
    private TextView textViewLicense;
    private ImageView imageViewNic;
    private ImageView imageViewLicense;

    private Button buttonEditProfile;

    public DriverProfileFragment() {
        // Required empty public constructor
    }

    public static DriverProfileFragment newInstance(String driverGson, int index) {
        DriverProfileFragment fragment = new DriverProfileFragment();
        Bundle args = new Bundle();
        args.putString(DRIVER, driverGson);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            String driverGson = getArguments().getString(DRIVER);
            index = getArguments().getInt(INDEX);

            if (driverGson != null) {

                driver = GsonUtils.gsonToDriver(driverGson);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_profile, container, false);

        initViews(view);

        setupViews();

        return view;
    }

    private void initViews(View view) {

        imageViewAvatar = view.findViewById(R.id.image_view_avatar);
        imageViewAvatar.setOnClickListener(this);

        textViewName = view.findViewById(R.id.text_view_name);
        textViewPhone = view.findViewById(R.id.text_view_phone);
        textViewEmail = view.findViewById(R.id.text_view_email);
        textViewJazz = view.findViewById(R.id.text_view_jazz);
        textViewNic = view.findViewById(R.id.text_view_nic);
        textViewLicense = view.findViewById(R.id.text_view_license);

        imageViewNic = view.findViewById(R.id.image_view_nic);
        imageViewNic.setOnClickListener(this);

        imageViewLicense = view.findViewById(R.id.image_view_license);
        imageViewLicense.setOnClickListener(this);

        buttonEditProfile = view.findViewById(R.id.button_edit_profile);
        buttonEditProfile.setOnClickListener(this);
    }

    private void setupViews() {

        Picasso.get()
                .load(driver.getAvatarUrl())
                .into(imageViewAvatar);

        textViewName.setText(driver.getFullName());

        textViewPhone.setText(MessageFormat.format("+92{0}",
                driver.getPhoneNumber()));

        textViewEmail.setText(driver.getEmail());

        textViewJazz.setText(driver.getJazzCashNumber());

        textViewNic.setText(driver.getNicNumber());

        textViewLicense.setText(driver.getLicenseNumber());

        Picasso.get()
                .load(driver.getNicImageUrl())
                .into(imageViewNic);

        Picasso.get()
                .load(driver.getLicenseImageUrl())
                .into(imageViewLicense);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_edit_profile) {

            Bundle bundle = new Bundle();
            bundle.putString("driver", GsonUtils.driverToGson(driver));

            EditDriverDialog dialog = new EditDriverDialog();
            dialog.setArguments(bundle);
            dialog.setCancelable(false);
            dialog.setListener(this);
            dialog.show(getChildFragmentManager(), "dialog_edit_merchant");

        } else if (view.getId() == R.id.image_view_avatar) {

            ImageUtils.showImage(getContext(), driver.getAvatarUrl());

        } else if (view.getId() == R.id.image_view_nic) {

            ImageUtils.showImage(getContext(), driver.getNicImageUrl());

        } else if (view.getId() == R.id.image_view_license) {

            ImageUtils.showImage(getContext(), driver.getLicenseImageUrl());
        }
    }

    @Override
    public void onDriverEdited(String avatarUrl, String fullName, String email,
                               String jazzCashNumber, String licenseNumber) {

        // update locally
        Session.user.getDeliveryBoys().get(index).setFullName(fullName);
        Session.user.getDeliveryBoys().get(index).setEmail(email);
        Session.user.getDeliveryBoys().get(index).setJazzCashNumber(jazzCashNumber);
        Session.user.getDeliveryBoys().get(index).setLicenseNumber(licenseNumber);

        if (avatarUrl != null) {

            Session.user.getDeliveryBoys().get(index).setAvatarUrl(avatarUrl);
            Picasso.get()
                    .invalidate(avatarUrl);
        }

        driver = Session.user.getDeliveryBoys().get(index);

        setupViews();

        Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
    }
}