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
import com.laundromat.admin.model.Merchant;
import com.laundromat.admin.ui.interfaces.IMerchantClickListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class MerchantsRecyclerAdapter
        extends RecyclerView.Adapter<MerchantsRecyclerAdapter.ViewHolder> implements Filterable {

    private List<Merchant> merchants;
    private List<Merchant> merchantsFull;

    private IMerchantClickListener merchantClickListener;

    private Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Merchant> filteredList = new ArrayList<>();

            if (charSequence.equals(null) || charSequence.length() == 0) {

                filteredList.addAll(merchantsFull);
            } else {

                String stringPattern = charSequence.toString().toLowerCase().trim();

                for (Merchant item : merchantsFull) {

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

            merchants.clear();
            merchants.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public MerchantsRecyclerAdapter(List<Merchant> merchants) {
        this.merchants = merchants;
        this.merchantsFull = new ArrayList<>(merchants);
    }

    public void setMerchantClickListener(IMerchantClickListener merchantClickListener) {
        this.merchantClickListener = merchantClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_merchant, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MerchantsRecyclerAdapter.ViewHolder holder, int position) {

        Merchant merchant = merchants.get(position);

        String id = "Merchant ID: " + merchant.getId().substring(merchant.getId().length() - 10);
        holder.textViewId.setText(id);

        holder.textViewName.setText(MessageFormat
                .format("Name: {0}", merchant.getFullName()));

        holder.textViewNic.setText(MessageFormat
                .format("Nic: {0}", merchant.getNicNumber()));

        holder.textViewDate.setText(merchant.getDateCreated());

        holder.textViewLaundryName.setText(merchant.getLaundry().getName());
    }

    @Override
    public int getItemCount() {
        return merchants.size();
    }

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewId;
        TextView textViewName;
        TextView textViewNic;
        TextView textViewDate;
        TextView textViewLaundryName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewId = itemView.findViewById(R.id.text_view_id);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewNic = itemView.findViewById(R.id.text_view_nic);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewLaundryName = itemView.findViewById(R.id.text_view_laundry_name);
            itemView.setOnClickListener(view -> {

                int position = getAdapterPosition();

                if (merchantClickListener != null
                        && position != RecyclerView.NO_POSITION) {

                    Merchant merchant = merchants.get(position);

                    merchantClickListener.onMerchantClick(position, merchant);
                }
            });
        }
    }
}
