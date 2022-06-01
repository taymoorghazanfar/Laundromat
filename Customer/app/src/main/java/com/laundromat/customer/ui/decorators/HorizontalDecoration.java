package com.laundromat.customer.ui.decorators;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class HorizontalDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public HorizontalDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;
        outRect.top = space;

        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.left = space;
        } else {
            outRect.left = 0;
        }

        if(parent.getChildLayoutPosition(view) == (parent.getChildCount()+1)){

            outRect.right = 0;
        }

        else {

            outRect.right = space;
        }
    }
}