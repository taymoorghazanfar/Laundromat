package com.laundromat.merchant.ui.interfaces;

import com.laundromat.merchant.model.order.Order;

public interface IRequestedOrderClickListener {

    void onRequestedOrderClick(Order order);
}
