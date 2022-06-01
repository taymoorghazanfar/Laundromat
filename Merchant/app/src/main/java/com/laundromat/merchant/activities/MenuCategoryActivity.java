package com.laundromat.merchant.activities;

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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.laundromat.merchant.R;
import com.laundromat.merchant.dialogs.AddMenuCategoryDialog;
import com.laundromat.merchant.dialogs.DeleteMenuCategoryDialog;
import com.laundromat.merchant.dialogs.EditMenuCategoryDialog;
import com.laundromat.merchant.model.Merchant;
import com.laundromat.merchant.model.order.Order;
import com.laundromat.merchant.model.order.OrderStatus;
import com.laundromat.merchant.model.washable.WashableItemCategory;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.adapters.MenuCategoryRecyclerViewAdapter;
import com.laundromat.merchant.ui.decorators.SpacesItemDecoration;
import com.laundromat.merchant.ui.interfaces.IMenuCategoryClickListener;
import com.laundromat.merchant.ui.interfaces.IMenuCategoryCreatedListener;
import com.laundromat.merchant.ui.interfaces.IMenuCategoryDeletedListener;
import com.laundromat.merchant.ui.interfaces.IMenuCategoryEditedListener;
import com.laundromat.merchant.ui.views.MovableFloatingActionButton;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MenuCategoryActivity extends AppCompatActivity
        implements View.OnClickListener, IMenuCategoryClickListener,
        IMenuCategoryCreatedListener, IMenuCategoryEditedListener, IMenuCategoryDeletedListener {

    // Constants
    private static final int REQUEST_CODE_MENU_ITEM = 111;

    // Variables
    private Merchant merchant = Session.user;
    private int selectedIndex;

    // Views
    private Toolbar toolbar;
    private RelativeLayout layoutLoading;
    private DrawerLayout drawer;
    private TextView textViewLaundryName;
    private TextView textViewEmpty;
    private RecyclerView recyclerViewMenuCategory;
    private MenuCategoryRecyclerViewAdapter adapter;
    private MovableFloatingActionButton buttonAdd;

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_category);

        initViews();
    }

    private void initViews() {

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        // setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initDrawer();

        initDrawerMenu();

        initSearchView();

        textViewLaundryName = findViewById(R.id.text_view_laundry_name);
        textViewLaundryName.setText(merchant.getLaundry().getName());

        this.textViewEmpty = findViewById(R.id.text_view_empty);

        if (Session.user.getLaundry().getMenu().size() > 0) {

            textViewEmpty.setVisibility(View.GONE);
        }

        this.buttonAdd = findViewById(R.id.button_add);
        this.buttonAdd.setOnClickListener(this);

        sortMenuCategories();

        // setup recycler view
        this.recyclerViewMenuCategory = findViewById(R.id.recycler_view_menu_category);
        adapter =
                new MenuCategoryRecyclerViewAdapter(Session.user.getLaundry().getMenu());
        adapter.setMenuCategoryClickListener(this);
        recyclerViewMenuCategory.setLayoutManager(new LinearLayoutManager(this));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        recyclerViewMenuCategory.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        recyclerViewMenuCategory.setAdapter(adapter);
    }

    private void sortMenuCategories() {

        Collections.sort(Session.user.getLaundry().getMenu(),
                (cat1, cat2)
                        -> cat1.getTitle().compareTo(cat2.getTitle()));
    }

    private void initSearchView() {

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

        TextView textViewName = findViewById(R.id.text_view_user_name);
        TextView textViewPhoneNumber = findViewById(R.id.text_view_phone_number);

        textViewName.setText(Session.user.getFullName());
        textViewPhoneNumber.setText(MessageFormat.format("+92{0}",
                Session.user.getPhoneNumber()));

        View optionHome = findViewById(R.id.option_dashboard);
        optionHome.setOnClickListener(this);

        View optionOrders = findViewById(R.id.option_orders);
        optionOrders.setOnClickListener(this);

        View optionMenu = findViewById(R.id.option_menu);
        optionMenu.setOnClickListener(this);

        View optionTransactions = findViewById(R.id.option_transactions);
        optionTransactions.setOnClickListener(this);

        View optionLogout = findViewById(R.id.option_logout);
        optionLogout.setOnClickListener(this);

        View optionProfile = findViewById(R.id.option_profile);
        optionProfile.setOnClickListener(this);
    }

    private void showMenuItemsActivity(WashableItemCategory menuCategory) {

        Intent intent = new Intent(MenuCategoryActivity.this, MenuItemsActivity.class);
        intent.putExtra("menu_category", menuCategory);

        startActivityForResult(intent, REQUEST_CODE_MENU_ITEM);
    }

    private void showAddMenuCategoryDialog() {

        AddMenuCategoryDialog dialog = new AddMenuCategoryDialog();
        dialog.setCancelable(false);
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "dialog_add_category");
    }

    private void showEditMenuCategoryDialog(WashableItemCategory menuCategory, int index) {

        if (Session.user.getLaundry().getOrders().size() > 0) {

            boolean orderPending = false;

            for (Order order : Session.user.getLaundry().getOrders()) {

                if (order.getStatus() != OrderStatus.COMPLETED
                        && order.getStatus() != OrderStatus.DECLINED
                        && order.getStatus() != OrderStatus.CANCELLED) {

                    orderPending = true;
                    break;
                }
            }

            if (orderPending) {

                Toast.makeText(this, "Your laundry have pending orders." +
                        " Clear all orders to perform this action", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable("menu_category", menuCategory);
        bundle.putInt("menu_category_index", index);

        EditMenuCategoryDialog dialog = new EditMenuCategoryDialog();
        dialog.setArguments(bundle);
        dialog.setCancelable(false);
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "dialog_edit_category");
    }

    private void showDeleteMenuCategoryDialog(int index) {

        if (Session.user.getLaundry().getOrders().size() > 0) {

            boolean orderPending = false;

            for (Order order : Session.user.getLaundry().getOrders()) {

                if (order.getStatus() != OrderStatus.COMPLETED
                        && order.getStatus() != OrderStatus.DECLINED
                        && order.getStatus() != OrderStatus.CANCELLED) {

                    orderPending = true;
                    break;
                }
            }

            if (orderPending) {

                Toast.makeText(this, "Your laundry have pending orders." +
                        " Clear all orders to perform this action", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Bundle bundle = new Bundle();
        bundle.putInt("menu_category_index", index);

        DeleteMenuCategoryDialog dialog = new DeleteMenuCategoryDialog();
        dialog.setArguments(bundle);
        dialog.setCancelable(false);
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "dialog_delete_category");
    }

    private void notifyChanges(String message) {

        // refresh view recycler view
        adapter =
                new MenuCategoryRecyclerViewAdapter(Session.user.getLaundry().getMenu());
        adapter.setMenuCategoryClickListener(this);
        recyclerViewMenuCategory.setAdapter(adapter);

        // hide or show empty message
        textViewEmpty.setVisibility(Session.user.getLaundry().getMenu().size() > 0 ?
                View.GONE : View.VISIBLE);

        Toast.makeText(this,
                message,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.option_dashboard) {

            startActivity(new Intent(MenuCategoryActivity.this, DashboardActivity.class));
            finish();

        } else if (view.getId() == R.id.option_orders) {

            startActivity(new Intent(MenuCategoryActivity.this, OrdersActivity.class));
            finish();

        } else if (view.getId() == R.id.option_menu) {

            if (drawer.isDrawerOpen(GravityCompat.START)) {

                drawer.closeDrawer(GravityCompat.START);
            }

        } else if (view.getId() == R.id.option_transactions) {

            startActivity(new Intent(MenuCategoryActivity.this, TransactionsActivity.class));
            finish();

        } else if (view.getId() == R.id.option_profile) {

            startActivity(new Intent(MenuCategoryActivity.this, ProfileActivity.class));
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
                data.put("laundry_id", Session.user.getLaundry().getId());
                data.put("status", false);

                showLoadingAnimation();

                FirebaseFunctions
                        .getInstance()
                        .getHttpsCallable("laundry-setAvailability")
                        .call(data)
                        .addOnSuccessListener(httpsCallableResult -> {

                            hideLoadingAnimation();

                            Session.destroy(MenuCategoryActivity.this);

                            startActivity(new Intent(MenuCategoryActivity.this, LoginActivity.class));
                            finish();

                        })
                        .addOnFailureListener(e -> {

                            hideLoadingAnimation();

                            Toast.makeText(MenuCategoryActivity.this,
                                    e.getMessage(), Toast.LENGTH_SHORT).show();

                            Log.d("laundry", "acceptTripRequest: " + e.getMessage());
                        });
            });

            alert.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

            alert.show();

        } else if (view.getId() == R.id.button_add) {

            showAddMenuCategoryDialog();
        }
    }

    @Override
    public void onMenuCategoryClick(WashableItemCategory menuCategory, int index) {

        selectedIndex = index;
        showMenuItemsActivity(menuCategory);
    }

    @Override
    public void onMenuCategoryEditClick(WashableItemCategory menuCategory, int index) {

        showEditMenuCategoryDialog(menuCategory, index);
    }

    @Override
    public void onMenuCategoryDeleteClick(int index) {

        showDeleteMenuCategoryDialog(index);
    }

    @Override
    public void onMenuCategoryCreated(WashableItemCategory menuCategory) {

        Session.user.getLaundry().getMenu().add(menuCategory);
        sortMenuCategories();
        notifyChanges("Menu category added");
    }

    @Override
    public void onMenuCategoryEdited(WashableItemCategory menuCategory, int index) {

        // update session user data
        Session.user.getLaundry().getMenu().set(index, menuCategory);
        sortMenuCategories();
        notifyChanges("Menu category updated");
    }

    @Override
    public void onMenuCategoryDeleted(int index) {

        // delete the menu category from session user
        Session.user.getLaundry().getMenu().remove(index);
        sortMenuCategories();
        notifyChanges("Menu category deleted");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MENU_ITEM && resultCode == RESULT_OK) {

            if (data != null) {

                if (adapter != null) {

                    WashableItemCategory menuCategory = data.getParcelableExtra("menu_category");
                    Session.user.getLaundry().getMenu().set(selectedIndex, menuCategory);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(MenuCategoryActivity.this, DashboardActivity.class));
        finish();
    }
}