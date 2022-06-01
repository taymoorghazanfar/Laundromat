package com.laundromat.merchant.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.laundromat.merchant.R;
import com.laundromat.merchant.activities.OrderRequestActivity;
import com.laundromat.merchant.model.Merchant;
import com.laundromat.merchant.model.observers.IMerchantObserver;
import com.laundromat.merchant.model.order.Order;
import com.laundromat.merchant.model.order.OrderStatus;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.adapters.CurrentOrdersRecyclerAdapter;
import com.laundromat.merchant.ui.adapters.RequestedOrdersRecyclerAdapter;
import com.laundromat.merchant.ui.decorators.SpacesItemDecoration;
import com.laundromat.merchant.ui.interfaces.IRequestedOrderClickListener;
import com.laundromat.merchant.utils.GsonUtils;
import com.laundromat.merchant.utils.ParseUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RequestedOrdersFragment extends Fragment
        implements IRequestedOrderClickListener, IMerchantObserver {

    // Variables
    private Merchant merchant = Session.user;

    // Views
    private TextView textViewEmpty;
    private RelativeLayout layoutLoading;
    private RecyclerView recyclerViewRequestedOrders;
    private RequestedOrdersRecyclerAdapter adapter;

    private SearchView searchView;

    public RequestedOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.user.registerObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_requested_orders, container, false);

        initViews(view);

        return view;
    }

    private void initViews(View view) {

        textViewEmpty = view.findViewById(R.id.text_view_empty);
        textViewEmpty.setVisibility(View.GONE);

        layoutLoading = view.findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        recyclerViewRequestedOrders = view.findViewById(R.id.recycler_view_requested_orders);
        recyclerViewRequestedOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        this.recyclerViewRequestedOrders.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

        initRecyclerView();

        searchView = view.findViewById(R.id.search_view);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void initRecyclerView() {

        adapter = new RequestedOrdersRecyclerAdapter(getRequestedOrders());
        adapter.setRequestedOrderClickListener(this);
        recyclerViewRequestedOrders.setAdapter(adapter);

        textViewEmpty.setVisibility(getRequestedOrders().size() > 0 ? View.GONE : View.VISIBLE);
    }

    private ArrayList<Order> getRequestedOrders() {

        ArrayList<Order> orders = new ArrayList<>();

        for (Order order : merchant.getLaundry().getOrders()) {

            if (order.getStatus() == OrderStatus.REQUESTED) {

                // deep copy
                Gson gson = new Gson();
                orders.add(gson.fromJson(gson.toJson(order), Order.class));
            }
        }

        // sort the list by most recent order
        Collections.sort(orders, new Comparator<Order>() {
            final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            @Override
            public int compare(Order order1, Order order2) {
                try {
                    return dateFormat.parse(order2.getDateCreated())
                            .compareTo(dateFormat.parse(order1.getDateCreated()));

                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });

        return orders;
    }

    private void showLoadingAnimation() {

        layoutLoading.setVisibility(View.VISIBLE);
        layoutLoading.bringToFront();
        layoutLoading.animate().translationY(0);
    }

    private void hideLoadingAnimation() {

        layoutLoading.setVisibility(View.GONE);
        layoutLoading.animate().translationY(layoutLoading.getHeight());
    }

    @Override
    public void onRequestedOrderClick(Order order) {

        Intent intent = new Intent(getContext(), OrderRequestActivity.class);
        intent.putExtra("order", GsonUtils.orderToGson(order));

        startActivity(intent);
    }

    @Override
    public void updateView(String task, String orderId, OrderStatus status) {

        if (getActivity() != null) {

            getActivity().runOnUiThread(() -> {

                adapter = new RequestedOrdersRecyclerAdapter(getRequestedOrders());
                adapter.setRequestedOrderClickListener(this);
                recyclerViewRequestedOrders.setAdapter(adapter);

                textViewEmpty.setVisibility(getRequestedOrders().size() > 0 ? View.GONE : View.VISIBLE);
            });
        }
    }
}