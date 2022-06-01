package com.laundromat.customer.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.customer.R;
import com.laundromat.customer.model.washable.ServiceType;
import com.laundromat.customer.ui.interfaces.IQuantitySelectedListener;
import com.travijuu.numberpicker.library.NumberPicker;

import java.text.MessageFormat;
import java.util.List;

public class SelectServicesRecyclerAdapter
        extends RecyclerView.Adapter<SelectServicesRecyclerAdapter.ViewHolder> {

    private List<ServiceType> serviceTypes;
    private IQuantitySelectedListener quantitySelectedListener;

    public SelectServicesRecyclerAdapter(List<ServiceType> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public void setQuantitySelectedListener(IQuantitySelectedListener quantitySelectedListener) {
        this.quantitySelectedListener = quantitySelectedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_select_service, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectServicesRecyclerAdapter.ViewHolder holder, int position) {

        ServiceType serviceType = serviceTypes.get(position);

        holder.textViewName.setText(serviceType.getName());
        holder.textViewPrice.setText(MessageFormat.format("PKR {0}", serviceType.getPrice()));
        holder.numberPickerQuantity.setValue(serviceType.getQuantity());
        holder.numberPickerQuantity.setValueChangedListener((quantity, action) -> {

            if (quantitySelectedListener != null) {

                quantitySelectedListener.onQuantitySelected(serviceType, quantity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return serviceTypes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewName;
        private final TextView textViewPrice;
        private NumberPicker numberPickerQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewPrice = itemView.findViewById(R.id.text_view_price);
            numberPickerQuantity = itemView.findViewById(R.id.number_picker_quantity);
        }
    }
}
