package com.laundromat.admin.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.laundromat.admin.R;
import com.laundromat.admin.dialogs.EditCustomerDialog;
import com.laundromat.admin.dialogs.EditMerchantDialog;
import com.laundromat.admin.model.Customer;
import com.laundromat.admin.model.Merchant;
import com.laundromat.admin.model.Transaction;
import com.laundromat.admin.model.order.Order;
import com.laundromat.admin.model.order.OrderStatus;
import com.laundromat.admin.model.util.TransactionType;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.adapters.AddressesRecyclerViewAdapter;
import com.laundromat.admin.ui.decorators.SpacesItemDecoration;
import com.laundromat.admin.ui.interfaces.ICustomerEditedListener;
import com.laundromat.admin.ui.interfaces.IMerchantEditedListener;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.ImageUtils;
import com.laundromat.admin.utils.LocationUtils;
import com.laundromat.admin.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerProfileFragment extends Fragment
        implements View.OnClickListener, ICustomerEditedListener {

    // Constants
    private static final String CUSTOMER = "customer";
    private static final String INDEX = "index";

    // Variables
    private Customer customer;
    private int index;

    // Views
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

    private RecyclerView recyclerViewAddresses;
    private AddressesRecyclerViewAdapter adapter;

    private Button buttonEditProfile;

    public CustomerProfileFragment() {
        // Required empty public constructor
    }

    public static CustomerProfileFragment newInstance(String customerGson, int index) {
        CustomerProfileFragment fragment = new CustomerProfileFragment();
        Bundle args = new Bundle();
        args.putString(CUSTOMER, customerGson);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            String customerGson = getArguments().getString(CUSTOMER);
            index = getArguments().getInt(INDEX);

            if (customerGson != null) {

                customer = GsonUtils.gsonToCustomer(customerGson);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_customer_profile, container, false);

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

        // sort addresses
        Collections.sort(customer.getLocations(),
                (location1, location2)
                        -> location1.getName().compareTo(location2.getName()));
        // recycler view
        recyclerViewAddresses = view.findViewById(R.id.recycler_view_addresses);
        adapter = new AddressesRecyclerViewAdapter(getContext(), customer.getLocations());
        recyclerViewAddresses.setAdapter(adapter);
        recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(getContext()));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        this.recyclerViewAddresses.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

        buttonEditProfile = view.findViewById(R.id.button_edit_profile);
        buttonEditProfile.setOnClickListener(this);
    }

    private void setupViews() {

        Picasso.get()
                .load(customer.getAvatarUrl())
                .into(imageViewAvatar);

        textViewName.setText(customer.getFullName());

        textViewPhone.setText(MessageFormat.format("+92{0}",
                customer.getPhoneNumber()));

        textViewEmail.setText(customer.getEmail());

        String locationImageUrl = StringUtils.getMapsStaticImageUrl(getContext(),
                customer.getLocation());

        Picasso.get()
                .load(locationImageUrl)
                .into(imageViewLocation);

        String locationAddress = LocationUtils.getAddressFromLatLng(getContext(),
                customer.getLocation().latitude, customer.getLocation().longitude);

        textViewLocationAddress.setText(locationAddress);

        // total orders
        textViewTotalOrders.setText(MessageFormat.format("{0}\nOrders",
                Session.user.getOrders().size()));

        double totalSpent = 0;
        double totalPickup = 0;

        for (Transaction transaction : customer.getTransactions()) {

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
                .format("{0}\nTransactions", customer.getTransactions().size()));

        int cancellations = 0;
        for (Order order : customer.getOrders()) {

            if (order.getStatus() == OrderStatus.CANCELLED) {

                cancellations++;
            }
        }

        // total cancellations
        textViewTotalCancelled.setText(MessageFormat
                .format("{0}\nCancellations", cancellations));

        // total addresses
        textViewTotalAddresses.setText(MessageFormat
                .format("{0}\nAddresses", customer.getLocations().size()));
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_edit_profile) {

            Bundle bundle = new Bundle();
            bundle.putString("customer", GsonUtils.customerToGson(customer));

            EditCustomerDialog dialog = new EditCustomerDialog();
            dialog.setArguments(bundle);
            dialog.setCancelable(false);
            dialog.setListener(this);
            dialog.show(getChildFragmentManager(), "dialog_edit_customer");

        } else if (view.getId() == R.id.image_view_avatar) {

            ImageUtils.showImage(getContext(), customer.getAvatarUrl());
        }
    }

    @Override
    public void onCustomerEdited(String avatarUrl, String fullName, String email) {

        // update locally
        Session.user.getCustomers().get(index).setFullName(fullName);
        Session.user.getCustomers().get(index).setEmail(email);

        if (avatarUrl != null) {

            Session.user.getCustomers().get(index).setAvatarUrl(avatarUrl);
            Picasso.get()
                    .invalidate(avatarUrl);
        }

        customer = Session.user.getCustomers().get(index);

        setupViews();

        Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
    }
}