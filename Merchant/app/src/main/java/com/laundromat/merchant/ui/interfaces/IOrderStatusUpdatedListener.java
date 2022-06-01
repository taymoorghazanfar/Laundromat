package com.laundromat.merchant.ui.interfaces;

import com.laundromat.merchant.model.order.OrderStatus;

public interface IOrderStatusUpdatedListener {

    void onOrderStatusUpdated(OrderStatus status);
}
