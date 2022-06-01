package com.laundromat.customer.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.customer.R;
import com.laundromat.customer.model.washable.ServiceType;
import com.laundromat.customer.ui.interfaces.IServiceClickListener;

import java.text.MessageFormat;
import java.util.List;

public class ServicesRecyclerAdapter
        extends RecyclerView.Adapter<ServicesRecyclerAdapter.ViewHolder> {

    private List<ServiceType> services;
    private IServiceClickListener serviceClickListener;

    public ServicesRecyclerAdapter(List<ServiceType> services) {
        this.services = services;
    }

    public void setServiceClickListener(IServiceClickListener serviceClickListener) {
        this.serviceClickListener = serviceClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_service, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicesRecyclerAdapter.ViewHolder holder, int position) {

        ServiceType service = services.get(position);

        holder.textViewName.setText(service.getName());
        holder.textViewPrice.setText(MessageFormat.format("PKR {0}", service.getPrice()));
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewName;
        private final TextView textViewPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewPrice = itemView.findViewById(R.id.text_view_price);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (serviceClickListener != null) {

                        serviceClickListener.onServiceClick();
                    }
                }
            });
        }
    }
}
