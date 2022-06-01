package com.laundromat.admin.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.laundromat.admin.R;
import com.laundromat.admin.model.Laundry;
import com.laundromat.admin.model.washable.WashableItemCategory;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.ImageUtils;
import com.laundromat.admin.utils.LocationUtils;
import com.laundromat.admin.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;

import de.hdodenhof.circleimageview.CircleImageView;

public class LaundryRequestProfileFragment extends Fragment implements View.OnClickListener {

    // Constants
    private static final String LAUNDRY = "laundry";

    // Variables
    Laundry laundry;

    // Views
    private CircleImageView imageViewAvatar;
    private TextView textViewName;
    private TextView textViewHomeBased;

    private TextView textViewOrders;
    private TextView textViewEarnings;
    private TextView textViewProducts;

    private TextView textViewOpeningTime;
    private TextView textViewClosingTime;
    private ImageView imageViewLocation;
    private TextView textViewLocationAddress;

    public LaundryRequestProfileFragment() {
        // Required empty public constructor
    }

    public static LaundryRequestProfileFragment newInstance(String laundryGson) {
        LaundryRequestProfileFragment fragment = new LaundryRequestProfileFragment();
        Bundle args = new Bundle();
        args.putString(LAUNDRY, laundryGson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            String laundryGson = getArguments().getString(LAUNDRY);

            if (laundryGson != null) {

                laundry = GsonUtils.gsonToLaundry(laundryGson);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_laundry_request_profile, container, false);

        initViews(view);

        setupViews();

        return view;
    }

    private void initViews(View view) {

        imageViewAvatar = view.findViewById(R.id.image_view_avatar);
        imageViewAvatar.setOnClickListener(this);

        textViewName = view.findViewById(R.id.text_view_name);
        textViewHomeBased = view.findViewById(R.id.text_view_home_based);

        textViewOrders = view.findViewById(R.id.text_view_total_orders);
        textViewEarnings = view.findViewById(R.id.text_view_earning);
        textViewProducts = view.findViewById(R.id.text_view_total_products);

        textViewOpeningTime = view.findViewById(R.id.text_view_opening_time);
        textViewClosingTime = view.findViewById(R.id.text_view_closing_time);

        imageViewLocation = view.findViewById(R.id.image_view_location);
        textViewLocationAddress = view.findViewById(R.id.text_view_location_address);
    }

    private void setupViews() {

        Picasso.get()
                .load(laundry.getLogoUrl())
                .into(imageViewAvatar);

        textViewName.setText(laundry.getName());

        textViewHomeBased.setVisibility(laundry.isHomeBased() ? View.VISIBLE : View.GONE);

        getTotalOrders();

        getTotalEarnings();

        getTotalProducts();

        textViewOpeningTime.setText(laundry.getTimings().getOpeningTime());

        textViewClosingTime.setText(laundry.getTimings().getClosingTime());

        String locationImageUrl = StringUtils.getMapsStaticImageUrl(getContext(),
                laundry.getLocation());

        Picasso.get()
                .load(locationImageUrl)
                .into(imageViewLocation);

        String locationAddress = LocationUtils.getAddressFromLatLng(getContext(),
                laundry.getLocation().latitude, laundry.getLocation().longitude);

        textViewLocationAddress.setText(locationAddress);
    }

    private void getTotalOrders() {

        textViewOrders.setText(MessageFormat.format("{0}\nOrders",
                laundry.getOrders().size()));
    }

    private void getTotalEarnings() {

        textViewEarnings.setText(MessageFormat
                .format("PKR {0}\nEarnings", 0));
    }

    private void getTotalProducts() {

        int total = 0;

        for (WashableItemCategory category : laundry.getMenu()) {

            total += category.getWashableItems().size();
        }

        textViewProducts.setText(MessageFormat
                .format("{0}\nProducts", total));
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.image_view_avatar) {

            ImageUtils.showImage(getContext(), laundry.getLogoUrl());
        }
    }
}