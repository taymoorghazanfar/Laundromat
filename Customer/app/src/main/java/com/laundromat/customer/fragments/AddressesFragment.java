package com.laundromat.customer.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.laundromat.customer.R;
import com.laundromat.customer.dialogs.AddAddressDialog;
import com.laundromat.customer.dialogs.DeleteAddressDialog;
import com.laundromat.customer.dialogs.EditAddressDialog;
import com.laundromat.customer.model.Customer;
import com.laundromat.customer.model.order.Order;
import com.laundromat.customer.model.order.OrderStatus;
import com.laundromat.customer.model.util.Location;
import com.laundromat.customer.prefs.Session;
import com.laundromat.customer.ui.adapters.AddressesRecyclerViewAdapter;
import com.laundromat.customer.ui.decorators.SpacesItemDecoration;
import com.laundromat.customer.ui.interfaces.IAddressClickListener;
import com.laundromat.customer.ui.interfaces.IAddressCreatedListener;
import com.laundromat.customer.ui.interfaces.IAddressDeletedListener;
import com.laundromat.customer.ui.interfaces.IAddressEditedListener;

import org.w3c.dom.Text;

import java.util.Collections;

public class AddressesFragment extends Fragment
        implements View.OnClickListener, IAddressClickListener,
        IAddressCreatedListener, IAddressEditedListener, IAddressDeletedListener {


    // Views
    private RecyclerView recyclerViewAddresses;
    private AddressesRecyclerViewAdapter adapter;
    private FloatingActionButton buttonAdd;
//    private TextView textViewEmpty;

    public AddressesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_addresses, container, false);

        initViews(view);

        return view;
    }

    private void initViews(View view) {

//        textViewEmpty = view.findViewById(R.id.text_view_empty);
//        textViewEmpty.setVisibility(View.GONE);

        buttonAdd = view.findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(this);

        initRecyclerView(view);
    }

    private void initRecyclerView(View view) {

        sortLocations();

        recyclerViewAddresses = view.findViewById(R.id.recycler_view_addresses);
        adapter = new AddressesRecyclerViewAdapter(getContext(), Session.user.getLocations());
        adapter.setAddressClickListener(this);
        recyclerViewAddresses.setAdapter(adapter);
        recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(getContext()));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        this.recyclerViewAddresses.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
    }

    private void sortLocations() {

        Collections.sort(Session.user.getLocations(),
                (location1, location2)
                        -> location1.getName().compareTo(location2.getName()));
    }

    private void showAddAddressDialog() {

        AddAddressDialog dialog = new AddAddressDialog();
        dialog.setCancelable(false);
        dialog.setListener(this);
        dialog.show(getChildFragmentManager(), "dialog_add_address");
    }

    private void showEditAddressDialog(int index) {

        if (Session.user.getOrders().size() > 0) {

            boolean orderPending = false;

            for (Order order : Session.user.getOrders()) {

                if (order.getStatus() != OrderStatus.COMPLETED
                        && order.getStatus() != OrderStatus.DECLINED
                        && order.getStatus() != OrderStatus.CANCELLED) {

                    orderPending = true;
                    break;
                }
            }

            if (orderPending) {

                Toast.makeText(getContext(), "Your have pending orders." +
                        " Clear all orders to perform this action", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable("address", Session.user.getLocations().get(index));
        bundle.putInt("address_index", index);

        EditAddressDialog dialog = new EditAddressDialog();
        dialog.setArguments(bundle);
        dialog.setCancelable(false);
        dialog.setListener(this);
        dialog.show(getChildFragmentManager(), "dialog_edit_address");
    }

    private void showDeleteAddressDialog(int index) {

        if (Session.user.getOrders().size() > 0) {

            boolean orderPending = false;

            for (Order order : Session.user.getOrders()) {

                if (order.getStatus() != OrderStatus.COMPLETED
                        && order.getStatus() != OrderStatus.DECLINED
                        && order.getStatus() != OrderStatus.CANCELLED) {

                    orderPending = true;
                    break;
                }
            }

            if (orderPending) {

                Toast.makeText(getContext(), "Your have pending orders." +
                        " Clear all orders to perform this action", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Bundle bundle = new Bundle();
        bundle.putInt("address_index", index);
        bundle.putParcelable("location", Session.user.getLocations().get(index).getLatLng());

        DeleteAddressDialog dialog = new DeleteAddressDialog();
        dialog.setArguments(bundle);
        dialog.setCancelable(false);
        dialog.setListener(this);
        dialog.show(getChildFragmentManager(), "dialog_delete_address");
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_add) {

            showAddAddressDialog();
        }
    }

    @Override
    public void onAddressEditClick(int index) {

        showEditAddressDialog(index);
    }

    @Override
    public void onAddressDeleteClick(int index) {

        showDeleteAddressDialog(index);
    }

    @Override
    public void onAddressCreated(Location location) {

        Session.user.getLocations().add(location);
        sortLocations();
        adapter.notifyDataSetChanged();
//        textViewEmpty.setVisibility(Session.user.getLocations().size() > 0 ?
//                View.VISIBLE : View.GONE);
        Toast.makeText(getContext(), "New location added", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAddressEdited(Location location, int index) {

        Session.user.getLocations().set(index, location);
        sortLocations();
        adapter.notifyDataSetChanged();
//        textViewEmpty.setVisibility(Session.user.getLocations().size() > 0 ?
//                View.VISIBLE : View.GONE);
        Toast.makeText(getContext(), "Location updated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAddressDeleted(int index) {

        Session.user.getLocations().remove(index);
        sortLocations();
        adapter.notifyDataSetChanged();
//        textViewEmpty.setVisibility(Session.user.getLocations().size() > 0 ?
//                View.VISIBLE : View.GONE);
        Toast.makeText(getContext(), "Location deleted", Toast.LENGTH_SHORT).show();
    }
}