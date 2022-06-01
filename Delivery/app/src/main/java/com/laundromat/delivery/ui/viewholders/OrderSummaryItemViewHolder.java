package com.laundromat.delivery.ui.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.delivery.R;


public class OrderSummaryItemViewHolder extends RecyclerView.ViewHolder {

    TextView textViewItem;
    TextView textViewPrice;

    public OrderSummaryItemViewHolder(@NonNull View itemView) {
        super(itemView);

        textViewItem = itemView.findViewById(R.id.text_view_item);
        textViewPrice = itemView.findViewById(R.id.text_view_price);
    }
}
