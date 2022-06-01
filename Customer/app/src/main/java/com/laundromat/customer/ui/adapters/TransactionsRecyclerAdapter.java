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
import com.laundromat.customer.model.Transaction;
import com.laundromat.customer.model.util.PaymentMethod;
import com.laundromat.customer.model.util.TransactionType;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class TransactionsRecyclerAdapter
        extends RecyclerView.Adapter<TransactionsRecyclerAdapter.ViewHolder> implements Filterable {

    private List<Transaction> transactions;
    private List<Transaction> transactionsFull;
    private Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Transaction> filteredList = new ArrayList<>();

            if (charSequence.equals(null) || charSequence.length() == 0) {

                filteredList.addAll(transactionsFull);
            } else {

                String stringPattern = charSequence.toString().toLowerCase().trim();

                for (Transaction item : transactionsFull) {

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

            transactions.clear();
            transactions.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public TransactionsRecyclerAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
        this.transactionsFull = new ArrayList<>(transactions);
    }

    public void setTransactions(List<Transaction> transactions) {

        this.transactions.clear();
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_transaction, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionsRecyclerAdapter.ViewHolder holder, int position) {

        Transaction transaction = transactions.get(position);

        holder.textViewId.setText
                (MessageFormat.format("ID: {0}",
                        transaction.getId().substring(transaction.getId().length() - 10)));

        holder.textViewDate.setText(transaction.getDateCreated());
        holder.textViewPrice.setText(MessageFormat.format("PKR {0}", transaction.getAmount()));

        String paymentMethod;

        if (transaction.getPaymentMethod() == PaymentMethod.JAZZ_CASH) {

            paymentMethod = "JAZZ CASH";
            holder.textViewPaymentMethod.setBackgroundColor(Color.parseColor("#F44336"));

        } else {

            paymentMethod = "CASH";
            holder.textViewPaymentMethod.setBackgroundColor(Color.parseColor("#4CAF50"));
        }

        String type = "";

        if (transaction.getType() == TransactionType.ORDER_PAYMENT) {

            type = "Order Payment";

        } else if (transaction.getType() == TransactionType.PICKUP_FEE) {

            type = "Pickup Fee";
        }

        holder.textViewPaymentMethod.setText(paymentMethod);
        holder.textViewType.setText(type);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewId;
        TextView textViewDate;
        TextView textViewPrice;
        TextView textViewPaymentMethod;
        TextView textViewType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewId = itemView.findViewById(R.id.text_view_id);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewPrice = itemView.findViewById(R.id.text_view_price);
            textViewPaymentMethod = itemView.findViewById(R.id.text_view_payment_method);
            textViewType = itemView.findViewById(R.id.text_view_type);
        }
    }
}
