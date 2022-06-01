package com.laundromat.merchant.fragments;

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

import com.laundromat.merchant.R;
import com.laundromat.merchant.dialogs.EditLaundryDialog;
import com.laundromat.merchant.model.Laundry;
import com.laundromat.merchant.model.Transaction;
import com.laundromat.merchant.model.util.TransactionType;
import com.laundromat.merchant.model.washable.WashableItemCategory;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.interfaces.ILaundryEditedListener;
import com.laundromat.merchant.utils.ImageUtils;
import com.laundromat.merchant.utils.LocationUtils;
import com.laundromat.merchant.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;

import de.hdodenhof.circleimageview.CircleImageView;


public class LaundryProfileFragment extends Fragment
        implements View.OnClickListener, ILaundryEditedListener {

    // Views
    private CircleImageView imageViewAvatar;
    private TextView textViewName;
    private TextView textViewHomeBased;

    private TextView textViewOrders;
    private TextView textViewEarnings;
    private TextView textViewProducts;

    private TextView textViewOpeningTime;
    private TextView textViewClosingTime;
    private TextView textViewDiscount;
    private ImageView imageViewLocation;
    private TextView textViewLocationAddress;

    private Button buttonEditLaundry;

    public LaundryProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_laundry_profile, container, false);

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

        textViewDiscount = view.findViewById(R.id.text_view_discount);

        imageViewLocation = view.findViewById(R.id.image_view_location);
        textViewLocationAddress = view.findViewById(R.id.text_view_location_address);

        buttonEditLaundry = view.findViewById(R.id.button_edit_laundry);
        buttonEditLaundry.setOnClickListener(this);
    }

    private void setupViews() {

        Laundry laundry = Session.user.getLaundry();

        Picasso.get()
                .load(Session.user.getLaundry().getLogoUrl())
                .into(imageViewAvatar);

        textViewName.setText(laundry.getName());

        textViewHomeBased.setVisibility(laundry.isHomeBased() ? View.VISIBLE : View.GONE);

        getTotalOrders();

        getTotalEarnings();

        getTotalProducts();

        textViewOpeningTime.setText(laundry.getTimings().getOpeningTime());

        textViewClosingTime.setText(laundry.getTimings().getClosingTime());

        textViewDiscount.setText(MessageFormat.format("{0}% Off", laundry.getDiscount()));

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
                Session.user.getLaundry().getOrders().size()));
    }

    private void getTotalEarnings() {

        double earnings = 0;

        for (Transaction transaction : Session.user.getTransactions()) {

            if (transaction.getType() == TransactionType.EARNING) {

                earnings += transaction.getAmount();
            }
        }

        textViewEarnings.setText(MessageFormat
                .format("PKR {0}\nEarnings", earnings));
    }

    private void getTotalProducts() {

        int total = 0;

        for (WashableItemCategory category : Session.user.getLaundry().getMenu()) {

            total += category.getWashableItems().size();
        }

        textViewProducts.setText(MessageFormat
                .format("{0}\nProducts", total));
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_edit_laundry) {

            EditLaundryDialog dialog = new EditLaundryDialog();
            dialog.setCancelable(false);
            dialog.setListener(this);
            dialog.show(getChildFragmentManager(), "dialog_edit_laundry");

        } else if (view.getId() == R.id.image_view_avatar) {

            ImageUtils.showImage(getContext(), Session.user.getLaundry().getLogoUrl());
        }
    }

    @Override
    public void onLaundryEdited(String logoUrl, String openingTime, String closingTime, double discount) {

        Session.user.getLaundry().getTimings().setOpeningTime(openingTime);
        Session.user.getLaundry().getTimings().setClosingTime(closingTime);
        Session.user.getLaundry().setDiscount(discount);

        if (logoUrl != null) {

            Session.user.getLaundry().setLogoUrl(logoUrl);
        }

        Picasso.get()
                .invalidate(logoUrl);

        setupViews();

        Toast.makeText(getContext(), "Laundry Updated", Toast.LENGTH_SHORT).show();
    }
}