package com.laundromat.admin.ui.viewholders;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.admin.R;
import com.laundromat.admin.model.washable.ServiceType;
import com.laundromat.admin.model.washable.WashableItem;

import java.text.MessageFormat;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class ServiceTypeSection extends Section {

    private final String serviceType;
    private final String laundryName;
    private final List<WashableItem> items;

    public ServiceTypeSection(String serviceType, String laundryName, List<WashableItem> items) {

        super(SectionParameters.builder()
                .itemResourceId(R.layout.recycler_item_service_child)
                .headerResourceId(R.layout.recycler_item_laundry_name)
                .build());

        this.serviceType = serviceType;
        this.laundryName = laundryName;
        this.items = items;
    }

    @Override
    public int getContentItemsTotal() {
        return items.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {

        return new LaundryServiceChildViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {

        WashableItem item = items.get(position);

        LaundryServiceChildViewHolder itemViewHolder = (LaundryServiceChildViewHolder) holder;

        itemViewHolder.textViewItemName
                .setText(item.getName());

        double price = 0;

        for (ServiceType service : item.getServiceTypes()) {

            if (service.getName().equals(this.serviceType)) {

                price = service.getPrice();
                break;
            }
        }

        itemViewHolder.textViewPrice
                .setText(MessageFormat.format("PKR {0}", price));
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {

        return new LaundryServiceHeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {

        ((LaundryServiceHeaderViewHolder) holder).textViewLaundryName.setText(laundryName);
    }
}
