package com.laundromat.customer.ui.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.customer.R;

public class CartItemHeaderViewHolder extends RecyclerView.ViewHolder {

    TextView textViewServiceName;

    public CartItemHeaderViewHolder(@NonNull View itemView) {
        super(itemView);

        textViewServiceName = itemView.findViewById(R.id.text_view_name);
    }
}
