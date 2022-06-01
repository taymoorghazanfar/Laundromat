package com.laundromat.merchant.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.merchant.R;
import com.laundromat.merchant.model.washable.WashableItem;
import com.laundromat.merchant.ui.interfaces.IMenuItemClickListener;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class MenuItemRecyclerViewAdapter
        extends RecyclerView.Adapter<MenuItemRecyclerViewAdapter.ViewHolder> implements Filterable {

    private List<WashableItem> items;
    private List<WashableItem> itemsFull;

    private IMenuItemClickListener menuItemClickListener;
    private Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<WashableItem> filteredList = new ArrayList<>();

            if (charSequence.equals(null) || charSequence.length() == 0) {

                filteredList.addAll(itemsFull);
            } else {

                String stringPattern = charSequence.toString().toLowerCase().trim();

                for (WashableItem item : itemsFull) {

                    if (item.getName().toLowerCase().contains(stringPattern)) {

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

            items.clear();
            items.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public MenuItemRecyclerViewAdapter(List<WashableItem> items) {

        this.items = items;
        this.itemsFull = new ArrayList<>(this.items);
    }

    public List<WashableItem> getItems() {
        return items;
    }

    public void setItems(List<WashableItem> items) {
        this.items = items;
    }

    public void setMenuItemClickListener(IMenuItemClickListener menuItemClickListener) {
        this.menuItemClickListener = menuItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_menu_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemRecyclerViewAdapter.ViewHolder holder, int position) {

        WashableItem washableItem = items.get(position);

        Picasso.get()
                .load(washableItem.getImageUrl())
                .into(holder.imageViewItem);

        holder.textViewName.setText(washableItem.getName());
        holder.textViewServicesCount.setText(MessageFormat
                .format("Service(s): {0}", washableItem.getServiceTypes().size()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageViewItem;
        private final TextView textViewName;
        private final TextView textViewServicesCount;
        private final ImageButton buttonEdit;
        private final ImageButton buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewItem = itemView.findViewById(R.id.image_view_item_rec);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewServicesCount = itemView.findViewById(R.id.text_view_services_count);
            buttonEdit = itemView.findViewById(R.id.button_edit);
            buttonDelete = itemView.findViewById(R.id.button_delete);

            itemView.setOnClickListener(view -> {

                int position = getAdapterPosition();

                if (menuItemClickListener != null
                        && position != RecyclerView.NO_POSITION) {

                    WashableItem washableItem = items.get(position);

                    menuItemClickListener.onMenuItemClick(washableItem);
                }
            });

            buttonEdit.setOnClickListener(view -> {

                int position = getAdapterPosition();

                if (menuItemClickListener != null
                        && position != RecyclerView.NO_POSITION) {

                    WashableItem washableItem = items.get(position);

                    menuItemClickListener.onMenuItemEditClick(washableItem, position);
                }
            });

            buttonDelete.setOnClickListener(view -> {

                int position = getAdapterPosition();

                if (menuItemClickListener != null
                        && position != RecyclerView.NO_POSITION) {

                    menuItemClickListener.onMenuItemDeleteClick(position);
                }
            });
        }
    }
}
