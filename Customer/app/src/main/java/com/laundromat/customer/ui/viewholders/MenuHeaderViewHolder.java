package com.laundromat.customer.ui.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.customer.R;

public class MenuHeaderViewHolder extends RecyclerView.ViewHolder {

    TextView textViewTitle;

    public MenuHeaderViewHolder(@NonNull View itemView) {
        super(itemView);

        textViewTitle = itemView.findViewById(R.id.text_view_title);
    }
}
