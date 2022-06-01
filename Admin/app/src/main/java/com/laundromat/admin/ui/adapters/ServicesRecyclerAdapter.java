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
import com.laundromat.admin.model.Laundry;
import com.laundromat.admin.model.Merchant;
import com.laundromat.admin.model.washable.ServiceType;
import com.laundromat.admin.model.washable.WashableItem;
import com.laundromat.admin.model.washable.WashableItemCategory;
import com.laundromat.admin.prefs.Session;
import com.laundromat.admin.ui.interfaces.IServiceTypeClickListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ServicesRecyclerAdapter
        extends RecyclerView.Adapter<ServicesRecyclerAdapter.ViewHolder> implements Filterable {

    private List<ServiceType> serviceTypes;
    private List<ServiceType> serviceTypesFull;

    private IServiceTypeClickListener serviceTypeClickListener;

    private Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<ServiceType> filteredList = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0) {

                filteredList.addAll(serviceTypesFull);
            } else {

                String stringPattern = charSequence.toString().toLowerCase().trim();

                for (ServiceType item : serviceTypesFull) {

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

            serviceTypes.clear();
            serviceTypes.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public ServicesRecyclerAdapter(List<ServiceType> serviceTypes) {

        this.serviceTypes = serviceTypes;
        this.serviceTypesFull = new ArrayList<>(this.serviceTypes);
    }

    public List<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(List<ServiceType> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public void setServiceTypeClickListener(IServiceTypeClickListener serviceTypeClickListener) {
        this.serviceTypeClickListener = serviceTypeClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_service_type, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicesRecyclerAdapter.ViewHolder holder, int position) {

        ServiceType serviceType = serviceTypes.get(position);

        holder.textViewName.setText(serviceType.getName());

        int count = 0;

        for (Merchant merchant : Session.user.getMerchants()) {

            Laundry laundry = merchant.getLaundry();

            for (WashableItemCategory washableItemCategory : laundry.getMenu()) {

                for (WashableItem washableItem : washableItemCategory.getWashableItems()) {

                    for (ServiceType service : washableItem.getServiceTypes()) {

                        if (service.getName().equals(serviceType.getName())) {

                            count++;
                            break;
                        }
                    }
                }
            }
        }

        holder.textViewItemCount.setText(MessageFormat
                .format("Items: {0}", count));
    }

    @Override
    public int getItemCount() {
        return serviceTypes.size();
    }

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewName;
        private final TextView textViewItemCount;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewItemCount = itemView.findViewById(R.id.text_view_item_count);

            itemView.setOnClickListener(view -> {

                int position = getAdapterPosition();

                if (serviceTypeClickListener != null
                        && position != RecyclerView.NO_POSITION) {

                    ServiceType serviceType = serviceTypes.get(position);

                    serviceTypeClickListener.onServiceClick(serviceType);
                }
            });
        }
    }
}
