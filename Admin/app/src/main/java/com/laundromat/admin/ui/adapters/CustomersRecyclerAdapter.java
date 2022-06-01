package com.laundromat.admin.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.admin.R;
import com.laundromat.admin.model.Customer;
import com.laundromat.admin.ui.interfaces.ICustomerClickListener;
import com.laundromat.admin.utils.LocationUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class CustomersRecyclerAdapter
        extends RecyclerView.Adapter<CustomersRecyclerAdapter.ViewHolder> implements Filterable {

    private Context context;
    private List<Customer> customers;
    private List<Customer> customersFull;

    private ICustomerClickListener customerClickListener;

    private Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Customer> filteredList = new ArrayList<>();

            if (charSequence.equals(null) || charSequence.length() == 0) {

                filteredList.addAll(customersFull);
            } else {

                String stringPattern = charSequence.toString().toLowerCase().trim();

                for (Customer item : customersFull) {

                    if (item.getId().substring(item.getId().length() - 10).toLowerCase().contains(stringPattern)) {

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

            customers.clear();
            customers.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public CustomersRecyclerAdapter(Context context, List<Customer> customers) {

        this.context = context;
        this.customers = customers;
        this.customersFull = new ArrayList<>(customers);
    }

    public void setCustomerClickListener(ICustomerClickListener customerClickListener) {
        this.customerClickListener = customerClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_customer, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomersRecyclerAdapter.ViewHolder holder, int position) {

        Customer customer = customers.get(position);

        String id = "Customer ID: " + customer.getId().substring(customer.getId().length() - 10);
        holder.textViewId.setText(id);

        holder.textViewName.setText(MessageFormat
                .format("Name: {0}", customer.getFullName()));

        holder.textViewDate.setText(customer.getDateCreated());

        String locationAddress = LocationUtils.getAddressFromLatLng(context,
                customer.getLocation().latitude, customer.getLocation().longitude);

        holder.textViewLocation.setText(locationAddress);
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewId;
        TextView textViewName;
        TextView textViewDate;
        TextView textViewLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewId = itemView.findViewById(R.id.text_view_id);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewLocation = itemView.findViewById(R.id.text_view_location_address);
            itemView.setOnClickListener(view -> {

                int position = getAdapterPosition();

                if (customerClickListener != null
                        && position != RecyclerView.NO_POSITION) {

                    Customer customer = customers.get(position);

                    customerClickListener.onCustomerClick(position, customer);
                }
            });
        }
    }
}
