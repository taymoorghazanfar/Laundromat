package com.laundromat.admin.ui.viewholders;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.admin.R;
import com.laundromat.admin.model.order.SaleItem;

import java.text.MessageFormat;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class OrderSummarySection extends Section {

    private String serviceName;
    private List<SaleItem> saleItems;

    public OrderSummarySection(String serviceName, List<SaleItem> saleItems) {

        super(SectionParameters.builder()
                .itemResourceId(R.layout.recycler_item_order_summary)
                .headerResourceId(R.layout.recycler_item_order_summary_header)
                .build());

        this.serviceName = serviceName;
        this.saleItems = saleItems;
    }

    @Override
    public int getContentItemsTotal() {
        return saleItems.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {

        return new OrderSummaryItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {

        SaleItem saleItem = saleItems.get(position);

        OrderSummaryItemViewHolder itemViewHolder = (OrderSummaryItemViewHolder) holder;

        itemViewHolder.textViewItem
                .setText(MessageFormat.format("{0}x {1}",
                        saleItem.getQuantity(), saleItem.getWashableItem().getName()));

        itemViewHolder.textViewPrice
                .setText(MessageFormat.format("PKR {0}", saleItem.getPrice()));
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {

        return new OrderSummaryHeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {

        ((OrderSummaryHeaderViewHolder) holder).textViewServiceName.setText(serviceName);
    }
}
