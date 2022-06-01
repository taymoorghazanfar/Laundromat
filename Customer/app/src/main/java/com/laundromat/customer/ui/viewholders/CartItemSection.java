package com.laundromat.customer.ui.viewholders;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.customer.R;
import com.laundromat.customer.model.order.SaleItem;
import com.laundromat.customer.ui.interfaces.ISaleItemClickListener;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class CartItemSection extends Section {

    private String serviceName;
    private List<SaleItem> saleItems;
    private ISaleItemClickListener saleItemClickListener;

    public CartItemSection(String serviceName, List<SaleItem> saleItems,
                           ISaleItemClickListener saleItemClickListener) {

        super(SectionParameters.builder()
                .itemResourceId(R.layout.recycler_item_cart_item)
                .headerResourceId(R.layout.recycler_item_cart_item_header)
                .build());

        this.serviceName = serviceName;
        this.saleItems = saleItems;
        this.saleItemClickListener = saleItemClickListener;
    }

    @Override
    public int getContentItemsTotal() {
        return saleItems.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {

        return new CartItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {

        SaleItem saleItem = saleItems.get(position);


        CartItemViewHolder itemViewHolder = (CartItemViewHolder) holder;

        Picasso.get()
                .load(saleItem.getWashableItem().getImageUrl())
                .into(itemViewHolder.imageViewItem);

        itemViewHolder.textViewName.setText(saleItem.getWashableItem().getName());

        itemViewHolder.textViewQuantity
                .setText(MessageFormat.format("x{0}", saleItem.getQuantity()));

        itemViewHolder.textViewPrice
                .setText(MessageFormat.format("PKR {0}", saleItem.getPrice()));

        itemViewHolder.itemView.setOnClickListener(view -> {

            if (saleItemClickListener != null) {

                saleItemClickListener.onSaleItemClick(serviceName, saleItem);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {

        return new CartItemHeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {

        ((CartItemHeaderViewHolder) holder).textViewServiceName.setText(serviceName);

    }
}
