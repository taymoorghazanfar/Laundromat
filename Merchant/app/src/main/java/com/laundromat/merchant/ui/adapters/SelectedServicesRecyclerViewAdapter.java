package com.laundromat.merchant.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.merchant.R;
import com.laundromat.merchant.model.washable.ServiceType;

import java.text.MessageFormat;
import java.util.List;

public class SelectedServicesRecyclerViewAdapter
        extends RecyclerView.Adapter<SelectedServicesRecyclerViewAdapter.ViewHolder> {

    private List<ServiceType> serviceTypes;

    public SelectedServicesRecyclerViewAdapter(List<ServiceType> serviceTypes) {

        this.serviceTypes = serviceTypes;
    }

    public List<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(List<ServiceType> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_show_service, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedServicesRecyclerViewAdapter.ViewHolder holder, int position) {

        ServiceType serviceType = serviceTypes.get(position);

        holder.textViewName.setText(serviceType.getName());

        holder.textViewPrice.setText(MessageFormat
                .format("PKR {0}", serviceType.getPrice()));
    }

    @Override
    public int getItemCount() {
        return serviceTypes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewName;
        private final TextView textViewPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewPrice = itemView.findViewById(R.id.text_view_price);
        }
    }
}
