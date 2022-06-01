package com.laundromat.admin.ui.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.admin.R;

public class LaundryServiceChildViewHolder extends RecyclerView.ViewHolder {

    TextView textViewItemName;
    TextView textViewPrice;

    public LaundryServiceChildViewHolder(@NonNull View itemView) {
        super(itemView);

        textViewItemName = itemView.findViewById(R.id.text_view_item_name);
        textViewPrice = itemView.findViewById(R.id.text_view_price);
    }
}