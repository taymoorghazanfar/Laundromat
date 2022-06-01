package com.laundromat.customer.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.laundromat.customer.R;
import com.laundromat.customer.model.order.OrderItem;
import com.laundromat.customer.model.order.SaleItem;
import com.laundromat.customer.model.util.Cart;
import com.laundromat.customer.prefs.Session;
import com.laundromat.customer.ui.decorators.SpacesItemDecoration;
import com.laundromat.customer.ui.interfaces.ISaleItemClickListener;
import com.laundromat.customer.ui.viewholders.CartItemSection;
import com.laundromat.customer.utils.NumberUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class CartActivity extends AppCompatActivity
        implements View.OnClickListener, ISaleItemClickListener {

    // Constants
    private static final int REQUEST_CODE_MENU_ITEM = 111;

    // Variables
    Cart cart = Session.user.getCart();

    // Views
    private ImageButton buttonBack;
    private ImageButton buttonClear;
    private TextView textViewLaundryName;
    private TextView textViewEmpty;
    private RecyclerView recyclerViewItems;
    private SectionedRecyclerViewAdapter adapter;
    private LinearLayout layoutCheckout;
    private TextView textViewTotal;
    private TextView textViewPrice;
    private TextView textViewDiscountGiven;
    private AppCompatButton buttonProceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();

        setupViews();

        initRecyclerView();
    }

    public void setupViews() {

        if (!cart.isEmpty()) {

            //calculate discount
            double discount = NumberUtils.getDiscount(cart.getLaundry().getDiscount(), cart.getPrice());
            double discountGiven = cart.getPrice() - discount;

            textViewLaundryName.setVisibility(View.VISIBLE);
            buttonClear.setVisibility(View.VISIBLE);
            textViewEmpty.setVisibility(View.GONE);
            layoutCheckout.setVisibility(View.VISIBLE);

            textViewLaundryName.setText(cart.getLaundry().getName());

            textViewTotal.setText(MessageFormat
                    .format("Total: PKR {0}", cart.getPrice()));
            textViewDiscountGiven.setText(MessageFormat
                    .format("Discount: PKR {0}", discount));
            textViewPrice.setText(MessageFormat.format("PKR {0}", discountGiven));

        } else {

            textViewEmpty.setVisibility(View.VISIBLE);
            buttonClear.setVisibility(View.GONE);
            textViewLaundryName.setVisibility(View.GONE);
            layoutCheckout.setVisibility(View.GONE);
        }
    }

    private void initViews() {

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        buttonClear = findViewById(R.id.button_clear);
        buttonClear.setOnClickListener(this);

        buttonProceed = findViewById(R.id.button_proceed);
        buttonProceed.setOnClickListener(this);

        textViewLaundryName = findViewById(R.id.text_view_name);
        textViewPrice = findViewById(R.id.text_view_price);
        layoutCheckout = findViewById(R.id.layout_checkout);
        textViewEmpty = findViewById(R.id.text_view_empty);

        textViewTotal = findViewById(R.id.text_view_total);
        textViewDiscountGiven = findViewById(R.id.text_view_discount_given);

        recyclerViewItems = findViewById(R.id.recycler_view_items);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_margin);
        this.recyclerViewItems.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
    }

    private void initRecyclerView() {

        adapter = new SectionedRecyclerViewAdapter();

        for (final Map.Entry<String, OrderItem> entry : cart.getOrderItems().entrySet()) {

            if (entry.getValue().getSaleItems().size() > 0) {

                List<SaleItem> saleItems = new ArrayList<>(entry.getValue().getSaleItems().values());

                adapter.addSection(
                        new CartItemSection(entry.getKey(), saleItems, this));
            }
        }

        recyclerViewItems.setAdapter(adapter);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_back) {

            finish();

        } else if (view.getId() == R.id.button_proceed) {

            gotoCheckout();

        } else if (view.getId() == R.id.button_clear) {

            showClearCartDialog();
        }
    }

    private void gotoCheckout() {

        startActivity(new Intent(CartActivity.this, CheckoutActivity.class));
    }

    private void showClearCartDialog() {

        AlertDialog.Builder alert;
        alert = new AlertDialog.Builder(this);
        alert.setTitle("Clear Cart");
        alert.setMessage("Proceed to remove all selected items from cart ?");

        alert.setPositiveButton("Yes", (dialog, whichButton) -> {

            cart.clearCart(CartActivity.this);
            dialog.dismiss();
            setupViews();
            adapter.removeAllSections();
        });

        alert.setNegativeButton("Cancel", (dialog, i) -> dialog.dismiss());

        alert.show();
    }

    @Override
    public void onSaleItemClick(String serviceTypeName, SaleItem saleItem) {

        cart.showCart();

        Intent intent = new Intent(CartActivity.this, MenuItemActivity.class);
        intent.putExtra("activity_name", "CartActivity");
        intent.putExtra("laundry", cart.getLaundry());
        intent.putExtra("item", saleItem);
        startActivityForResult(intent, REQUEST_CODE_MENU_ITEM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MENU_ITEM) {

            setupViews();
            adapter.removeAllSections();
            initRecyclerView();
        }
    }
}