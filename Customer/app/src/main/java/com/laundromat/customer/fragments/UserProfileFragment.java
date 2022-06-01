package com.laundromat.customer.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.laundromat.customer.R;
import com.laundromat.customer.dialogs.EditCustomerDialog;
import com.laundromat.customer.dialogs.UpdatePasswordDialog;
import com.laundromat.customer.model.Transaction;
import com.laundromat.customer.model.order.Order;
import com.laundromat.customer.model.order.OrderStatus;
import com.laundromat.customer.model.util.TransactionType;
import com.laundromat.customer.prefs.Session;
import com.laundromat.customer.ui.interfaces.ICustomerEditedListener;
import com.laundromat.customer.ui.interfaces.IPasswordUpdatedListener;
import com.laundromat.customer.utils.ImageUtils;
import com.laundromat.customer.utils.LocationUtils;
import com.laundromat.customer.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserProfileFragment extends Fragment
        implements View.OnClickListener, IPasswordUpdatedListener, ICustomerEditedListener {

    private CircleImageView imageViewAvatar;
    private TextView textViewName;
    private TextView textViewPhone;
    private TextView textViewEmail;

    private TextView textViewTotalOrders;
    private TextView textViewTotalSpent;
    private TextView textViewTotalPickup;
    private TextView textViewTotalTransactions;
    private TextView textViewTotalCancelled;
    private TextView textViewTotalAddresses;

    private ImageView imageViewLocation;
    private TextView textViewLocationAddress;

    private Button buttonChangePassword;
    private Button buttonEditProfile;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

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

        textViewTotalOrders = view.findViewById(R.id.text_view_total_orders);
        textViewTotalSpent = view.findViewById(R.id.text_view_total_spent);
        textViewTotalPickup = view.findViewById(R.id.text_view_total_pickup);
        textViewTotalTransactions = view.findViewById(R.id.text_view_total_transactions);
        textViewTotalCancelled = view.findViewById(R.id.text_view_total_cancelled);
        textViewTotalAddresses = view.findViewById(R.id.text_view_total_addresses);

        imageViewLocation = view.findViewById(R.id.image_view_location);
        textViewLocationAddress = view.findViewById(R.id.text_view_location_address);

        buttonChangePassword = view.findViewById(R.id.button_change_password);
        buttonChangePassword.setOnClickListener(this);

        buttonEditProfile = view.findViewById(R.id.button_edit_profile);
        buttonEditProfile.setOnClickListener(this);
    }

    private void setupViews() {

        Picasso.get()
                .load(Session.user.getAvatarUrl())
                .into(imageViewAvatar);

        textViewName.setText(Session.user.getFullName());

        textViewPhone.setText(MessageFormat.format("+92{0}",
                Session.user.getPhoneNumber()));

        textViewEmail.setText(Session.user.getEmail());

        String locationImageUrl = StringUtils.getMapsStaticImageUrl(getContext(),
                Session.user.getLocation());

        Picasso.get()
                .load(locationImageUrl)
                .into(imageViewLocation);

        String locationAddress = LocationUtils.getAddressFromLatLng(getContext(),
                Session.user.getLocation().latitude, Session.user.getLocation().longitude);

        textViewLocationAddress.setText(locationAddress);

        // total orders
        textViewTotalOrders.setText(MessageFormat.format("{0}\nOrders",
                Session.user.getOrders().size()));

        double totalSpent = 0;
        double totalPickup = 0;

        for (Transaction transaction : Session.user.getTransactions()) {

            totalSpent += transaction.getAmount();

            if (transaction.getType() == TransactionType.PICKUP_FEE) {

                totalPickup += transaction.getAmount();
            }
        }

        // total spent
        textViewTotalSpent.setText(MessageFormat
                .format("{0} PKR\nTotal Spent", totalSpent));

        // total pickup
        textViewTotalPickup.setText(MessageFormat
                .format("{0} PKR\nTrip Fare", totalPickup));

        // total transactions
        textViewTotalTransactions.setText(MessageFormat
                .format("{0}\nTransactions", Session.user.getTransactions().size()));

        int cancellations = 0;
        for (Order order : Session.user.getOrders()) {

            if (order.getStatus() == OrderStatus.CANCELLED) {

                cancellations++;
            }
        }

        // total cancellations
        textViewTotalCancelled.setText(MessageFormat
                .format("{0}\nCancellations", cancellations));

        // total addresses
        textViewTotalAddresses.setText(MessageFormat
                .format("{0}\nAddresses", Session.user.getLocations().size()));
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

            EditCustomerDialog dialog = new EditCustomerDialog();
            dialog.setCancelable(false);
            dialog.setListener(this);
            dialog.show(getChildFragmentManager(), "dialog_edit_merchant");

        } else if (view.getId() == R.id.image_view_avatar) {

            ImageUtils.showImage(getContext(), Session.user.getAvatarUrl());
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
    public void onCustomerEdited(String avatarUrl, String fullName,
                                 String phoneNumber, String email) {

        // update locally
        Session.user.setFullName(fullName);
        Session.user.setPhoneNumber(phoneNumber);
        Session.user.setEmail(email);

        if (avatarUrl != null) {

            Session.user.setAvatarUrl(avatarUrl);
            Picasso.get()
                    .invalidate(avatarUrl);
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