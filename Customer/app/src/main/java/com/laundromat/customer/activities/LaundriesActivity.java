package com.laundromat.customer.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.customer.R;
import com.laundromat.customer.model.Customer;
import com.laundromat.customer.model.Laundry;
import com.laundromat.customer.model.util.LaundryRecyclerItem;
import com.laundromat.customer.model.util.Location;
import com.laundromat.customer.model.util.UserLocation;
import com.laundromat.customer.prefs.CartPrefs;
import com.laundromat.customer.prefs.Session;
import com.laundromat.customer.ui.adapters.AllLaundriesRecyclerAdapter;
import com.laundromat.customer.ui.adapters.HomeBasedLaundriesRecyclerAdapter;
import com.laundromat.customer.ui.decorators.HorizontalDecoration;
import com.laundromat.customer.ui.decorators.SpacesItemDecoration;
import com.laundromat.customer.ui.interfaces.IAllLaundryClickListener;
import com.laundromat.customer.ui.interfaces.IAllLaundryFilterListener;
import com.laundromat.customer.ui.interfaces.IHomeBasedLaundryClickListener;
import com.laundromat.customer.ui.interfaces.IHomeBasedLaundryFilterListener;
import com.laundromat.customer.utils.DateUtils;
import com.laundromat.customer.utils.GsonUtils;
import com.laundromat.customer.utils.LocationUtils;
import com.laundromat.customer.utils.ParseUtils;
import com.laundromat.customer.utils.TimeUtils;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaundriesActivity extends AppCompatActivity
        implements View.OnClickListener, RadioGroup.OnCheckedChangeListener,
        IHomeBasedLaundryClickListener, IAllLaundryClickListener,
        IHomeBasedLaundryFilterListener, IAllLaundryFilterListener, SwipeRefreshLayout.OnRefreshListener {

    // Constants
    private static final String TAG = "laundriesActivity";
    private static final int REQUEST_CODE_CART = 111;
    private static final int REQUEST_CODE_MENU = 222;

    // Variables
    private final Customer customer = Session.user;

    private List<Laundry> nearbyLaundries;
    private List<LaundryRecyclerItem> allLaundries;
    private List<LaundryRecyclerItem> homeBasedLaundries;

    // Views
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton buttonSelectLocation;
    private LinearLayout layoutHomeBasedLaundries;
    private LinearLayout layoutAllLaundries;
    private TextView textViewCartItemCount;
    private TextView textViewCurrentLocation;
    private DrawerLayout drawer;
    private LinearLayout layoutLocations;
    private SearchView searchViewLaundries;
    private RecyclerView recyclerViewHomeBasedLaundries;
    private RecyclerView recyclerViewAllLaundries;
    private HomeBasedLaundriesRecyclerAdapter homeBasedLaundriesAdapter;
    private AllLaundriesRecyclerAdapter allLaundriesAdapter;
    private RelativeLayout layoutLoading;
    private RelativeLayout layoutEmpty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laundries);

        initViews();

        getNearbyLaundries();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // save cart to session
        CartPrefs.set(LaundriesActivity.this, customer.getCart());

    }

    private void initViews() {

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        layoutEmpty = findViewById(R.id.layout_empty);
        layoutEmpty.setVisibility(View.GONE);

        layoutAllLaundries = findViewById(R.id.layout_all_laundries);
        layoutHomeBasedLaundries = findViewById(R.id.layout_home_based_laundries);

        initToolbar();

        initDrawer();

        initDrawerMenu();

        initLocationsList();

        initSearchView();

        initRecyclerViews();

        updateCartItemsCount();
    }

    private void initToolbar() {

        // setup toolbar
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        // setup toolbar views
        textViewCurrentLocation = toolbar.findViewById(R.id.text_view_selected_location);

        RelativeLayout layoutSelectLocation = toolbar.findViewById(R.id.layout_select_location);
        layoutSelectLocation.setOnClickListener(this);

        ImageView imageViewCart = toolbar.findViewById(R.id.image_view_cart);
        imageViewCart.setOnClickListener(this);

        textViewCartItemCount = findViewById(R.id.text_view_item_count);

        buttonSelectLocation = findViewById(R.id.button_down);
        buttonSelectLocation.setOnClickListener(this);
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

        TextView textViewName = findViewById(R.id.text_view_user_name);
        TextView textViewPhoneNumber = findViewById(R.id.text_view_phone_number);

        textViewName.setText(Session.user.getFullName());
        textViewPhoneNumber.setText(MessageFormat.format("+92{0}",
                Session.user.getPhoneNumber()));

        View optionHome = findViewById(R.id.option_home);
        optionHome.setOnClickListener(this);

        View optionOrders = findViewById(R.id.option_orders);
        optionOrders.setOnClickListener(this);

        View optionTransactions = findViewById(R.id.option_transactions);
        optionTransactions.setOnClickListener(this);

        View optionLogout = findViewById(R.id.option_logout);
        optionLogout.setOnClickListener(this);

        View optionProfile = findViewById(R.id.option_profile);
        optionProfile.setOnClickListener(this);
    }


    private void initLocationsList() {

        layoutLocations = findViewById(R.id.layout_locations);

        // get data for radio buttons
        List<UserLocation> locations = new ArrayList<>();

        for (Location location : customer.getLocations()) {

            UserLocation data = new UserLocation();
            data.setLocationName(location.getName());

            if (location.getLatLng().latitude == customer.getLocation().latitude
                    && location.getLatLng().longitude == customer.getLocation().longitude) {

                data.setSelected(true);

                // set current location name on text view
                textViewCurrentLocation.setText(location.getName());

            } else {

                data.setSelected(false);
            }

            locations.add(data);
        }

        // create radio buttons programmatically
        RadioGroup radioGroupLocations = findViewById(R.id.radio_group_locations);

        for (int x = 0; x < locations.size(); x++) {

            UserLocation location = locations.get(x);

            RadioButton radioButton = new RadioButton(LaundriesActivity.this);

            radioButton.setId(View.generateViewId());
            radioButton.setText(location.getLocationName());
            radioButton.setTextColor(Color.BLACK);
            radioButton.setTextSize(18f);
            radioButton.setChecked(location.isSelected());

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) radioGroupLocations.getLayoutParams();

            if (x == 0) {

                params.setMargins(10, 10, 10, 10);

            } else {

                params.setMargins(10, 0, 10, 10);
            }

            radioButton.setLayoutParams(params);

            radioGroupLocations.addView(radioButton);
        }

        radioGroupLocations.setOnCheckedChangeListener(this);
    }

    private void initSearchView() {

        searchViewLaundries = findViewById(R.id.search_view_laundry);
        searchViewLaundries.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        searchViewLaundries.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (newText.length() == 0) {

                    layoutHomeBasedLaundries.setVisibility(View.VISIBLE);
                    layoutAllLaundries.setVisibility(View.VISIBLE);
                }

                homeBasedLaundriesAdapter.getFilter().filter(newText);
                allLaundriesAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void initRecyclerViews() {

        nearbyLaundries = new ArrayList<>();
        allLaundries = new ArrayList<>();
        homeBasedLaundries = new ArrayList<>();

        // init home based laundries adapter
        recyclerViewHomeBasedLaundries = findViewById(R.id.recycler_view_home_based_laundries);
        homeBasedLaundriesAdapter = new HomeBasedLaundriesRecyclerAdapter(homeBasedLaundries);
        homeBasedLaundriesAdapter.setLaundryClickListener(this);
        homeBasedLaundriesAdapter.setHomeBasedLaundryFilterListener(this);
        recyclerViewHomeBasedLaundries
                .setLayoutManager(new LinearLayoutManager(
                        this, RecyclerView.HORIZONTAL, false));
        recyclerViewHomeBasedLaundries.setAdapter(homeBasedLaundriesAdapter);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        this.recyclerViewHomeBasedLaundries.addItemDecoration(new HorizontalDecoration(spacingInPixels));

        // init all laundries adapter
        recyclerViewAllLaundries = findViewById(R.id.recycler_view_all_laundries);
        allLaundriesAdapter = new AllLaundriesRecyclerAdapter(allLaundries);
        allLaundriesAdapter.setLaundryClickListener(this);
        allLaundriesAdapter.setLaundryFilterListener(this);
        recyclerViewAllLaundries
                .setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAllLaundries.setAdapter(allLaundriesAdapter);
        this.recyclerViewAllLaundries.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
    }

    private void updateCartItemsCount() {

        if (customer.getCart().isEmpty()) {

            textViewCartItemCount.setVisibility(View.GONE);

        } else {

            textViewCartItemCount.setVisibility(View.VISIBLE);
            textViewCartItemCount.setText(String.valueOf(customer.getCart().getTotalSaleItems()));
        }
    }

    private void toggleLocationsLayout() {

        ViewGroup parent = findViewById(R.id.layout_content);

        Transition transition = new Slide(Gravity.TOP);
        transition.setDuration(500);
        transition.addTarget(R.id.layout_locations);

        TransitionManager.beginDelayedTransition(parent, transition);
        layoutLocations.setVisibility(layoutLocations.getVisibility()
                == View.GONE ? View.VISIBLE : View.GONE);

        layoutLocations.bringToFront();

//        if (layoutLocations.getVisibility() == View.GONE) {
//
//            layoutLocations.setVisibility(View.VISIBLE);
//            layoutLocations.bringToFront();
//
//        } else {
//
//            layoutLocations.setVisibility(View.GONE);
//        }
    }

    private void setCustomerLocation(int locationIndex) {

        String selectedLocationName = customer.getLocations().get(locationIndex).getName();
        LatLng selectedLocationAddress = customer.getLocations().get(locationIndex).getLatLng();

        textViewCurrentLocation.setText(selectedLocationName);

        // change customer current location
        customer.setLocation(selectedLocationAddress);

        // set query data
        Map<String, Object> data = new HashMap<>();
        data.put("customer_id", Session.user.getId());
        data.put("latitude", selectedLocationAddress.latitude);
        data.put("longitude", selectedLocationAddress.longitude);

        showLoadingAnimation();

        // update customer location in database
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("customer-updateLocation")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    hideLoadingAnimation();

                    // fetch laundries nearby to this location
                    getNearbyLaundries();

                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Log.d(TAG, "setCustomerLocation: " + e.getMessage());
                });
    }

    private void getNearbyLaundries() {

        Map<String, Object> data = new HashMap<>();
        data.put("latitude", customer.getLocation().latitude);
        data.put("longitude", customer.getLocation().longitude);

        showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("laundry-getNearbyLaundries")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    hideLoadingAnimation();

                    if (httpsCallableResult.getData() != null) {

                        nearbyLaundries.clear();
                        allLaundries.clear();
                        homeBasedLaundries.clear();

                        nearbyLaundries =
                                new ArrayList<>(ParseUtils.parseLaundries(
                                        httpsCallableResult.getData()));

                        if (nearbyLaundries.size() > 0) {

                            hideEmptyLayout();

                            getAllLaundriesData();

                            sortByDistance();

                            updateRecyclerViews();

                            // update cart laundry
                            if (Session.user.getCart() != null) {

                                if (Session.user.getCart().getLaundry() != null) {

                                    for (Laundry laundry : nearbyLaundries) {

                                        if (laundry.getId().equals(Session.user.getCart().getLaundry().getId())) {

                                            Session.user.getCart().setLaundry(laundry);
                                            break;
                                        }
                                    }
                                }
                            }

                        } else {

                            showEmptyLayout();
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    hideLoadingAnimation();

                    Log.d(TAG, "setCustomerLocation: " + e.getMessage());
                });
    }

    private void getAllLaundriesData() {

        for (Laundry laundry : nearbyLaundries) {

            LaundryRecyclerItem item = new LaundryRecyclerItem();

            // get distance
            double distance = LocationUtils.
                    getDistanceBetweenTwoPoints(customer.getLocation(), laundry.getLocation());

            // get duration
            @SuppressLint({"NewApi", "LocalSuppress"}) long duration =
                    DateUtils.getLaundryDeliveryDuration(laundry.getOrders());

            item.setLaundry(laundry);
            item.setDistance(distance);
            item.setDuration(duration);

            allLaundries.add(item);
        }
    }

    private void sortByDistance() {

        Collections.sort(allLaundries, (o1, o2) -> {
            if (o1.getDistance() == o2.getDistance())
                return 0;
            return o1.getDistance() < o2.getDistance() ? -1 : 1;
        });
    }

    private void updateRecyclerViews() {

        for (LaundryRecyclerItem item : allLaundries) {

            if (item.getLaundry().isHomeBased()) {

                homeBasedLaundries.add(item);
            }
        }

        Log.d(TAG, "updateRecyclerViews: size: " + nearbyLaundries.size());
        homeBasedLaundriesAdapter = new HomeBasedLaundriesRecyclerAdapter(homeBasedLaundries);
        homeBasedLaundriesAdapter.setLaundryClickListener(this);
        homeBasedLaundriesAdapter.setHomeBasedLaundryFilterListener(this);
        recyclerViewHomeBasedLaundries.setAdapter(homeBasedLaundriesAdapter);

        allLaundriesAdapter = new AllLaundriesRecyclerAdapter(allLaundries);
        allLaundriesAdapter.setLaundryClickListener(this);
        allLaundriesAdapter.setLaundryFilterListener(this);
        recyclerViewAllLaundries.setAdapter(allLaundriesAdapter);
    }

    private void showLaundryMenu(Laundry laundry) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String currentTime = dateFormat.format(new Date());

        String openingTime = laundry.getTimings().getOpeningTime();
        String closingTime = laundry.getTimings().getClosingTime();

        boolean laundryOpen;
        try {

            laundryOpen = TimeUtils.isTimeBetweenTwoTime(openingTime, closingTime, currentTime);

            if (!laundryOpen) {

                AlertDialog.Builder alert;
                alert = new AlertDialog.Builder(this);
                alert.setTitle("This laundry is currently closed");
                alert.setCancelable(false);

                alert.setPositiveButton("OK", (dialog, whichButton) -> {

                    dialog.dismiss();
                });

                alert.show();

            } else {

                Intent intent = new Intent(LaundriesActivity.this, LaundryProfileActivity.class);
                intent.putExtra("laundry", GsonUtils.laundryToGson(laundry));
                startActivityForResult(intent, REQUEST_CODE_MENU);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.option_home) {

            if (drawer.isDrawerOpen(GravityCompat.START)) {

                drawer.closeDrawer(GravityCompat.START);
            }

        } else if (view.getId() == R.id.option_orders) {

            startActivity(new Intent(LaundriesActivity.this, OrdersActivity.class));
            finish();

        } else if (view.getId() == R.id.option_transactions) {

            startActivity(new Intent(LaundriesActivity.this, TransactionsActivity.class));
            finish();

        } else if (view.getId() == R.id.option_profile) {

            startActivity(new Intent(LaundriesActivity.this, ProfileActivity.class));
            finish();

        } else if (view.getId() == R.id.option_logout) {

            AlertDialog.Builder alert;
            alert = new AlertDialog.Builder(this);
            alert.setTitle("Logout ?");

            alert.setPositiveButton("Yes", (dialog, whichButton) -> {

                dialog.dismiss();
                Session.destroy(LaundriesActivity.this);

                startActivity(new Intent(LaundriesActivity.this, LoginActivity.class));
                finish();
            });

            alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

            alert.show();

        } else if (view.getId() == R.id.layout_select_location) {

            toggleLocationsLayout();

        } else if (view.getId() == R.id.image_view_cart) {

            showCart();

        } else if (view.getId() == R.id.button_down) {

            toggleLocationsLayout();
        }
    }

    private void showCart() {

        startActivityForResult(new Intent(
                LaundriesActivity.this,
                CartActivity.class), REQUEST_CODE_CART);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int id) {

        View radioButton = radioGroup.findViewById(id);
        int index = radioGroup.indexOfChild(radioButton);

        toggleLocationsLayout();
        customer.getCart().clearCart(LaundriesActivity.this);
        setCustomerLocation(index);
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

    private void showEmptyLayout() {

        layoutEmpty.setVisibility(View.VISIBLE);
        layoutEmpty.bringToFront();
        layoutEmpty.animate().translationY(0);

        layoutHomeBasedLaundries.setVisibility(View.GONE);
        layoutAllLaundries.setVisibility(View.GONE);
    }

    private void hideEmptyLayout() {

        layoutEmpty.setVisibility(View.GONE);
        layoutEmpty.animate().translationY(layoutEmpty.getHeight());

        layoutHomeBasedLaundries.setVisibility(View.VISIBLE);
        layoutAllLaundries.setVisibility(View.VISIBLE);
    }

    @Override
    public void onHomeBasedLaundryClick(Laundry laundry) {

        showLaundryMenu(laundry);
    }

    @Override
    public void onAllLaundryClick(Laundry laundry) {

        showLaundryMenu(laundry);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        updateCartItemsCount();
    }

    @Override
    public void onHomeBasedLaundriesFiltered(boolean isEmpty) {

        layoutHomeBasedLaundries.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onAllLaundriesFiltered(boolean isEmpty) {

        layoutAllLaundries.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRefresh() {

        getNearbyLaundries();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}