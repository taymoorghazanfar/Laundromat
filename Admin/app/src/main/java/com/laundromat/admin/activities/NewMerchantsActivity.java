package com.laundromat.admin.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.admin.R;
import com.laundromat.admin.model.Merchant;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.adapters.MerchantsRecyclerAdapter;
import com.laundromat.admin.ui.decorators.SpacesItemDecoration;
import com.laundromat.admin.ui.interfaces.IMerchantClickListener;
import com.laundromat.admin.utils.GsonUtils;
import com.laundromat.admin.utils.ParseUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NewMerchantsActivity extends AppCompatActivity
        implements View.OnClickListener, IMerchantClickListener, SwipeRefreshLayout.OnRefreshListener {

    // Constants
    private static final int REQUEST_CODE_MERCHANT = 111;

    // Variables
    private List<Merchant> merchants;
    private int selectedIndex;

    // Views
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout layoutLoading;
    private DrawerLayout drawer;
    private TextView textViewEmpty;
    private RecyclerView recyclerViewMerchants;
    private MerchantsRecyclerAdapter adapter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_merchants);

        initViews();
    }

    private void initViews() {

        // setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        initDrawer();

        initDrawerMenu();

        textViewEmpty = findViewById(R.id.text_view_empty);
        textViewEmpty.setVisibility(View.GONE);

        recyclerViewMerchants = findViewById(R.id.recycler_view_merchants);
        recyclerViewMerchants.setLayoutManager(new LinearLayoutManager(this));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        recyclerViewMerchants.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

        initRecyclerView();

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

    private void initDrawer() {

        drawer = findViewById(R.id.drawer_layout);

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

        View optionDashboard = findViewById(R.id.option_dashboard);
        optionDashboard.setOnClickListener(this);

        View optionNewMerchants = findViewById(R.id.option_new_merchants);
        optionNewMerchants.setOnClickListener(this);

        View optionNewDrivers = findViewById(R.id.option_new_drivers);
        optionNewDrivers.setOnClickListener(this);

        View optionAllMerchants = findViewById(R.id.option_merchants);
        optionAllMerchants.setOnClickListener(this);

        View optionAllDrivers = findViewById(R.id.option_drivers);
        optionAllDrivers.setOnClickListener(this);

        View optionCustomers = findViewById(R.id.option_customers);
        optionCustomers.setOnClickListener(this);

        View optionOrders = findViewById(R.id.option_orders);
        optionOrders.setOnClickListener(this);

        View optionTrips = findViewById(R.id.option_trips);
        optionTrips.setOnClickListener(this);

        View optionServiceTypes = findViewById(R.id.option_services);
        optionServiceTypes.setOnClickListener(this);

        View optionSettings = findViewById(R.id.option_config);
        optionSettings.setOnClickListener(this);
    }

    private void initRecyclerView() {

        Collections.sort(Session.user.getNewMerchants(), new Comparator<Merchant>() {
            final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            @Override
            public int compare(Merchant m1, Merchant m2) {
                try {
                    return dateFormat.parse(m2.getDateCreated())
                            .compareTo(dateFormat.parse(m1.getDateCreated()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });

        merchants = Session.user.getNewMerchants();

        adapter = new MerchantsRecyclerAdapter(merchants);
        adapter.setMerchantClickListener(this);
        recyclerViewMerchants.setAdapter(adapter);

        textViewEmpty.setVisibility(merchants.size() > 0 ? View.GONE : View.VISIBLE);
    }

    private void getNewMerchants() {

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("admin-getNewRegistrationRequests")
                .call("merchant")
                .addOnSuccessListener(httpsCallableResult1 -> {

                    hideLoadingAnimation();

                    Session.user.setNewMerchants(ParseUtils
                            .parseMerchants(httpsCallableResult1.getData()));

                    initRecyclerView();

                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Toast.makeText(NewMerchantsActivity.this, e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.d("merchants", "onFailure: " + e.getMessage());
                });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(NewMerchantsActivity.this,
                DashboardActivity.class));
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.option_dashboard:
                startActivity(new Intent(NewMerchantsActivity.this,
                        DashboardActivity.class));
                finish();
                break;

            case R.id.option_new_merchants:
                if (drawer.isDrawerOpen(GravityCompat.START)) {

                    drawer.closeDrawer(GravityCompat.START);
                }

            case R.id.option_new_drivers:
                startActivity(new Intent(NewMerchantsActivity.this, NewDriversActivity.class));
                finish();
                break;

            case R.id.option_merchants:
                startActivity(new Intent(NewMerchantsActivity.this, MerchantsActivity.class));
                finish();
                break;

            case R.id.option_drivers:
                startActivity(new Intent(NewMerchantsActivity.this, DriversActivity.class));
                finish();
                break;

            case R.id.option_customers:
                startActivity(new Intent(NewMerchantsActivity.this, CustomersActivity.class));
                finish();
                break;

            case R.id.option_orders:
                startActivity(new Intent(NewMerchantsActivity.this, OrdersActivity.class));
                finish();
                break;

            case R.id.option_trips:
                startActivity(new Intent(NewMerchantsActivity.this, TripsActivity.class));
                finish();
                break;

            case R.id.option_services:
                startActivity(new Intent(NewMerchantsActivity.this, ServiceTypesActivity.class));
                finish();
                break;

            case R.id.option_config:
                startActivity(new Intent(NewMerchantsActivity.this, SettingsActivity.class));
                finish();
                break;

            case R.id.option_logout:
                AlertDialog.Builder alert;
                alert = new AlertDialog.Builder(this);
                alert.setTitle("Logout ?");

                alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                    dialog.dismiss();
                    Session.destroy(NewMerchantsActivity.this);

                    startActivity(new Intent(NewMerchantsActivity.this, LoginActivity.class));
                    finish();
                });

                alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

                alert.show();
        }
    }

    @Override
    public void onMerchantClick(int index, Merchant merchant) {

        selectedIndex = index;

        Intent intent = new Intent(
                NewMerchantsActivity.this,
                MerchantRequestActivity.class);

        intent.putExtra("merchant", GsonUtils.merchantToGson(merchant));
        intent.putExtra("laundry", GsonUtils.laundryToGson(merchant.getLaundry()));

        startActivityForResult(intent, REQUEST_CODE_MERCHANT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MERCHANT && resultCode == RESULT_OK) {

            if (data != null) {

                String task = data.getStringExtra("task");

                if (task.equals("accept")) {

                    // save new merchant locally
                    Merchant merchant = Session.user.getNewMerchants().get(selectedIndex);
                    Session.user.getMerchants().add(merchant);

                    Toast.makeText(this, "Merchant registration accepted",
                            Toast.LENGTH_SHORT).show();

                } else if (task.equals("decline")) {

                    Toast.makeText(this,
                            "Merchant registration declined", Toast.LENGTH_SHORT).show();
                }

                Session.user.getNewMerchants().remove(selectedIndex);
                initRecyclerView();
            }
        }
    }

    @Override
    public void onRefresh() {

        getNewMerchants();
        swipeRefreshLayout.setRefreshing(false);
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
}