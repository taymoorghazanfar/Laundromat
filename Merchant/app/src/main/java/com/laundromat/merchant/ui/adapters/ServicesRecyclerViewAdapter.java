package com.laundromat.merchant.ui.adapters;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.merchant.R;
import com.laundromat.merchant.model.washable.ServiceType;
import com.laundromat.merchant.model.washable.WashableItem;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ServicesRecyclerViewAdapter
        extends RecyclerView.Adapter<ServicesRecyclerViewAdapter.ViewHolder> {

    private ArrayList<ServiceType> serviceTypes;

    public ServicesRecyclerViewAdapter(ArrayList<ServiceType> serviceTypes) {

        this.serviceTypes = serviceTypes;
    }

    public List<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(ArrayList<ServiceType> serviceTypes) {
        this.serviceTypes = serviceTypes;
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
    public void onBindViewHolder(@NonNull ServicesRecyclerViewAdapter.ViewHolder holder, int position) {

        ServiceType serviceType = serviceTypes.get(position);

        holder.textViewName.setText(serviceType.getName());

        holder.editTextPrice.setText(String.valueOf(serviceType.getPrice()));

        holder.checkBoxEnabled.setChecked(serviceType.isActive());
    }

    @Override
    public int getItemCount() {
        return serviceTypes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewName;
        private final EditText editTextPrice;
        private CheckBox checkBoxEnabled;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.text_view_name);
            editTextPrice = itemView.findViewById(R.id.edit_text_price);
            checkBoxEnabled = itemView.findViewById(R.id.checkbox_enabled);

            editTextPrice.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    String text = editTextPrice.getText().toString().trim();

                    int position = getAdapterPosition();

                    serviceTypes.get(position)
                            .setPrice(TextUtils.isEmpty(text) ? 0.0 : Double.parseDouble(text));

                    Log.d("price", "afterTextChanged: " + serviceTypes.get(position).getPrice());
                }
            });

            checkBoxEnabled.setOnCheckedChangeListener((compoundButton, state) -> {

                int position = getAdapterPosition();

                serviceTypes.get(position).setActive(state);
            });
        }
    }
}
