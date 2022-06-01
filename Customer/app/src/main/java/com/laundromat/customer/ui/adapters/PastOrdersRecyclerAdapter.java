package com.laundromat.customer.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.customer.R;
import com.laundromat.customer.model.order.Order;
import com.laundromat.customer.model.util.PaymentMethod;
import com.laundromat.customer.ui.interfaces.IPastOrderClickListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class PastOrdersRecyclerAdapter
        extends RecyclerView.Adapter<PastOrdersRecyclerAdapter.ViewHolder> implements Filterable {

    private List<Order> orders;
    private List<Order> ordersFull;
    private IPastOrderClickListener pastOrderClickListener;
    private Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Order> filteredList = new ArrayList<>();

            if (charSequence.equals(null) || charSequence.length() == 0) {

                filteredList.addAll(ordersFull);
            } else {

                String stringPattern = charSequence.toString().toLowerCase().trim();

                for (Order item : ordersFull) {

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

            orders.clear();
            orders.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public PastOrdersRecyclerAdapter(List<Order> orders) {
        this.orders = orders;
        this.ordersFull = new ArrayList<>(orders);
    }

    public void setOrders(List<Order> orders) {

        this.orders.clear();
        this.orders = orders;
    }

    public void setPastOrderClickListener(IPastOrderClickListener pastOrderClickListener) {
        this.pastOrderClickListener = pastOrderClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_order, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PastOrdersRecyclerAdapter.ViewHolder holder, int position) {

        Order order = orders.get(position);

        holder.textViewLaundryName.setText(order.getLaundryName());

        String text = "";

        if (order.getDateCompleted() == null) {

            text = "Order ID: " + order.getId().substring(order.getId().length() - 10)
                    + "  |  " + order.getDateCreated();

        } else {

            text = "Order ID: " + order.getId().substring(order.getId().length() - 10)
                    + "  |  " + order.getDateCompleted();
        }

        holder.textViewOrder.setText(text);

        holder.textViewPrice.setText(MessageFormat.format("PKR {0}", order.getPrice()));

        String paymentMethod;

        if (order.getPaymentMethod() == PaymentMethod.JAZZ_CASH) {

            paymentMethod = "JAZZ CASH";
            holder.textViewPaymentMethod.setBackgroundColor(Color.parseColor("#F44336"));

        } else {

            paymentMethod = "CASH";
            holder.textViewPaymentMethod.setBackgroundColor(Color.parseColor("#4CAF50"));
        }

        holder.textViewPaymentMethod.setText(paymentMethod);

        holder.textViewStatus.setText(order.getStatus().toString().replace("_", " "));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewLaundryName;
        TextView textViewOrder;
        TextView textViewPrice;
        TextView textViewPaymentMethod;
        TextView textViewStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewLaundryName = itemView.findViewById(R.id.text_view_laundry_name);
            textViewOrder = itemView.findViewById(R.id.text_view_order);
            textViewPrice = itemView.findViewById(R.id.text_view_price);
            textViewPaymentMethod = itemView.findViewById(R.id.text_view_payment_method);
            textViewStatus = itemView.findViewById(R.id.text_view_status);

            itemView.setOnClickListener(view -> {

                int position = getAdapterPosition();

                if (pastOrderClickListener != null
                        && position != RecyclerView.NO_POSITION) {

                    Order order = orders.get(position);

                    pastOrderClickListener.onPastOrderClick(order);
                }
            });
        }
    }
}
