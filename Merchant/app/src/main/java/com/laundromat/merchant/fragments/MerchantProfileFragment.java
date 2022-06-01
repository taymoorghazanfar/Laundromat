package com.laundromat.merchant.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.laundromat.merchant.R;
import com.laundromat.merchant.dialogs.DeleteMenuItemDialog;
import com.laundromat.merchant.dialogs.EditMerchantDialog;
import com.laundromat.merchant.dialogs.UpdatePasswordDialog;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.interfaces.IMerchantEditedListener;
import com.laundromat.merchant.ui.interfaces.IPasswordUpdatedListener;
import com.laundromat.merchant.utils.GsonUtils;
import com.laundromat.merchant.utils.ImageUtils;
import com.laundromat.merchant.utils.LocationUtils;
import com.laundromat.merchant.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;

import de.hdodenhof.circleimageview.CircleImageView;


public class MerchantProfileFragment extends Fragment
        implements View.OnClickListener, IPasswordUpdatedListener, IMerchantEditedListener {

    // Views
    private CircleImageView imageViewAvatar;
    private TextView textViewName;
    private TextView textViewPhone;
    private TextView textViewEmail;
    private TextView textViewJazz;
    private TextView textViewNic;
    private ImageView imageViewNic;
    private ImageView imageViewLocation;
    private TextView textViewLocationAddress;

    private Button buttonChangePassword;
    private Button buttonEditProfile;

    public MerchantProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_merchant_profile, container, false);

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

        imageViewNic = view.findViewById(R.id.image_view_nic);
        imageViewNic.setOnClickListener(this);

        imageViewLocation = view.findViewById(R.id.image_view_location);
        textViewLocationAddress = view.findViewById(R.id.text_view_location_address);

        buttonChangePassword = view.findViewById(R.id.button_change_password);
        buttonChangePassword.setOnClickListener(this);

        buttonEditProfile = view.findViewById(R.id.button_edit_profile);
        buttonEditProfile.setOnClickListener(this);
    }

    private void setupViews() {

        Log.d("profile", "setupViews: setup");

        Picasso.get()
                .load(Session.user.getAvatarUrl())
                .into(imageViewAvatar);

        textViewName.setText(Session.user.getFullName());

        textViewPhone.setText(MessageFormat.format("+92{0}",
                Session.user.getPhoneNumber()));

        textViewEmail.setText(Session.user.getEmail());

        textViewJazz.setText(Session.user.getJazzCashNumber());

        textViewNic.setText(Session.user.getNicNumber());

        Picasso.get()
                .load(Session.user.getNicImageUrl())
                .into(imageViewNic);

        String locationImageUrl = StringUtils.getMapsStaticImageUrl(getContext(),
                Session.user.getLocation());

        Picasso.get()
                .load(locationImageUrl)
                .into(imageViewLocation);

        String locationAddress = LocationUtils.getAddressFromLatLng(getContext(),
                Session.user.getLocation().latitude, Session.user.getLocation().longitude);

        textViewLocationAddress.setText(locationAddress);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_change_password) {

            Bundle bundle = new Bundle();
            bundle.putString("password", Session.user.getPassword());

            UpdatePasswordDialog dialog = new UpdatePasswordDialog();
            dialog.setArguments(bundle);
            dialog.setCancelable(false);
            dialog.setListener(this);
            dialog.show(getChildFragmentManager(), "dialog_update_password");

        } else if (view.getId() == R.id.button_edit_profile) {

            EditMerchantDialog dialog = new EditMerchantDialog();
            dialog.setCancelable(false);
            dialog.setListener(this);
            dialog.show(getChildFragmentManager(), "dialog_edit_merchant");

        } else if (view.getId() == R.id.image_view_avatar) {

            ImageUtils.showImage(getContext(), Session.user.getAvatarUrl());

        } else if (view.getId() == R.id.image_view_nic) {

            ImageUtils.showImage(getContext(), Session.user.getNicImageUrl());
        }
    }

    @Override
    public void onPasswordUpdated(String password) {

        // locally save the password
        Session.user.setPassword(password);

        // save password to prefs
        Session.setPassword(getContext(), password);

        Toast.makeText(getContext(), "Password Updated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMerchantEdited(String avatarUrl, String fullName, String phoneNumber,
                                 String email, String jazzCashNumber, LatLng location) {

        // update locally
        Session.user.setFullName(fullName);
        Session.user.setPhoneNumber(phoneNumber);
        Session.user.setEmail(email);
        Session.user.setJazzCashNumber(jazzCashNumber);

        if (avatarUrl != null) {

            Session.user.setAvatarUrl(avatarUrl);
            Picasso.get()
                    .invalidate(avatarUrl);
        }

        if (location != null) {

            Session.user.setLocation(location);
        }

        // check if phone number was updated
        if (!phoneNumber.equals(Session.getPhoneNumber(getContext()))) {

            // update phone number in session
            Session.setPhoneNumber(getContext(), phoneNumber);
        }

        setupViews();

        Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
    }
}