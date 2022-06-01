package com.laundromat.customer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.laundromat.customer.R;
import com.laundromat.customer.activities.OrderActivity;
import com.laundromat.customer.model.Customer;
import com.laundromat.customer.model.observers.IOrderObserver;
import com.laundromat.customer.model.order.Order;
import com.laundromat.customer.model.order.OrderStatus;
import com.laundromat.customer.prefs.Session;
import com.laundromat.customer.ui.adapters.PastOrdersRecyclerAdapter;
import com.laundromat.customer.ui.decorators.SpacesItemDecoration;
import com.laundromat.customer.ui.interfaces.IPastOrderClickListener;
import com.laundromat.customer.utils.GsonUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PastOrdersFragment extends Fragment
        implements IOrderObserver, IPastOrderClickListener {

    // Variables
    private Customer customer = Session.user;

    // Views
    private TextView textViewEmpty;
    private RelativeLayout layoutLoading;
    private RecyclerView recyclerViewPastOrders;
    private PastOrdersRecyclerAdapter adapter;

    private SearchView searchView;

    public PastOrdersFragment() {
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

        View view = inflater.inflate(R.layout.fragment_past_orders, container, false);

        initViews(view);

        return view;
    }

    private void initViews(View view) {

        textViewEmpty = view.findViewById(R.id.text_view_empty);
        textViewEmpty.setVisibility(View.GONE);

        layoutLoading = view.findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        recyclerViewPastOrders = view.findViewById(R.id.recycler_view_past_orders);
        recyclerViewPastOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        this.recyclerViewPastOrders.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

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

        adapter = new PastOrdersRecyclerAdapter(getPastOrders());
        adapter.setPastOrderClickListener(this);
        recyclerViewPastOrders.setAdapter(adapter);

        textViewEmpty.setVisibility(getPastOrders().size() > 0 ? View.GONE : View.VISIBLE);
    }

    private ArrayList<com.laundromat.customer.model.order.Order> getPastOrders() {

        ArrayList<com.laundromat.customer.model.order.Order> orders = new ArrayList<>();

        for (com.laundromat.customer.model.order.Order order : customer.getOrders()) {

            if (order.getStatus() == OrderStatus.COMPLETED
                    || order.getStatus() == OrderStatus.CANCELLED
                    || order.getStatus() == OrderStatus.DECLINED) {

                // deep copy
                Gson gson = new Gson();
                orders.add(gson.fromJson(gson.toJson(order),
                        com.laundromat.customer.model.order.Order.class));
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
    public void onPastOrderClick(com.laundromat.customer.model.order.Order order) {

        Intent intent = new Intent(getContext(), OrderActivity.class);
        intent.putExtra("order", GsonUtils.orderToGson(order));

        startActivity(intent);
    }

    @Override
    public void updateView(String task, String orderId, OrderStatus status) {

        if (getActivity() != null) {

            getActivity().runOnUiThread(() -> {

                adapter = new PastOrdersRecyclerAdapter(getPastOrders());
                adapter.setPastOrderClickListener(this);
                recyclerViewPastOrders.setAdapter(adapter);

                textViewEmpty.setVisibility(getPastOrders().size() > 0 ?
                        View.GONE : View.VISIBLE);
            });
        }
    }
}