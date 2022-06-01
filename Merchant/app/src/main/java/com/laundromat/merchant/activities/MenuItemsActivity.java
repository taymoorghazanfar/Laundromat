package com.laundromat.merchant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.merchant.R;
import com.laundromat.merchant.dialogs.AddMenuItemDialog;
import com.laundromat.merchant.dialogs.DeleteMenuItemDialog;
import com.laundromat.merchant.dialogs.EditMenuItemDialog;
import com.laundromat.merchant.dialogs.ShowMenuItemDialog;
import com.laundromat.merchant.model.order.Order;
import com.laundromat.merchant.model.order.OrderStatus;
import com.laundromat.merchant.model.washable.WashableItem;
import com.laundromat.merchant.model.washable.WashableItemCategory;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.adapters.MenuItemRecyclerViewAdapter;
import com.laundromat.merchant.ui.decorators.SpacesItemDecoration;
import com.laundromat.merchant.ui.interfaces.IMenuItemClickListener;
import com.laundromat.merchant.ui.interfaces.IMenuItemCreatedListener;
import com.laundromat.merchant.ui.interfaces.IMenuItemDeletedListener;
import com.laundromat.merchant.ui.interfaces.IMenuItemEditedListener;
import com.laundromat.merchant.ui.views.MovableFloatingActionButton;
import com.laundromat.merchant.utils.Globals;
import com.laundromat.merchant.utils.ParseUtils;
import com.squareup.picasso.Picasso;

import java.util.Collections;

public class MenuItemsActivity extends AppCompatActivity
        implements View.OnClickListener, IMenuItemClickListener,
        IMenuItemCreatedListener, IMenuItemEditedListener, IMenuItemDeletedListener {

    // Variables
    private WashableItemCategory menuCategory;

    // Views
    private TextView textViewCategoryName;
    private ImageButton buttonBack;
    private TextView textViewEmpty;
    private RecyclerView recyclerViewMenuItems;
    private MenuItemRecyclerViewAdapter adapter;
    private MovableFloatingActionButton buttonAdd;
    private RelativeLayout layoutLoading;

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_items);

        getIntentData();

        initViews();
    }

    @Override
    public void onBackPressed() {
        backPressed();
    }

    private void getIntentData() {

        menuCategory = getIntent().getParcelableExtra("menu_category");
    }

    private void initViews() {

        layoutLoading = findViewById(R.id.layout_loading);
        layoutLoading.setVisibility(View.GONE);

        textViewCategoryName = findViewById(R.id.text_view_category_name);
        textViewCategoryName.setText(menuCategory.getTitle());

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        textViewEmpty = findViewById(R.id.text_view_empty);

        if (menuCategory.getWashableItems().size() > 0) {

            textViewEmpty.setVisibility(View.GONE);
        }

        this.buttonAdd = findViewById(R.id.button_add);
        this.buttonAdd.setOnClickListener(this);

        sortMenuItems();

        // setup recycler view
        this.recyclerViewMenuItems = findViewById(R.id.recycler_view_menu_items);
        adapter = new MenuItemRecyclerViewAdapter(menuCategory.getWashableItems());
        adapter.setMenuItemClickListener(this);
        recyclerViewMenuItems.setLayoutManager(new LinearLayoutManager(this));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        recyclerViewMenuItems.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        recyclerViewMenuItems.setAdapter(adapter);

        initSearchView();
    }

    private void sortMenuItems() {

        Collections.sort(menuCategory.getWashableItems(),
                (item1, item2)
                        -> item1.getName().compareTo(item2.getName()));
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

    private void showViewMenuItemDialog(WashableItem menuItem) {

        Bundle bundle = new Bundle();
        bundle.putParcelable("menu_item", menuItem);

        ShowMenuItemDialog dialog = new ShowMenuItemDialog();
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "dialog_show_item");
    }

    private void showAddMenuItemDialog() {

        // if available services are null, get em
        if (Globals.getAvailableServices() == null || Globals.getAvailableServices().size() == 0) {

            showLoadingAnimation();

            FirebaseFunctions
                    .getInstance()
                    .getHttpsCallable("admin-getServiceTypes")
                    .call()
                    .addOnSuccessListener(httpsCallableResult -> {

                        if (httpsCallableResult.getData() != null) {

                            hideLoadingAnimation();

                            //todo: show hide anim
                            Globals.setAvailableServices(ParseUtils
                                    .parseServiceTypes(httpsCallableResult.getData()));

                            Bundle bundle = new Bundle();
                            bundle.putParcelable("menu_category", menuCategory);

                            AddMenuItemDialog dialog = new AddMenuItemDialog();
                            dialog.setArguments(bundle);
                            dialog.setCancelable(false);
                            dialog.setListener(this);
                            dialog.show(getSupportFragmentManager(), "dialog_add_item");
                        }
                    })
                    .addOnFailureListener(e -> {

                        hideLoadingAnimation();
                        Toast.makeText(MenuItemsActivity.this,
                                e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("services", "showAddMenuItemDialog: " + e.getMessage());
                    });
        } else {

            Bundle bundle = new Bundle();
            bundle.putParcelable("menu_category", menuCategory);

            AddMenuItemDialog dialog = new AddMenuItemDialog();
            dialog.setArguments(bundle);
            dialog.setCancelable(false);
            dialog.setListener(this);
            dialog.show(getSupportFragmentManager(), "dialog_add_item");
        }
    }

    private void showEditMenuItemDialog(WashableItem item, int index) {

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
        bundle.putParcelable("menu_item", item);
        bundle.putInt("menu_item_index", index);

        EditMenuItemDialog dialog = new EditMenuItemDialog();
        dialog.setArguments(bundle);
        dialog.setCancelable(false);
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "dialog_edit_item");
    }

    private void showDeleteMenuItemDialog(int index) {

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
        bundle.putInt("menu_item_index", index);

        DeleteMenuItemDialog dialog = new DeleteMenuItemDialog();
        dialog.setArguments(bundle);
        dialog.setCancelable(false);
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "dialog_delete_item");
    }

    private void notifyChanges(String message) {

        sortMenuItems();
        adapter = new MenuItemRecyclerViewAdapter(menuCategory.getWashableItems());
        adapter.setMenuItemClickListener(this);
        recyclerViewMenuItems.setAdapter(adapter);

        // show or hide empty message text view
        textViewEmpty.setVisibility(menuCategory.getWashableItems().size() > 0 ?
                View.GONE : View.VISIBLE);

        Toast.makeText(this,
                message,
                Toast.LENGTH_SHORT).show();
    }

    private void backPressed() {

        Intent intent = new Intent();
        intent.putExtra("menu_category", menuCategory);
        setResult(RESULT_OK, intent);
        finish();
    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_back) {

            backPressed();

        } else if (view.getId() == R.id.button_add) {

            showAddMenuItemDialog();
        }
    }

    @Override
    public void onMenuItemClick(WashableItem menuItem) {

        showViewMenuItemDialog(menuItem);
    }

    @Override
    public void onMenuItemEditClick(WashableItem menuItem, int index) {

        showEditMenuItemDialog(menuItem, index);
    }

    @Override
    public void onMenuItemDeleteClick(int index) {

        showDeleteMenuItemDialog(index);
    }

    @Override
    public void onMenuItemCreated(WashableItem menuItem) {
        menuCategory.getWashableItems().add(menuItem);
        notifyChanges("New menu item added");
    }

    @Override
    public void onMenuItemEdited(WashableItem menuItem, int index) {

        menuCategory.getWashableItems().set(index, menuItem);
        Picasso.get()
                .invalidate(menuItem.getImageUrl());
        notifyChanges("Menu item updated");
    }

    @Override
    public void onMenuItemDeleted(int index) {

        menuCategory.getWashableItems().remove(index);
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
}