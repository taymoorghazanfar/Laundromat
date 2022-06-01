package com.laundromat.admin.ui.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.admin.R;

public class LaundryServiceHeaderViewHolder extends RecyclerView.ViewHolder {

    TextView textViewLaundryName;

    public LaundryServiceHeaderViewHolder(@NonNull View itemView) {
        super(itemView);

        textViewLaundryName = itemView.findViewById(R.id.text_view_laundry_name);
    }
}