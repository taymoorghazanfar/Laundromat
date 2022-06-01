package com.laundromat.admin.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.admin.R;
import com.laundromat.admin.dialogs.AddServiceTypeDialog;
import com.laundromat.admin.model.washable.ServiceType;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.adapters.ServicesRecyclerAdapter;
import com.laundromat.admin.ui.decorators.SpacesItemDecoration;
import com.laundromat.admin.ui.interfaces.IServiceTypeClickListener;
import com.laundromat.admin.ui.interfaces.IServiceTypeCreatedListener;
import com.laundromat.admin.ui.views.MovableFloatingActionButton;
import com.laundromat.admin.utils.GsonUtils;

import java.util.Collections;
import java.util.List;

public class ServiceTypesActivity extends AppCompatActivity
        implements View.OnClickListener, IServiceTypeClickListener, IServiceTypeCreatedListener {

    // Views
    private RelativeLayout layoutLoading;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private TextView textViewEmpty;
    private RecyclerView recyclerViewService;
    private ServicesRecyclerAdapter adapter;
    private SearchView searchView;
    private MovableFloatingActionButton buttonAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_types);

        initViews();
    }

    private void initViews() {

        // setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        initDrawer();

        initDrawerMenu();

        textViewEmpty = findViewById(R.id.text_view_empty);
        textViewEmpty.setVisibility(View.GONE);

        this.buttonAdd = findViewById(R.id.button_add);
        this.buttonAdd.setOnClickListener(this);

        recyclerViewService = findViewById(R.id.recycler_view_services);
        recyclerViewService.setLayoutManager(new LinearLayoutManager(this));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        recyclerViewService.addItemDecoration(new SpacesItemDecoration(spacingInPixels));

        setupRecyclerView();

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

    private void setupRecyclerView() {

        List<ServiceType> serviceTypes = Session.user.getServiceTypes();

        Collections.sort(serviceTypes,
                (serv1, serv2)
                        -> serv1.getName().compareTo(serv2.getName()));

        adapter = new ServicesRecyclerAdapter(serviceTypes);
        adapter.setServiceTypeClickListener(this);
        recyclerViewService.setAdapter(adapter);

        textViewEmpty.setVisibility(serviceTypes.size() > 0 ? View.GONE : View.VISIBLE);
    }

    private void showAddServiceDialog() {

        AddServiceTypeDialog dialog = new AddServiceTypeDialog();
        dialog.setCancelable(false);
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "dialog_add_service_type");
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ServiceTypesActivity.this,
                DashboardActivity.class));
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.option_dashboard:
                startActivity(new Intent(ServiceTypesActivity.this,
                        DashboardActivity.class));
                finish();
                break;

            case R.id.option_new_merchants:
                startActivity(new Intent(ServiceTypesActivity.this, NewMerchantsActivity.class));
                finish();

            case R.id.option_new_drivers:
                startActivity(new Intent(ServiceTypesActivity.this, NewDriversActivity.class));
                finish();
                break;

            case R.id.option_merchants:
                startActivity(new Intent(ServiceTypesActivity.this, MerchantsActivity.class));
                finish();
                break;

            case R.id.option_drivers:
                startActivity(new Intent(ServiceTypesActivity.this, DriversActivity.class));
                finish();
                break;

            case R.id.option_customers:
                startActivity(new Intent(ServiceTypesActivity.this, CustomersActivity.class));
                finish();
                break;

            case R.id.option_orders:
                startActivity(new Intent(ServiceTypesActivity.this, OrdersActivity.class));
                finish();
                break;

            case R.id.option_trips:
                startActivity(new Intent(ServiceTypesActivity.this, TripsActivity.class));
                finish();
                break;

            case R.id.option_services:
                if (drawer.isDrawerOpen(GravityCompat.START)) {

                    drawer.closeDrawer(GravityCompat.START);
                }
                break;

            case R.id.option_config:
                startActivity(new Intent(ServiceTypesActivity.this, SettingsActivity.class));
                finish();
                break;

            case R.id.button_add:
                showAddServiceDialog();
                break;

            case R.id.option_logout:
                AlertDialog.Builder alert;
                alert = new AlertDialog.Builder(this);
                alert.setTitle("Logout ?");

                alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                    dialog.dismiss();
                    Session.destroy(ServiceTypesActivity.this);

                    startActivity(new Intent(ServiceTypesActivity.this, LoginActivity.class));
                    finish();
                });

                alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

                alert.show();
        }
    }

    @Override
    public void onServiceTypeCreated(ServiceType serviceType) {

        // save locally
        Session.user.getServiceTypes().add(serviceType);

        // update view
        setupRecyclerView();

        Toast.makeText(this, "New service type added", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceClick(ServiceType serviceType) {

        Intent intent = new Intent(this, ServiceTypeActivity.class);
        intent.putExtra("service", GsonUtils.serviceToGson(serviceType));

        startActivity(intent);
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