package com.laundromat.merchant.ui.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.merchant.R;

public class OrderSummaryHeaderViewHolder extends RecyclerView.ViewHolder {

    TextView textViewServiceName;

    public OrderSummaryHeaderViewHolder(@NonNull View itemView) {
        super(itemView);

        textViewServiceName = itemView.findViewById(R.id.text_view_name);
    }
}