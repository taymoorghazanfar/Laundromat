package com.laundromat.delivery.activities;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.laundromat.delivery.R;
import com.laundromat.delivery.model.Transaction;
import com.laundromat.delivery.model.observers.ITripObserver;
import com.laundromat.delivery.model.util.TripStatus;
import com.laundromat.delivery.prefs.Session;
import com.laundromat.delivery.services.LocationServiceUtils;
import com.laundromat.delivery.ui.adapters.TransactionsRecyclerAdapter;
import com.laundromat.delivery.ui.decorators.SpacesItemDecoration;
import com.laundromat.delivery.utils.DateUtils;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class TransactionsActivity extends AppCompatActivity
        implements View.OnClickListener, ITripObserver {

    // Views
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private RecyclerView recyclerViewTransactions;
    private TransactionsRecyclerAdapter adapter;
    private TextView textViewEmpty;
    private TextView textViewTotalEarned;
    private TextView textViewEarnedToday;
    private RelativeLayout layoutLoading;

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        if (Session.user != null) {

            Session.user.registerObserver(this);
        }

        initViews();
    }

    private void initViews() {

        textViewEmpty = findViewById(R.id.text_view_empty);

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        initToolbar();

        initDrawer();

        initDrawerMenu();

        initRecyclerView();

        initFooter();

        searchView = findViewById(R.id.search_view);
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

    private void initToolbar() {

        // setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initDrawer() {

        drawer = findViewById(R.id.drawer);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void initDrawerMenu() {

        TextView textViewName = findViewById(R.id.text_view_user_name);
        TextView textViewPhoneNumber = findViewById(R.id.text_view_phone_number);

        textViewName.setText(Session.user.getFullName());
        textViewPhoneNumber.setText(MessageFormat.format("+92{0}",
                Session.user.getPhoneNumber()));

        View optionHome = findViewById(R.id.option_dashboard);
        optionHome.setOnClickListener(this);

        View optionOrders = findViewById(R.id.option_trips);
        optionOrders.setOnClickListener(this);

        View optionTransactions = findViewById(R.id.option_transactions);
        optionTransactions.setOnClickListener(this);

        View optionLogout = findViewById(R.id.option_logout);
        optionLogout.setOnClickListener(this);

        View optionProfile = findViewById(R.id.option_profile);
        optionProfile.setOnClickListener(this);
    }

    private void initRecyclerView() {

        recyclerViewTransactions = findViewById(R.id.recycler_view_transactions);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        this.recyclerViewTransactions.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

        adapter = new TransactionsRecyclerAdapter(getTransactions());
        recyclerViewTransactions.setAdapter(adapter);

        textViewEmpty.setVisibility(getTransactions().size()
                > 0 ? View.GONE : View.VISIBLE);
    }

    private void initFooter() {

        textViewTotalEarned = findViewById(R.id.text_view_total_earned);
        textViewEarnedToday = findViewById(R.id.text_view_earned_today);

        double totalEarned = 0;
        double earnedToday = 0;

        for (Transaction transaction : getTransactions()) {

            totalEarned += transaction.getAmount();

            if (DateUtils.isSameDay(transaction.getDateCreated())) {

                earnedToday += transaction.getAmount();
            }
        }

        textViewTotalEarned.setText(MessageFormat.format("PKR {0}", totalEarned));
        textViewEarnedToday.setText(MessageFormat.format("PKR {0}", earnedToday));
    }

    private ArrayList<Transaction> getTransactions() {

        ArrayList<Transaction> transactions = new ArrayList<>();

        for (Transaction transaction : Session.user.getTransactions()) {

            // deep copy
            Gson gson = new Gson();
            transactions.add(gson.fromJson(gson.toJson(transaction),
                    Transaction.class));
        }

        // sort the list by most recent order
        Collections.sort(transactions, new Comparator<Transaction>() {
            final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            @Override
            public int compare(Transaction transaction1, Transaction transaction2) {
                try {
                    return dateFormat.parse(transaction2.getDateCreated())
                            .compareTo(dateFormat.parse(transaction1.getDateCreated()));

                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });

        return transactions;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(TransactionsActivity.this, DashboardActivity.class));
        finish();
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.option_dashboard) {

            startActivity(new Intent(TransactionsActivity.this, DashboardActivity.class));
            finish();

        } else if (view.getId() == R.id.option_trips) {

            startActivity(new Intent(TransactionsActivity.this, TripsActivity.class));
            finish();

        } else if (view.getId() == R.id.option_transactions) {

            if (drawer.isDrawerOpen(GravityCompat.START)) {

                drawer.closeDrawer(GravityCompat.START);
            }

        } else if (view.getId() == R.id.option_profile) {

            startActivity(new Intent(TransactionsActivity.this, ProfileActivity.class));
            finish();

        } else if (view.getId() == R.id.option_logout) {

            AlertDialog.Builder alert;
            alert = new AlertDialog.Builder(this);
            alert.setTitle("Logout ?");

            alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                dialog.dismiss();

                if (drawer.isDrawerOpen(GravityCompat.START)) {

                    drawer.closeDrawer(GravityCompat.START);
                }

                Map<String, Object> data = new HashMap<>();
                data.put("driver_id", Session.user.getId());
                data.put("status", false);

                showLoadingAnimation();

                FirebaseFunctions
                        .getInstance()
                        .getHttpsCallable("delivery_boy-setAvailability")
                        .call(data)
                        .addOnSuccessListener(httpsCallableResult -> {

                            hideLoadingAnimation();

                            LocationServiceUtils.stopLocationService(TransactionsActivity.this);
                            Session.destroy(TransactionsActivity.this);

                            startActivity(new Intent(TransactionsActivity.this, LoginActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {

                            hideLoadingAnimation();

                            Toast.makeText(TransactionsActivity.this,
                                    e.getMessage(), Toast.LENGTH_SHORT).show();

                            Log.d("driver", "acceptTripRequest: " + e.getMessage());
                        });

            });

            alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

            alert.show();
        }
    }

    @Override
    public void updateView(String task, String tripId, TripStatus status) {

        runOnUiThread(() -> {

            adapter = new TransactionsRecyclerAdapter(getTransactions());
            recyclerViewTransactions.setAdapter(adapter);

            textViewEmpty.setVisibility(getTransactions().size()
                    > 0 ? View.GONE : View.VISIBLE);
        });
    }

    public void showLoadingAnimation() {

        layoutLoading.setVisibility(View.VISIBLE);
        layoutLoading.bringToFront();
        layoutLoading.animate().translationY(0);
    }

    public void hideLoadingAnimation() {

        layoutLoading.setVisibility(View.GONE);
        layoutLoading.animate().translationY(layoutLoading.getHeight());
    }
}