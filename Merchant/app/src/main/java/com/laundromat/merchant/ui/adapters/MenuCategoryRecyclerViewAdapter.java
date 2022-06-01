package com.laundromat.merchant.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.merchant.R;
import com.laundromat.merchant.model.washable.WashableItem;
import com.laundromat.merchant.model.washable.WashableItemCategory;
import com.laundromat.merchant.ui.interfaces.IMenuCategoryClickListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class MenuCategoryRecyclerViewAdapter
        extends RecyclerView.Adapter<MenuCategoryRecyclerViewAdapter.ViewHolder> implements Filterable {

    private List<WashableItemCategory> categories;
    private List<WashableItemCategory> categoriesFull;

    private IMenuCategoryClickListener menuCategoryClickListener;
    private Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<WashableItemCategory> filteredList = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0) {

                filteredList.addAll(categoriesFull);
            } else {

                String stringPattern = charSequence.toString().toLowerCase().trim();

                for (WashableItemCategory item : categoriesFull) {

                    if (item.getTitle().toLowerCase().contains(stringPattern)) {

                        filteredList.add(item);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

            categories.clear();
            categories.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public MenuCategoryRecyclerViewAdapter(List<WashableItemCategory> categories) {

        this.categories = categories;
        this.categoriesFull = new ArrayList<>(this.categories);
    }

    public List<WashableItemCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<WashableItemCategory> categories) {
        this.categories = categories;
    }

    public void setMenuCategoryClickListener(IMenuCategoryClickListener menuCategoryClickListener) {
        this.menuCategoryClickListener = menuCategoryClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_menu_category, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuCategoryRecyclerViewAdapter.ViewHolder holder, int position) {

        WashableItemCategory category = categories.get(position);

        holder.textViewTitle.setText(category.getTitle());
        holder.textViewItemCount.setText(MessageFormat
                .format("Items: {0}", category.getWashableItems().size()));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewTitle;
        private final TextView textViewItemCount;
        private final ImageButton buttonEdit;
        private final ImageButton buttonDelete;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewItemCount = itemView.findViewById(R.id.text_view_item_count);
            buttonEdit = itemView.findViewById(R.id.button_edit);
            buttonDelete = itemView.findViewById(R.id.button_delete);

            itemView.setOnClickListener(view -> {

                int position = getAdapterPosition();

                if (menuCategoryClickListener != null
                        && position != RecyclerView.NO_POSITION) {

                    WashableItemCategory category = categories.get(position);

                    menuCategoryClickListener.onMenuCategoryClick(category, position);
                }
            });

            buttonEdit.setOnClickListener(view -> {

                int position = getAdapterPosition();

                if (menuCategoryClickListener != null
                        && position != RecyclerView.NO_POSITION) {

                    WashableItemCategory category = categories.get(position);

                    menuCategoryClickListener.onMenuCategoryEditClick(category, position);
                }
            });

            buttonDelete.setOnClickListener(view -> {

                int position = getAdapterPosition();

                if (menuCategoryClickListener != null
                        && position != RecyclerView.NO_POSITION) {

                    menuCategoryClickListener.onMenuCategoryDeleteClick(position);
                }
            });
        }
    }
}
