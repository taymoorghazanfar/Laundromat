package com.laundromat.customer.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.customer.R;
import com.laundromat.customer.model.util.Location;
import com.laundromat.customer.ui.interfaces.IAddressClickListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.laundromat.customer.utils.LocationUtils.getAddressFromLatLng;

public class AddressesRecyclerViewAdapter
        extends RecyclerView.Adapter<AddressesRecyclerViewAdapter.ViewHolder> implements Filterable {

    private Context context;
    private List<Location> addresses;
    private List<Location> addressesFull;
    private IAddressClickListener addressClickListener;
    private Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Location> filteredList = new ArrayList<>();

            if (charSequence.equals(null) || charSequence.length() == 0) {

                filteredList.addAll(addressesFull);
            } else {

                String stringPattern = charSequence.toString().toLowerCase().trim();

                for (Location item : addressesFull) {

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

            addresses.clear();
            addresses.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public AddressesRecyclerViewAdapter(Context context, List<Location> addresses) {
        this.context = context;
        this.addresses = addresses;
        this.addressesFull = new ArrayList<>(addresses);
    }

    public void setAddressClickListener(IAddressClickListener addressClickListener) {
        this.addressClickListener = addressClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_address, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressesRecyclerViewAdapter.ViewHolder holder, int position) {

        Location address = addresses.get(position);

        holder.textViewName.setText(address.getName());

        String addressDescription = getAddressFromLatLng(
                context,
                address.getLatLng().latitude,
                address.getLatLng().longitude);

        if (addressDescription != null) {

            holder.textViewAddress.setText(addressDescription);

        } else {

            holder.textViewAddress.setText(MessageFormat.format("{0}, {1}",
                    address.getLatLng().latitude, address.getLatLng().latitude));
        }
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewName;
        private final TextView textViewAddress;
        private final ImageButton buttonEdit;
        private final ImageButton buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewAddress = itemView.findViewById(R.id.text_view_address);
            buttonEdit = itemView.findViewById(R.id.button_edit);
            buttonDelete = itemView.findViewById(R.id.button_delete);

            buttonEdit.setOnClickListener(view -> {

                int position = getAdapterPosition();

                if (addressClickListener != null
                        && position != RecyclerView.NO_POSITION) {

                    addressClickListener.onAddressEditClick(position);
                }
            });

            buttonDelete.setOnClickListener(view -> {

                int position = getAdapterPosition();

                if (addressClickListener != null
                        && position != RecyclerView.NO_POSITION) {

                    addressClickListener.onAddressDeleteClick(position);
                }
            });
        }
    }
}
