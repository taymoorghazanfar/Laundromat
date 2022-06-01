package com.laundromat.admin.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.admin.R;
import com.laundromat.admin.model.order.Order;
import com.laundromat.admin.model.util.PaymentMethod;
import com.laundromat.admin.ui.interfaces.IOrderClickListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class OrdersRecyclerAdapter
        extends RecyclerView.Adapter<OrdersRecyclerAdapter.ViewHolder> implements Filterable {

    private List<Order> orders;
    private List<Order> ordersFull;
    private IOrderClickListener orderClickListener;
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

    public OrdersRecyclerAdapter(List<Order> orders) {
        this.orders = orders;
        this.ordersFull = new ArrayList<>(orders);
    }

    public void setOrders(List<Order> orders) {

        this.orders.clear();
        this.orders = orders;
    }

    public void setOrderClickListener(IOrderClickListener orderClickListener) {
        this.orderClickListener = orderClickListener;
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
    public void onBindViewHolder(@NonNull OrdersRecyclerAdapter.ViewHolder holder, int position) {

        Order order = orders.get(position);

        String orderId = "Order ID: " + order.getId().substring(order.getId().length() - 10);
        holder.textViewOrderId.setText(orderId);

        holder.textViewLaundryName.setText(order.getLaundryName());

        holder.textViewQuantity.setText(MessageFormat.format("{0} Items",
                order.getItemsQuantity()));

        holder.textViewPrice.setText(MessageFormat.format("PKR {0}", order.getPrice()));

        holder.textViewDate.setText(order.getDateCreated());

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

        TextView textViewOrderId;
        TextView textViewLaundryName;
        TextView textViewQuantity;
        TextView textViewPrice;
        TextView textViewDate;
        TextView textViewPaymentMethod;
        TextView textViewStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewOrderId = itemView.findViewById(R.id.text_view_order_id);
            textViewLaundryName = itemView.findViewById(R.id.text_view_laundry_name);
            textViewQuantity = itemView.findViewById(R.id.text_view_quantity);
            textViewPrice = itemView.findViewById(R.id.text_view_price);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewPaymentMethod = itemView.findViewById(R.id.text_view_payment_method);
            textViewStatus = itemView.findViewById(R.id.text_view_status);

            itemView.setOnClickListener(view -> {

                int position = getAdapterPosition();

                if (orderClickListener != null
                        && position != RecyclerView.NO_POSITION) {

                    Order order = orders.get(position);

                    orderClickListener.onOrderClick(order);
                }
            });
        }
    }
}