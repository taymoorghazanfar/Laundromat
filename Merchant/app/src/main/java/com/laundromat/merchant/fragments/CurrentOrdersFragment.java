package com.laundromat.merchant.fragments;

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
import com.laundromat.merchant.R;
import com.laundromat.merchant.activities.OrderActivity;
import com.laundromat.merchant.model.Merchant;
import com.laundromat.merchant.model.Transaction;
import com.laundromat.merchant.model.observers.IMerchantObserver;
import com.laundromat.merchant.model.order.Order;
import com.laundromat.merchant.model.order.OrderStatus;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.adapters.CurrentOrdersRecyclerAdapter;
import com.laundromat.merchant.ui.adapters.MenuItemRecyclerViewAdapter;
import com.laundromat.merchant.ui.decorators.SpacesItemDecoration;
import com.laundromat.merchant.ui.interfaces.ICurrentOrderClickListener;
import com.laundromat.merchant.utils.GsonUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CurrentOrdersFragment extends Fragment
        implements IMerchantObserver, ICurrentOrderClickListener {

    // Variables
    private Merchant merchant = Session.user;

    // Views
    private TextView textViewEmpty;
    private RelativeLayout layoutLoading;
    private RecyclerView recyclerViewCurrentOrders;
    private CurrentOrdersRecyclerAdapter adapter;

    private SearchView searchView;

    public CurrentOrdersFragment() {
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

        View view = inflater.inflate(R.layout.fragment_current_orders, container, false);

        initViews(view);

        return view;
    }

    private void initViews(View view) {

        textViewEmpty = view.findViewById(R.id.text_view_empty);
        textViewEmpty.setVisibility(View.GONE);

        layoutLoading = view.findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        recyclerViewCurrentOrders = view.findViewById(R.id.recycler_view_current_orders);
        recyclerViewCurrentOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        this.recyclerViewCurrentOrders.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

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

        adapter = new CurrentOrdersRecyclerAdapter(getCurrentOrders());
        adapter.setCurrentOrderClickListener(this);
        recyclerViewCurrentOrders.setAdapter(adapter);

        textViewEmpty.setVisibility(getCurrentOrders().size() > 0 ? View.GONE : View.VISIBLE);
    }

    private ArrayList<Order> getCurrentOrders() {

        ArrayList<Order> orders = new ArrayList<>();

        for (Order order : merchant.getLaundry().getOrders()) {

            if (order.getStatus() != OrderStatus.CANCELLED
                    && order.getStatus() != OrderStatus.DECLINED
                    && order.getStatus() != OrderStatus.COMPLETED
                    && order.getStatus() != OrderStatus.REQUESTED) {

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
    public void onCurrentOrderClick(Order order) {

        Intent intent = new Intent(getContext(), OrderActivity.class);
        intent.putExtra("order", GsonUtils.orderToGson(order));

        startActivity(intent);
    }

    @Override
    public void updateView(String task, String orderId, OrderStatus status) {

        if (getActivity() != null) {

            getActivity().runOnUiThread(() -> {

                adapter = new CurrentOrdersRecyclerAdapter(getCurrentOrders());
                adapter.setCurrentOrderClickListener(this);
                recyclerViewCurrentOrders.setAdapter(adapter);

                textViewEmpty.setVisibility(getCurrentOrders().size() > 0 ? View.GONE : View.VISIBLE);
            });
        }
    }
}