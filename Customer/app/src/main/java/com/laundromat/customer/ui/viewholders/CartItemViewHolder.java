package com.laundromat.customer.ui.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.customer.R;
import com.makeramen.roundedimageview.RoundedImageView;

public class CartItemViewHolder extends RecyclerView.ViewHolder {

    ImageView imageViewItem;
    TextView textViewName;
    TextView textViewQuantity;
    TextView textViewPrice;

    public CartItemViewHolder(@NonNull View itemView) {
        super(itemView);

        imageViewItem = itemView.findViewById(R.id.image_view_item);
        textViewName = itemView.findViewById(R.id.text_view_item_name);
        textViewQuantity = itemView.findViewById(R.id.text_view_item_quantity);
        textViewPrice = itemView.findViewById(R.id.text_view_price);
    }
}
