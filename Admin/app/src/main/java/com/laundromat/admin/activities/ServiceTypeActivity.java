package com.laundromat.admin.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.admin.R;
import com.laundromat.admin.model.Laundry;
import com.laundromat.admin.model.Merchant;
import com.laundromat.admin.model.order.OrderItem;
import com.laundromat.admin.model.order.SaleItem;
import com.laundromat.admin.model.washable.ServiceType;
import com.laundromat.admin.model.washable.WashableItem;
import com.laundromat.admin.model.washable.WashableItemCategory;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.viewholders.OrderSummarySection;
import com.laundromat.admin.ui.viewholders.ServiceTypeSection;
import com.laundromat.admin.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class ServiceTypeActivity extends AppCompatActivity implements View.OnClickListener {

    // Variables
    private ServiceType serviceType;

    // Views
    private ImageButton buttonBack;
    private TextView textViewEmpty;
    private TextView textViewName;
    private RecyclerView recyclerViewServiceType;
    private SectionedRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_type);

        getIntentData();

        initViews();
    }

    private void getIntentData() {

        String serviceTypeGson = getIntent().getStringExtra("service");
        serviceType = GsonUtils.gsonToService(serviceTypeGson);
    }

    private void initViews() {

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        textViewEmpty = findViewById(R.id.text_view_empty);
        textViewEmpty.setVisibility(View.GONE);

        textViewName = findViewById(R.id.text_view_name);
        textViewName.setText(serviceType.getName());

        recyclerViewServiceType = findViewById(R.id.recycler_view_service_type);

        setupRecyclerView();
    }

    private void setupRecyclerView() {

        recyclerViewServiceType.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SectionedRecyclerViewAdapter();

        for (Merchant merchant : Session.user.getMerchants()) {

            Laundry laundry = merchant.getLaundry();
            List<WashableItem> items = new ArrayList<>();

            for (WashableItemCategory washableItemCategory : laundry.getMenu()) {

                for (WashableItem washableItem : washableItemCategory.getWashableItems()) {

                    for (ServiceType service : washableItem.getServiceTypes()) {

                        if (service.getName().equals(serviceType.getName())) {

                            items.add(washableItem);
                        }
                    }
                }
            }

            if (items.size() > 0) {

                adapter.addSection(
                        new ServiceTypeSection(serviceType.getName(), laundry.getName(), items));
            }
        }

        recyclerViewServiceType.setAdapter(adapter);

        textViewEmpty.setVisibility(adapter.getSectionCount() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_back) {

            finish();
        }
    }
}