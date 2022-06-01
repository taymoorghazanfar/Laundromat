package com.laundromat.admin.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.admin.R;
import com.laundromat.admin.model.DeliveryBoy;
import com.laundromat.admin.model.Merchant;
import com.laundromat.admin.ui.interfaces.IDriverClickListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class DriversRecyclerAdapter
        extends RecyclerView.Adapter<DriversRecyclerAdapter.ViewHolder> implements Filterable {

    private List<DeliveryBoy> drivers;
    private List<DeliveryBoy> driversFull;

    private IDriverClickListener driverClickListener;

    private Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<DeliveryBoy> filteredList = new ArrayList<>();

            if (charSequence.equals(null) || charSequence.length() == 0) {

                filteredList.addAll(driversFull);
            } else {

                String stringPattern = charSequence.toString().toLowerCase().trim();

                for (DeliveryBoy item : driversFull) {

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

            drivers.clear();
            drivers.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public DriversRecyclerAdapter(List<DeliveryBoy> drivers) {
        this.drivers = drivers;
        this.driversFull = new ArrayList<>(drivers);
    }

    public void setDriverClickListener(IDriverClickListener driverClickListener) {
        this.driverClickListener = driverClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_driver, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriversRecyclerAdapter.ViewHolder holder, int position) {

        DeliveryBoy driver = drivers.get(position);

        String id = "Driver ID: " + driver.getId().substring(driver.getId().length() - 10);
        holder.textViewId.setText(id);

        holder.textViewName.setText(MessageFormat
                .format("Name: {0}", driver.getFullName()));

        holder.textViewNic.setText(MessageFormat
                .format("Nic: {0}", driver.getNicNumber()));

        holder.textViewLicense.setText(MessageFormat
                .format("License: {0}", driver.getLicenseNumber()));

        holder.textViewDate.setText(driver.getDateCreated());

        holder.textViewPlateNumber.setText(driver.getVehicle().getPlateNumber());
    }

    @Override
    public int getItemCount() {
        return drivers.size();
    }

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewId;
        TextView textViewName;
        TextView textViewNic;
        TextView textViewLicense;
        TextView textViewDate;
        TextView textViewPlateNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewId = itemView.findViewById(R.id.text_view_id);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewNic = itemView.findViewById(R.id.text_view_nic);
            textViewLicense = itemView.findViewById(R.id.text_view_license);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewPlateNumber = itemView.findViewById(R.id.text_view_plate_number);
            itemView.setOnClickListener(view -> {

                int position = getAdapterPosition();

                if (driverClickListener != null
                        && position != RecyclerView.NO_POSITION) {

                    DeliveryBoy driver = drivers.get(position);

                    driverClickListener.onDriverClick(position, driver);
                }
            });
        }
    }
}
