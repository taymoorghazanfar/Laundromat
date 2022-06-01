package com.laundromat.customer.ui.viewholders;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.customer.R;
import com.laundromat.customer.model.washable.WashableItem;
import com.laundromat.customer.ui.adapters.ServicesRecyclerAdapter;
import com.laundromat.customer.ui.interfaces.IServiceClickListener;
import com.laundromat.customer.ui.interfaces.ITopReachedListener;
import com.laundromat.customer.ui.interfaces.IMenuItemClickListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class MenuSection extends Section {

    private Context context;
    private String title;
    private List<WashableItem> menuItems;
    private IMenuItemClickListener menuItemClickListener;
    private ITopReachedListener topReachedListener;


    public MenuSection(Context context, String title, List<WashableItem> menuItems,
                       IMenuItemClickListener menuItemClickListener,
                       ITopReachedListener topReachedListener) {

        super(SectionParameters.builder()
                .itemResourceId(R.layout.recycler_item_menu_item)
                .headerResourceId(R.layout.recycler_item_menu_header)
                .build());

        this.context = context;
        this.title = title;
        this.menuItems = menuItems;
        this.menuItemClickListener = menuItemClickListener;
        this.topReachedListener = topReachedListener;
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
        adapter.setServiceClickListener(() -> {

            if (menuItemClickListener != null) {

                menuItemClickListener.onMenuItemClick(menuItem);
            }
        });
        itemViewHolder.recyclerViewServices.setAdapter(adapter);
        itemViewHolder.recyclerViewServices
                .setLayoutManager(new LinearLayoutManager(
                        context, RecyclerView.HORIZONTAL, false));

        itemViewHolder.itemView.setOnClickListener(view -> {

            if (menuItemClickListener != null) {

                menuItemClickListener.onMenuItemClick(menuItem);
            }
        });

        itemViewHolder.layoutItem.setOnClickListener(view -> {

            if (menuItemClickListener != null) {

                menuItemClickListener.onMenuItemClick(menuItem);
            }
        });

        itemViewHolder.recyclerViewServices.setOnClickListener(view -> {

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

        if (topReachedListener != null) {

            topReachedListener.onTopReached(holder.getAdapterPosition());
        }
    }
}
