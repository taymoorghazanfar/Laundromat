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
import com.laundromat.admin.model.Laundry;
import com.laundromat.admin.model.Merchant;
import com.laundromat.admin.model.Transaction;
import com.laundromat.admin.model.util.TransactionType;
import com.laundromat.admin.model.washable.WashableItemCategory;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.interfaces.ILaundryEditedListener;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.ImageUtils;
import com.laundromat.admin.utils.LocationUtils;
import com.laundromat.admin.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;

import de.hdodenhof.circleimageview.CircleImageView;

public class LaundryProfileFragment extends Fragment
        implements View.OnClickListener, ILaundryEditedListener {

    // Constants
    private static final String LAUNDRY = "laundry";
    private static final String MERCHANT = "merchant";
    private static final String INDEX = "index";

    // Variables
    private Laundry laundry;
    private Merchant merchant;
    private int index;

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

    public static LaundryProfileFragment newInstance(String laundryGson,
                                                     String merchantGson, int index) {

        LaundryProfileFragment fragment = new LaundryProfileFragment();
        Bundle args = new Bundle();
        args.putString(LAUNDRY, laundryGson);
        args.putString(MERCHANT, merchantGson);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            String laundryGson = getArguments().getString(LAUNDRY);
            String merchantGson = getArguments().getString(MERCHANT);
            index = getArguments().getInt(INDEX);

            if (laundryGson != null && merchantGson != null) {

                laundry = GsonUtils.gsonToLaundry(laundryGson);
                merchant = GsonUtils.gsonToMerchant(merchantGson);
            }
        }
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
                laundry.getOrders().size()));
    }

    private void getTotalEarnings() {

        double earnings = 0;

        for (Transaction transaction : merchant.getTransactions()) {

            if (transaction.getType() == TransactionType.EARNING) {

                earnings += transaction.getAmount();
            }
        }

        textViewEarnings.setText(MessageFormat
                .format("PKR {0}\nEarnings", earnings));
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

        if (view.getId() == R.id.button_edit_laundry) {

            Bundle bundle = new Bundle();
            bundle.putString("laundry", GsonUtils.laundryToGson(laundry));

            EditLaundryDialog dialog = new EditLaundryDialog();
            dialog.setArguments(bundle);
            dialog.setCancelable(false);
            dialog.setListener(this);
            dialog.show(getChildFragmentManager(), "dialog_edit_laundry");

        } else if (view.getId() == R.id.image_view_avatar) {

            ImageUtils.showImage(getContext(), laundry.getLogoUrl());
        }
    }

    @Override
    public void onLaundryEdited(String logoUrl, String openingTime, String closingTime) {

        Session.user.getMerchants().get(index).getLaundry()
                .getTimings().setOpeningTime(openingTime);

        Session.user.getMerchants().get(index).getLaundry()
                .getTimings().setClosingTime(closingTime);

        if (logoUrl != null) {

            Session.user.getMerchants().get(index).getLaundry().setLogoUrl(logoUrl);
        }

        Picasso.get()
                .invalidate(logoUrl);

        laundry = Session.user.getMerchants().get(index).getLaundry();

        setupViews();

        Toast.makeText(getContext(), "Laundry Updated", Toast.LENGTH_SHORT).show();
    }
}