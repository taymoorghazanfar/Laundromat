package com.laundromat.customer.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.customer.R;
import com.laundromat.customer.model.Customer;
import com.laundromat.customer.model.Laundry;
import com.laundromat.customer.model.order.OrderItem;
import com.laundromat.customer.model.order.SaleItem;
import com.laundromat.customer.model.util.Cart;
import com.laundromat.customer.model.washable.ServiceType;
import com.laundromat.customer.model.washable.WashableItem;
import com.laundromat.customer.prefs.Session;
import com.laundromat.customer.ui.adapters.SelectServicesRecyclerAdapter;
import com.laundromat.customer.ui.interfaces.IQuantitySelectedListener;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuItemActivity extends AppCompatActivity
        implements View.OnClickListener, IQuantitySelectedListener {

    //Variables
    private Customer customer = Session.user;
    private Cart cart = customer.getCart();
    private Laundry laundry;
    private WashableItem washableItem;
    private List<ServiceType> availableServiceTypes;

    // Views
    private ImageButton buttonBack;
    private ImageView imageViewItem;
    private TextView textViewName;
    private RecyclerView recyclerViewServices;
    private SelectServicesRecyclerAdapter adapter;
    private Button buttonAddToOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_item);

        getIntentData();

        initViews();

        initRecyclerView();

        initOrderButton();
    }

    private void getIntentData() {

        laundry = getIntent().getParcelableExtra("laundry");

        if (getIntent().getStringExtra("activity_name") != null) {

            washableItem = ((SaleItem) getIntent()
                    .getParcelableExtra("item")).getWashableItem();

        } else {

            washableItem = getIntent().getParcelableExtra("item");
        }
    }

    private void initViews() {

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        imageViewItem = findViewById(R.id.image_view_item);
        Picasso.get()
                .load(washableItem.getImageUrl())
                .into(imageViewItem);

        textViewName = findViewById(R.id.text_view_name);
        textViewName.setText(washableItem.getName());
    }

    private void initRecyclerView() {

        availableServiceTypes = new ArrayList<>();

        for (ServiceType serviceType : washableItem.getServiceTypes()) {

            // getting shallow copy of each service
            availableServiceTypes.add(new ServiceType(serviceType));
        }

        // if there are some items previously selected, show their quantity (ie. washing x3)
        for (int x = 0; x < availableServiceTypes.size(); x++) {

            int quantity = cart.getServiceQuantity(
                    availableServiceTypes.get(x).getName(),
                    washableItem.getName());

            if (quantity != -1) {

                availableServiceTypes.get(x).setQuantity(quantity);
            }
        }

        recyclerViewServices = findViewById(R.id.recycler_view_services);
        recyclerViewServices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SelectServicesRecyclerAdapter(availableServiceTypes);
        adapter.setQuantitySelectedListener(this);
        recyclerViewServices.setAdapter(adapter);
    }

    private void initOrderButton() {

        buttonAddToOrder = findViewById(R.id.button_add_to_order);
        buttonAddToOrder.setOnClickListener(this);

        // set currently selected items price to the button
        updateSelectedItemsPrice();
    }

    private void updateSelectedItemsPrice() {

        double price = 0;

        for (ServiceType serviceType : availableServiceTypes) {

            price = price + serviceType.getQuantity() * serviceType.getPrice();
            Log.d("price", "price: " + price);
        }

        Log.d("price", "final price: " + price);

        buttonAddToOrder.setText(MessageFormat.format("Add to order (PKR {0})", price));
    }

    private void addItemsToCart() {

        Map<String, OrderItem> orderItems = new HashMap<>();

        // get selected services and create an order item
        for (ServiceType serviceType : availableServiceTypes) {

            if (serviceType.getQuantity() > 0) {

                OrderItem orderItem = new OrderItem();

                int saleItemQuantity = serviceType.getQuantity();
                double saleItemPrice = saleItemQuantity * serviceType.getPrice();

                SaleItem saleItem = new SaleItem(washableItem, saleItemQuantity, saleItemPrice);
                orderItem.getSaleItems().put(saleItem.getWashableItem().getName(), saleItem);
                orderItems.put(serviceType.getName(), orderItem);

            } else {

                if (cart.getOrderItems().get(serviceType.getName()) != null) {

                    if (cart.getOrderItems().get(serviceType.getName())
                            .getSaleItems().get(washableItem.getName()) != null) {

                        cart.getOrderItems().get(serviceType.getName())
                                .getSaleItems().remove(washableItem.getName());
                    }
                }
            }
        }

        cart.addItemsToCart(MenuItemActivity.this, laundry, orderItems);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_back) {

            finish();

        } else if (view.getId() == R.id.button_add_to_order) {

            buttonAddToOrder.setEnabled(false);
            addItemsToCart();
        }
    }

    // listener for service item quantity increase or decrease
    @Override
    public void onQuantitySelected(ServiceType serviceType, int quantity) {

        for (ServiceType availableServiceType : availableServiceTypes) {

            if (availableServiceType.getName().equals(serviceType.getName())) {

                serviceType.setQuantity(quantity);
            }
        }

        buttonAddToOrder.setEnabled(true);
        updateSelectedItemsPrice();
    }
}