package com.laundromat.admin.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.LatLng;
import com.laundromat.admin.R;
import com.laundromat.admin.dialogs.EditMerchantDialog;
import com.laundromat.admin.model.Merchant;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.interfaces.IMerchantEditedListener;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.ImageUtils;
import com.laundromat.admin.utils.LocationUtils;
import com.laundromat.admin.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;

import de.hdodenhof.circleimageview.CircleImageView;

public class MerchantProfileFragment extends Fragment
        implements View.OnClickListener, IMerchantEditedListener {

    // Constants
    private static final String MERCHANT = "merchant";
    private static final String INDEX = "index";

    // Variables
    private Merchant merchant;
    private int index;

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

    private Button buttonEditProfile;

    public MerchantProfileFragment() {
        // Required empty public constructor
    }

    public static MerchantProfileFragment newInstance(String merchantGson, int index) {
        MerchantProfileFragment fragment = new MerchantProfileFragment();
        Bundle args = new Bundle();
        args.putString(MERCHANT, merchantGson);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            String merchantGson = getArguments().getString(MERCHANT);
            index = getArguments().getInt(INDEX);

            if (merchantGson != null) {

                merchant = GsonUtils.gsonToMerchant(merchantGson);
            }
        }
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

        buttonEditProfile = view.findViewById(R.id.button_edit_profile);
        buttonEditProfile.setOnClickListener(this);
    }

    private void setupViews() {

        Log.d("profile", "setupViews: setup");

        Picasso.get()
                .load(merchant.getAvatarUrl())
                .into(imageViewAvatar);

        textViewName.setText(merchant.getFullName());

        textViewPhone.setText(MessageFormat.format("+92{0}",
                merchant.getPhoneNumber()));

        textViewEmail.setText(merchant.getEmail());

        textViewJazz.setText(merchant.getJazzCashNumber());

        textViewNic.setText(merchant.getNicNumber());

        Picasso.get()
                .load(merchant.getNicImageUrl())
                .into(imageViewNic);

        String locationImageUrl = StringUtils.getMapsStaticImageUrl(getContext(),
                merchant.getLocation());

        Picasso.get()
                .load(locationImageUrl)
                .into(imageViewLocation);

        String locationAddress = LocationUtils.getAddressFromLatLng(getContext(),
                merchant.getLocation().latitude, merchant.getLocation().longitude);

        textViewLocationAddress.setText(locationAddress);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_edit_profile) {

            Bundle bundle = new Bundle();
            bundle.putString("merchant", GsonUtils.merchantToGson(merchant));

            EditMerchantDialog dialog = new EditMerchantDialog();
            dialog.setArguments(bundle);
            dialog.setCancelable(false);
            dialog.setListener(this);
            dialog.show(getChildFragmentManager(), "dialog_edit_merchant");

        } else if (view.getId() == R.id.image_view_avatar) {

            ImageUtils.showImage(getContext(), merchant.getAvatarUrl());

        } else if (view.getId() == R.id.image_view_nic) {

            ImageUtils.showImage(getContext(), merchant.getNicImageUrl());
        }
    }

    @Override
    public void onMerchantEdited(String avatarUrl, String fullName,
                                 String email, String jazzCashNumber, LatLng location) {

        // update locally
        Session.user.getMerchants().get(index).setFullName(fullName);
        Session.user.getMerchants().get(index).setEmail(email);
        Session.user.getMerchants().get(index).setJazzCashNumber(jazzCashNumber);

        if (avatarUrl != null) {

            Session.user.getMerchants().get(index).setAvatarUrl(avatarUrl);
            Picasso.get()
                    .invalidate(avatarUrl);
        }

        if (location != null) {

            Session.user.getMerchants().get(index).setLocation(location);
        }

        merchant = Session.user.getMerchants().get(index);

        setupViews();

        Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
    }
}