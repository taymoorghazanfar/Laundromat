package com.laundromat.merchant.ui.viewholders;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.merchant.R;
import com.laundromat.merchant.model.washable.WashableItem;
import com.laundromat.merchant.ui.adapters.ServicesRecyclerAdapter;
import com.laundromat.merchant.ui.interfaces.IMenuItemClickListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class MenuSection extends Section {

    private Context context;
    private String title;
    private List<WashableItem> menuItems;
    private IMenuItemClickListener menuItemClickListener;

    public MenuSection(Context context, String title, List<WashableItem> menuItems,
                       IMenuItemClickListener menuItemClickListener) {

        super(SectionParameters.builder()
                .itemResourceId(R.layout.recycler_item_menu_item)
                .headerResourceId(R.layout.recycler_item_menu_header)
                .build());

        this.context = context;
        this.title = title;
        this.menuItems = menuItems;
        this.menuItemClickListener = menuItemClickListener;
    }

    @Override
    public int getContentItemsTotal() {
        return menuItems.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {

        return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {

        WashableItem menuItem = menuItems.get(position);

        MenuItemViewHolder itemViewHolder = (MenuItemViewHolder) holder;
        itemViewHolder.textViewName.setText(menuItem.getName());

        Picasso.get()
                .load(menuItem.getImageUrl())
                .into(itemViewHolder.imageViewItem);

        ServicesRecyclerAdapter adapter = new ServicesRecyclerAdapter(menuItem.getServiceTypes());
        itemViewHolder.recyclerViewServices.setAdapter(adapter);
        itemViewHolder.recyclerViewServices
                .setLayoutManager(new LinearLayoutManager(
                        context, RecyclerView.HORIZONTAL, false));

        itemViewHolder.itemView.setOnClickListener(view -> {

            if (menuItemClickListener != null) {

                menuItemClickListener.onMenuItemClick(menuItem);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {

        return new MenuHeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {

        ((MenuHeaderViewHolder) holder).textViewTitle.setText(title);

    }
}
