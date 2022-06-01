package com.laundromat.merchant.ui.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.merchant.R;
import com.makeramen.roundedimageview.RoundedImageView;

public class MenuItemViewHolder extends RecyclerView.ViewHolder {

    TextView textViewName;
    RoundedImageView imageViewItem;
    RecyclerView recyclerViewServices;

    public MenuItemViewHolder(@NonNull View itemView) {
        super(itemView);

        this.textViewName = itemView.findViewById(R.id.text_view_name);
        imageViewItem = itemView.findViewById(R.id.image_view_item);
        recyclerViewServices = itemView.findViewById(R.id.recycler_view_services);
    }
}
