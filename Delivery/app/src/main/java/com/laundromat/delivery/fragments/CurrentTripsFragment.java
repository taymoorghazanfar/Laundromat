package com.laundromat.delivery.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.laundromat.delivery.R;
import com.laundromat.delivery.activities.TripRequestActivity;
import com.laundromat.delivery.model.DeliveryBoy;
import com.laundromat.delivery.model.Trip;
import com.laundromat.delivery.model.observers.ITripObserver;
import com.laundromat.delivery.model.util.TripStatus;
import com.laundromat.delivery.prefs.Session;
import com.laundromat.delivery.ui.adapters.TripsRecyclerAdapter;
import com.laundromat.delivery.ui.decorators.SpacesItemDecoration;
import com.laundromat.delivery.ui.interfaces.ITripClickListener;
import com.laundromat.delivery.utils.GsonUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CurrentTripsFragment extends Fragment
        implements ITripClickListener, ITripObserver {

    // Variables
    private DeliveryBoy deliveryBoy = Session.user;

    // Views
    private TextView textViewEmpty;
    private RelativeLayout layoutLoading;
    private RecyclerView recyclerViewCurrentTrips;
    private TripsRecyclerAdapter adapter;

    private SearchView searchView;

    public CurrentTripsFragment() {
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

        View view = inflater.inflate(R.layout.fragment_current_trips, container, false);

        initViews(view);

        return view;
    }

    private void initViews(View view) {

        textViewEmpty = view.findViewById(R.id.text_view_empty);
        textViewEmpty.setVisibility(View.GONE);

        layoutLoading = view.findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        recyclerViewCurrentTrips = view.findViewById(R.id.recycler_view_current_trips);
        recyclerViewCurrentTrips.setLayoutManager(new LinearLayoutManager(getContext()));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        this.recyclerViewCurrentTrips.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

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

        adapter = new TripsRecyclerAdapter(getContext(), getCurrentTrips());
        adapter.setTripClickListener(this);
        recyclerViewCurrentTrips.setAdapter(adapter);

        textViewEmpty.setVisibility(getCurrentTrips().size() > 0 ? View.GONE : View.VISIBLE);
    }

    private ArrayList<Trip> getCurrentTrips() {

        ArrayList<Trip> trips = new ArrayList<>();

        for (Trip trip : deliveryBoy.getTrips()) {

            if (trip.getStatus() != TripStatus.REQUESTED
                    && trip.getStatus() != TripStatus.DECLINED
                    && trip.getStatus() != TripStatus.COMPLETED
                    && trip.getStatus() != TripStatus.CANCELLED) {

                // deep copy
                Gson gson = new Gson();
                trips.add(gson.fromJson(gson.toJson(trip), Trip.class));
            }
        }

        // sort the list by most recent trips
        Collections.sort(trips, new Comparator<Trip>() {
            final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            @Override
            public int compare(Trip trip1, Trip trip2) {
                try {
                    return dateFormat.parse(trip2.getDateCreated())
                            .compareTo(dateFormat.parse(trip1.getDateCreated()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });

        return trips;
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
    public void onTripClick(Trip trip) {

        Intent intent = new Intent(getContext(), TripRequestActivity.class);
        intent.putExtra("trip", GsonUtils.tripToGson(trip));

        startActivity(intent);
    }

    @Override
    public void updateView(String task, String tripId, TripStatus status) {

        if (getActivity() != null) {

            getActivity().runOnUiThread(() -> {

                adapter = new TripsRecyclerAdapter(getContext(), getCurrentTrips());
                adapter.setTripClickListener(this);
                recyclerViewCurrentTrips.setAdapter(adapter);

                textViewEmpty.setVisibility(getCurrentTrips().size() > 0 ? View.GONE : View.VISIBLE);
            });
        }
    }
}