package com.laundromat.customer.ui.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

@CoordinatorLayout.DefaultBehavior(CardViewCustomBehavior.class)
public class CustomCardView extends CardView {

    public CustomCardView(@NonNull Context context) {
        super(context);
    }

    public CustomCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}